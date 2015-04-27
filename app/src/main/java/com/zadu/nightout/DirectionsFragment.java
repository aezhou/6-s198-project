package com.zadu.nightout;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.zadu.nightout.DirectionsFragment.OnDirectionsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DirectionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DirectionsFragment extends Fragment implements PlanChangedListener, OnMapReadyCallback {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_directions, container, false);

        map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                .getMap();
        LatLng coordsMIT = new LatLng(42.3598, -71.0921);
        Marker MIT = map.addMarker(new MarkerOptions().position(coordsMIT)
                .title("MIT"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordsMIT, 15));

        // Populate the destination spinner
        destSpinner = (Spinner) v.findViewById(R.id.dest_spinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.dest_spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destSpinner.setAdapter(adapter);

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
        // TODO: Make use actual coordinates and name of destination
        map.addMarker(new MarkerOptions()
                .position(new LatLng(42, -71))
                .title("MIT"));
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
        // TODO: when plan changes, update destination and ETAs
        if (getView() != null) {
            // get view to update with getView().findViewById

            mSqlHelper.getPlanDetail((MainActivity) getActivity(), mSqlHelper.PLACE_ADDRESS); //returns address
            // ACTUALLY USE mSqlHelper.getPlanAddressNoPipe((MainActivity) getActivity()) - ask Kristin about this
            // TODO: update UI with all details
        }
    }

    public void onDestinationChanged(String destName, String destAddress) {
        // TODO: update UI and such
    }

    public void onHomeChanged(String homeAddress) {
        //TODO: react to change in shared prefs (ask Amanda) SharedPreferences listener?
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
