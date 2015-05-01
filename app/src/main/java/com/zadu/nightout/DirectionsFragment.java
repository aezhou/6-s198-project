package com.zadu.nightout;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.zadu.nightout.DirectionsFragment.OnDirectionsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DirectionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DirectionsFragment extends Fragment implements PlanChangedListener, OnMapReadyCallback,
        SharedPreferences.OnSharedPreferenceChangeListener, AdapterView.OnItemSelectedListener {
    private static String TAG = "DirectionsFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // Create a place to store the destination spinner
    private Spinner destSpinner;
    private MapFragment mMapFragment;
    private GoogleMap map;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnDirectionsFragmentInteractionListener mListener;
    private Button getDirectionsButton;
    private Button callRideButton;

    private MyOpenHelper mSqlHelper;
    private SharedPreferences mSharedPrefs;

    private static String homeLat = null;
    private static String homeLng = null;
    private static String homePlaceID = null;
    private static String tempETA = null;

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
        // TODO: add code to fill in initial values for home address if null and put them in sharedprefs (HELPER WEEEEEE!!!!!)
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

        // Populate the destination spinner
        // TODO: Make show actual dest name, Home, and Other
        destSpinner = (Spinner) v.findViewById(R.id.dest_spinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.dest_spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destSpinner.setAdapter(adapter);
        destSpinner.setOnItemSelectedListener(this);

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
            // TODO: Get home coords, probably need to cast to Double from String
            if (endpoint.equals("Home") && homeLat != null && homeLng != null) {
                Double homeLatDouble = new Double(homeLat);
                Double homeLngDouble = new Double(homeLng);
                LatLng homeCoords = new LatLng(homeLatDouble, homeLngDouble);
                map.addMarker(new MarkerOptions().position(homeCoords).title("Home"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeCoords, 15));
            }
            else if (endpoint.equals("Other")) {
                // TODO: get and check coords for other, then show if valid
            }
            // If home and other coords are also null, show current location
            else {
                // TODO: use current location
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.OnDirectionsFragmentInteraction(uri);
        }
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
            String destName = mSqlHelper.getPlanDetail((MainActivity) getActivity(), "PLACE_NAME");
            String destAddress = mSqlHelper.getPlanAddressNoPipe((MainActivity) getActivity());
            refreshDirectionFragmentView(getView(), destName, destAddress);
        }
    }

    public void onDestinationChanged(String destName, String destAddress) {
        if (getView() != null) {
            refreshDirectionFragmentView(getView(), destName, destAddress);
        }
    }

    private void refreshDirectionFragmentView(View v, String destName, String destAddress) {
        Log.i(TAG, "refreshing directions fragment");
        setUpMap("Destination");
        TextView destAddressView = (TextView) getView().findViewById(R.id.dest_address);
        destAddressView.setText(destAddress);
        // TODO: update spinner text with destName
        // TODO: update current location, just for good measure
        // TODO: update ETA
    }

    public void updateETAs() {

    }

    // TODO: Remove this?
    public static void setTempETA(String tempETA) {
        tempETA = tempETA;
        Log.i(TAG, "stored the temp ETA: " + tempETA);
    }

    public void onHomeChanged(String homeAddress) {
        // TODO: handle null case
        // TODO: update my fragment UI
        // TODO: find new ID and latlong and store those in sharedprefs
            // TODO: build geocoding URL
            // TODO: call the geocoding API thingy .doInBackground with the URL (will store result)
        makeGeocodingCall(homeAddress);
    }

    private void makeGeocodingCall(String address) {
        // TODO: build up URL for address (used for home address lookup)
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
            // TODO: See if this works
            Log.i(TAG, "API URL: " + apiUrl);
            ((MainActivity) getActivity()).resetGeocodingApiCaller();
            ((MainActivity) getActivity()).googleGeocodingCallApi.execute(apiUrl);
        }
    }

    private String buildReverseGeocodingURL(String lat, String lng) {
        String baseURL = "https://maps.googleapis.com/maps/api/geocode/json?";
        String latLngParam = "latlng=" + lat + "," + lng;
        String keyParam = "&key=AIzaSyD3xH-kCCFsSPonGRRi7isV-O5ejZWIts8";
        return baseURL + latLngParam + keyParam;
    }

    public static void storeHomeInfo(String lat, String lng, String placeID) {
        // TODO: make this store to the SharedPreferences thing, not variables within this fragment (ask Amanda)
        homeLat = lat;
        homeLng = lng;
        homePlaceID = placeID;
        Log.i(TAG, "stored the home stuff");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key == "home_address") {
            String homeAddress = sharedPreferences.getString(key, null);
            onHomeChanged(homeAddress);
        }
    }

    // Selection listener for destination spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Object selected = parent.getItemAtPosition(position);
        String selectionName = selected.toString();
        // TODO: update map
        setUpMap(selectionName);

        // Show either the Plan destination or input for a separate destination, based on selection
        TextView destAddressView = (TextView) getView().findViewById(R.id.dest_address);
        EditText otherDestInput = (EditText) getView().findViewById(R.id.dest_address_other);
        if (selectionName.equals("Other")) {
            destAddressView.setVisibility(View.GONE);
            otherDestInput.setVisibility(View.VISIBLE);
        }
        else {
            otherDestInput.setVisibility(View.GONE);
            destAddressView.setVisibility(View.VISIBLE);
        }
    }

    // Nothing selected listener for destination spinner
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing?
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
        // TODO: Update argument type and name (you can rename listener/method)

        // send things in fragment to listener, which MainActivity extends
        public void OnDirectionsFragmentInteraction(Object object);
        public void callRide(Object something);
        public void openGoogleMapsDirections(Object something);
    }

}
