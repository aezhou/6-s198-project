package com.zadu.nightout;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.zadu.nightout.DirectionsFragment.OnDirectionsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DirectionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DirectionsFragment extends Fragment implements PlanChangedListener, OnMapReadyCallback,
        SharedPreferences.OnSharedPreferenceChangeListener, AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener {
    private static String TAG = "DirectionsFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // Create a place to store the destination spinner
    private Spinner destSpinner;
    private MapFragment mMapFragment;
    private GoogleMap map;
    private AutoCompleteTextView autoCompView;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnDirectionsFragmentInteractionListener mListener;
    private Button refreshETAButton;
    private Button getDirectionsButton;
    private Button callRideButton;

    private MyOpenHelper mSqlHelper;
    private SharedPreferences mSharedPrefs;

    private static MainActivity activity;
    private static View view;

    private String otherDestPlaceID = null;
    private String otherDestLat = null;
    private String otherDestLng = null;
    private static String drivingETA = "?";
    private static String transitETA = "?";
    private static String walkingETA = "?";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DirectionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DirectionsFragment newInstance(String param1, String param2) {
        DirectionsFragment fragment = new DirectionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public DirectionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_directions, container, false);

        map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMap();
        try {
            setUpMap("Destination");
        } catch (NullPointerException e) {
            Log.e(TAG, "No place coords yet.", e);
        }

        // Set listener for the "Refresh ETA Data" button
        refreshETAButton = (Button) v.findViewById(R.id.eta_refresh_button);
        refreshETAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRefreshETAButtonPressed(view);
            }
        });

        // Set listener for the "Get Directions" button
        getDirectionsButton = (Button) v.findViewById(R.id.directions_button);
        getDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGetDirectionsButtonPressed(view);
            }
        });

        // Set listener for the "Call a Ride" button
        callRideButton = (Button) v.findViewById(R.id.call_ride_button);
        callRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCallRideButtonPressed(view);
            }
        });

        // Populate the destination spinner
        updateDestSpinnerContents(v);
        Spinner destSpinner = (Spinner) v.findViewById(R.id.dest_spinner);

        autoCompView = (AutoCompleteTextView) v.findViewById(R.id.dest_address_other);
        autoCompView.clearFocus();
        autoCompView.setAdapter(GooglePlacesAutocompleteAdapter.getInstance(getActivity()));
        autoCompView.setOnItemClickListener(this);

        view = v;
        updateETAs(destSpinner.getSelectedItem().toString(), 0);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        setUpMap("Destination");
    }

    /*
    Show the correct location on the map, based on the user's spinner selection and what is
    available.
    */
    private void setUpMap(String endpoint) {
        map.clear();
        // Show the new destination on the embedded map
        String destName = mSqlHelper.getPlanDetail((MainActivity) getActivity(), "PLACE_NAME");
        Double destLat = mSqlHelper.getPlanLatLong((MainActivity) getActivity(), "PLACE_LAT");
        Double destLng = mSqlHelper.getPlanLatLong((MainActivity) getActivity(), "PLACE_LONG");
        if (!endpoint.equals("Home") && !endpoint.equals("Other") &&
                destLat != null && destLng != null) {
            LatLng destCoords = new LatLng(destLat, destLng);
            map.addMarker(new MarkerOptions().position(destCoords).title(destName));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(destCoords, 15));
        }
        // If the destination coords are null, show home on the map instead
        else {
            String homeLat = mSharedPrefs.getString("home_lat", null);
            String homeLng = mSharedPrefs.getString("home_lng", null);
            if (endpoint.equals("Home") && homeLat != null && homeLng != null) {
                Double homeLatDouble = new Double(homeLat);
                Double homeLngDouble = new Double(homeLng);
                LatLng homeCoords = new LatLng(homeLatDouble, homeLngDouble);
                map.addMarker(new MarkerOptions().position(homeCoords).title("Home"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeCoords, 15));
            }
            else if (endpoint.equals("Other")) {
                if (otherDestLat != null && otherDestLng != null) {
                    Double otherLatDouble = new Double(otherDestLat);
                    Double otherLngDouble = new Double(otherDestLng);
                    LatLng otherCoords = new LatLng(otherLatDouble, otherLngDouble);
                    map.addMarker(new MarkerOptions().position(otherCoords).title("Other"));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(otherCoords, 15));
                }
            }
        }
    }

    private void updateDestSpinnerContents(View view) {
        if (view != null) {
            // FIXME: gets null view when settings home address is updated - Maybe fixed?
            destSpinner = (Spinner) view.findViewById(R.id.dest_spinner);
            ArrayList<String> destSpinnerContents = new ArrayList<String>();
            String placeName = mSqlHelper.getPlanDetail((MainActivity) getActivity(), "PLACE_NAME");
            if (placeName != null) {
                destSpinnerContents.add(placeName);
            }
            String homeAddress = mSharedPrefs.getString("home_address", null);
            if (homeAddress != null) {
                destSpinnerContents.add("Home");
            }
            destSpinnerContents.add("Other");

            ArrayAdapter<String> destSpinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, destSpinnerContents);
            destSpinner.setAdapter(destSpinnerArrayAdapter);
            destSpinner.setOnItemSelectedListener(this);
        }
        else {
            Log.e(TAG, "View was null, not updating destination spinner contents.");
        }
    }

    public void onRefreshETAButtonPressed(Object something) {
        updateETAs(destSpinner.getSelectedItem().toString(), 0);
    }

    public void onGetDirectionsButtonPressed(Object something) {
        if(mListener != null) {
            mListener.openGoogleMapsDirections(something);
        }
    }

    public void onCallRideButtonPressed(Object something) {
        if(mListener != null) {
            mListener.callRide(something);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSqlHelper = ((MainActivity) getActivity()).getSqlHelper();
        try {
            mListener = (OnDirectionsFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPlanChanged() {
        if (getView() != null) {
            refreshDirectionFragmentView();
        }
    }

    public void onDestinationChanged() {
        if (getView() != null) {
            refreshDirectionFragmentView();
        }
    }

    private void refreshDirectionFragmentView() {
        Log.i(TAG, "refreshing directions fragment");
        // Update spinner contents with current destination, which in turn will update map and ETAs
        updateDestSpinnerContents(getView());
    }

    public void updateETAs(final String selection, final int numFailedAttempts) {
        ((MainActivity) getActivity()).getLastLoc();
        String lastLat = ((MainActivity)getActivity()).getLastLat();
        String lastLng = ((MainActivity)getActivity()).getLastLng();
        // If current location found, make call and update UI with ETA results
        if (lastLat != null && lastLng != null) {
            switch (selection) {
                case "Home":
                    String homeAddress = mSharedPrefs.getString("home_address", null);
                    if (homeAddress != null) {
                        makeDistanceMatrixCall(lastLat, lastLng, homeAddress, "driving");
                        makeDistanceMatrixCall(lastLat, lastLng, homeAddress, "transit");
                        makeDistanceMatrixCall(lastLat, lastLng, homeAddress, "walking");
                    }
                    break;
                case "Other":
                    AutoCompleteTextView otherDestAddressEditor = (
                            AutoCompleteTextView) getView().findViewById(R.id.dest_address_other);
                    String otherAddress = otherDestAddressEditor.getText().toString();
                    if (otherAddress != null && otherAddress != "") {
                        makeDistanceMatrixCall(lastLat, lastLng, otherAddress, "driving");
                        makeDistanceMatrixCall(lastLat, lastLng, otherAddress, "transit");
                        makeDistanceMatrixCall(lastLat, lastLng, otherAddress, "walking");
                    }
                    break;
                default:
                    TextView destAddressTextView = (TextView) getView().findViewById(R.id.dest_address);
                    String destAddress = destAddressTextView.getText().toString();
                    if (destAddress != "") {
                        makeDistanceMatrixCall(lastLat, lastLng, destAddress, "driving");
                        makeDistanceMatrixCall(lastLat, lastLng, destAddress, "transit");
                        makeDistanceMatrixCall(lastLat, lastLng, destAddress, "walking");
                    }
                    break;
            }
        }
        // Else, update UI with results unknown
        else {
            setDrivingETA("?");
            setTransitETA("?");
            setWalkingETA("?");
            // Try again in half a second, but only up to twice
            if (numFailedAttempts < 2) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateETAs(selection, numFailedAttempts + 1);
                    }
                }, 500);
            }
        }
    }

    // Used to get ETAs
    private void makeDistanceMatrixCall(String lat, String lng, String destAddress, String mode) {
        String baseURL = "https://maps.googleapis.com/maps/api/distancematrix/json?";
        String currentLocParam = "origins=" + lat + "," + lng;
        String destParam = "&destinations=";
        String encodedDestAddress = null;
        String modeParam = "&mode=" + mode;
        String keyParam = "&key=AIzaSyD3xH-kCCFsSPonGRRi7isV-O5ejZWIts8";
        try {
            encodedDestAddress = URLEncoder.encode(destAddress, "UTF-8");
            destParam = destParam + encodedDestAddress;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Encoding exception");
            e.printStackTrace();
        }
        if (encodedDestAddress != null) {
            String apiUrl = baseURL + currentLocParam + destParam + modeParam + keyParam;
            ((MainActivity) getActivity()).resetDistanceMatrixApiCaller();
            ((MainActivity) getActivity()).googleDistanceMatrixCallApi.execute(apiUrl, mode);
        }
    }

    public static void setDrivingETA(String drivingETAVal) {
        drivingETA = drivingETAVal;
        TextView drivingTimeTextView = (TextView) view.findViewById(R.id.driving_time);
        drivingTimeTextView.setText(drivingETA);
    }

    public static void setTransitETA(String transitETAVal) {
        transitETA = transitETAVal;
        TextView transitTimeTextView = (TextView) view.findViewById(R.id.transit_time);
        transitTimeTextView.setText(transitETA);
    }


    public static void setWalkingETA(String walkingETAVal) {
        walkingETA = walkingETAVal;
        TextView walkingTimeTextView = (TextView) view.findViewById(R.id.walking_time);
        walkingTimeTextView.setText(walkingETA);
    }


    public void onHomeChanged(String homeAddress) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPrefs.edit().putString("home_lat", null).apply();
        sharedPrefs.edit().putString("home_lng", null).apply();
        sharedPrefs.edit().putString("home_place_id", null).apply();
        if (getView() != null) {
            updateDestSpinnerContents(getView());
            // Find new ID and latlong and store those in sharedprefs
            makeGeocodingCall(homeAddress);
        }
        else {
            Log.e(TAG, "View was null, not updating home data (will now be null)");
        }
    }

    // Used for home address lookup to get coords and place_id
    private void makeGeocodingCall(String address) {
        if (address != null && address != "") {
            String baseURL = "https://maps.googleapis.com/maps/api/geocode/json?";
            String addressParam = "address=";
            String encodedAddress = null;
            String keyParam = "&key=AIzaSyD3xH-kCCFsSPonGRRi7isV-O5ejZWIts8";
            try {
                encodedAddress = URLEncoder.encode(address, "UTF-8");
                addressParam = addressParam + encodedAddress;
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Encoding exception");
                e.printStackTrace();
            }
            if (encodedAddress != null) {
                String apiUrl = baseURL + addressParam + keyParam;
                ((MainActivity) getActivity()).resetGeocodingApiCaller();
                ((MainActivity) getActivity()).googleGeocodingCallApi.execute(apiUrl);
            }
        }
    }

    // TODO: remove if not using
    // For current location
    private String buildReverseGeocodingURL(String lat, String lng) {
        String baseURL = "https://maps.googleapis.com/maps/api/geocode/json?";
        String latLngParam = "latlng=" + lat + "," + lng;
        String keyParam = "&key=AIzaSyD3xH-kCCFsSPonGRRi7isV-O5ejZWIts8";
        return baseURL + latLngParam + keyParam;
    }

    public static void storeHomeInfo(String lat, String lng, String placeID) {
        Log.i(TAG, "Storing home info: " + lat + ", " + lng + " for " + placeID);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPrefs.edit().putString("home_lat", lat).apply();
        sharedPrefs.edit().putString("home_lng", lng).apply();
        sharedPrefs.edit().putString("home_place_id", placeID).apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key == "home_address") {
            String homeAddress = sharedPreferences.getString(key, null);
            Log.i(TAG, "Home address changed to: " + homeAddress);
            onHomeChanged(homeAddress);
        }
    }

    // Selection listener for destination spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Object selected = parent.getItemAtPosition(position);
        String selectionName = selected.toString();

        // Show either the Plan destination or input for a separate destination, based on selection
        TextView destAddressView = (TextView) getView().findViewById(R.id.dest_address);
        AutoCompleteTextView otherDestInput = (
                AutoCompleteTextView) getView().findViewById(R.id.dest_address_other);
        if (selectionName.equals("Other")) {
            destAddressView.setVisibility(View.GONE);
            otherDestInput.setVisibility(View.VISIBLE);
        }
        else {
            otherDestInput.setVisibility(View.GONE);
            destAddressView.setVisibility(View.VISIBLE);
            if (selectionName.equals("Home")) {
                destAddressView.setText(mSharedPrefs.getString("home_address", "address unknown"));
            }
            else {
                destAddressView.setText(
                        mSqlHelper.getPlanAddressNoPipe((MainActivity) getActivity()));
            }
        }

        // Update the rest of the UI
        setUpMap(selectionName);
        updateETAs(selectionName, 0);
    }

    // Nothing selected listener for destination spinner
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing?
    }

    // Click listener for Other destination autocomplete
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        resetInternalOtherDestFields();
        String choice = (String) adapterView.getItemAtPosition(position);
        autoCompView.setText(choice);
        // Hide keyboard
        autoCompView.clearFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autoCompView.getWindowToken(), 0);

        try {
            otherDestPlaceID = GooglePlacesAutocompleteAdapter.getPredictions().getJSONObject(((int) id)).getString("place_id");
            GoogleApiClient googleAPIClient = new GoogleApiClient.Builder(this.getActivity())
                    .addConnectionCallbacks((MainActivity) this.getActivity())
                    .addOnConnectionFailedListener((MainActivity) this.getActivity())
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
            Places.GeoDataApi.getPlaceById(googleAPIClient, otherDestPlaceID)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(PlaceBuffer places) {
                            if (places.getStatus().isSuccess()) {
                                final Place myPlace = places.get(0);
                                otherDestLat = String.valueOf(myPlace.getLatLng().latitude);
                                otherDestLng = String.valueOf(myPlace.getLatLng().longitude);
                            }
                            places.release();

                            setUpMap("Other");
                            updateETAs("Other", 0);
                        }
                    });
            googleAPIClient.connect();
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }
    }

    private void resetInternalOtherDestFields() {
        otherDestLat = null;
        otherDestLng = null;
        otherDestPlaceID = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnDirectionsFragmentInteractionListener {
        // send things in fragment to listener, which MainActivity extends
        public void callRide(Object something);
        public void openGoogleMapsDirections(Object something);
    }

}
