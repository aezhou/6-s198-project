package com.zadu.nightout;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import org.json.JSONException;


public class PlanDetailsFragment extends Fragment implements AdapterView.OnItemClickListener, PlanChangedListener{

    private static String TAG = "PlanDetailsFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnPlanDetailsListener mListener;
    private AutoCompleteTextView autoCompView;
    private Button reserveOnlineButton;
    private Button reserveCallButton;
    private ImageView openMapImage;
    private Button sharePlanButton;
    private Button timePickerButton;
    private Button datePickerButton;
//    private Button findButton;
    private CheckBox reservationMadeBox;
    private MyOpenHelper mSqlHelper;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlanDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlanDetailsFragment newInstance(String param1, String param2) {
        PlanDetailsFragment fragment = new PlanDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PlanDetailsFragment() {
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

        // Hide the keyboard until the user clicks the text input
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_plan_details, container, false);

        reserveOnlineButton = (Button) v.findViewById(R.id.reservationOnlineButton);
        reserveOnlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReserveOnlineButtonPressed(view);
            }
        });

        reserveCallButton = (Button) v.findViewById(R.id.reservationCallButton);
        reserveCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReserveCallButtonPressed(view);
            }
        });

        sharePlanButton = (Button) v.findViewById(R.id.planShareButton);
        sharePlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharePlan(view);
            }
        });

        timePickerButton = (Button) v.findViewById(R.id.timePickerButton);
        timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(view);
            }
        });

        datePickerButton = (Button) v.findViewById(R.id.datePickerButton);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(view);
            }
        });

        reservationMadeBox = (CheckBox) v.findViewById(R.id.checkReservationCheckBox);
        reservationMadeBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateReservationStatus(reservationMadeBox.isChecked());
            }
        });

        autoCompView = (AutoCompleteTextView) v.findViewById(R.id.searchField);
        autoCompView.clearFocus();
        autoCompView.setAdapter(GooglePlacesAutocompleteAdapter.getInstance(getActivity()));
        autoCompView.setOnItemClickListener(this);

        refreshDetailFragmentView(v);
        refreshDetailFragmentView(v);
        return v;
    }

    // TODO: Update argument something to reflect plan information

    public void onReserveOnlineButtonPressed(Object something) {
        if(mListener != null) {
            mListener.makeOnlineReservation(something);
        }
    }

    public void onReserveCallButtonPressed(Object something) {
        if(mListener != null) {
            mListener.makeCallReservation(something);
        }
    }

    public void sharePlan(Object something) {
        if(mListener != null) {
            mListener.callSharePlan(something);
        }
    }

    public void showTimePicker(Object something) {
        if(mListener != null) {
            Log.i(TAG, "called showTimePicker()");
            mListener.showTimePickerDialog(something);
        }
    }

    public void showDatePicker(Object something) {
        if(mListener != null) {
            Log.i(TAG, "called showDatePicker()");
            mListener.showDatePickerDialog(something);
        }
    }

    public void updateReservationStatus(boolean isReserved) {
        if(mListener != null) {
            mListener.updateReservationStatus(isReserved);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSqlHelper = ((MainActivity) getActivity()).getSqlHelper();
        try {
            mListener = (OnPlanDetailsListener) activity;
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
        // TODO: update ui with info from database for place info and reservation info
        if(getView() != null) {
            refreshDetailFragmentView(getView());
        }
    }

    public void refreshDetailFragmentView(View v) {
        Log.i(TAG, "calling refreshDetailFragmentView");
        if(v != null) {
            TextView placeNameText = (TextView)v.findViewById(R.id.destinationName);
            // FIXME: null pointer crash can happen here after updating settings
            if(mSqlHelper.getPlanDetail((MainActivity)getActivity(), "PLACE_NAME") != null) {
                String placeName = mSqlHelper.getPlanDetail((MainActivity)getActivity(), "PLACE_NAME");
                placeNameText.setText(placeName);
            }
            else {
                placeNameText.setText("");
            }

            TextView placeStreetView = (TextView)v.findViewById(R.id.planAddressText);
            TextView placeCityStateZipView = (TextView)v.findViewById(R.id.destinationCityStateZip);
            if(mSqlHelper.getPlanDetail((MainActivity)getActivity(), "PLACE_ADDRESS") != null) {
                String placeAddress = mSqlHelper.getPlanDetail((MainActivity)getActivity(), "PLACE_ADDRESS");
                String [] placeAddressParts = placeAddress.split("\\|");
                String placeStreetAddress = placeAddressParts[0];
                String placeCityStateZip = placeAddressParts[1];

                placeStreetView.setText(placeStreetAddress);
                placeCityStateZipView.setText(placeCityStateZip);
            }
            else {
                placeStreetView.setText("");
                placeCityStateZipView.setText("");
            }

            TextView placePhoneNumber = (TextView) v.findViewById(R.id.destinationNumber);
            if(mSqlHelper.getPlanDetail((MainActivity)getActivity(), "PLACE_NUMBER") != null) {
                String placeNumber = mSqlHelper.getPlanDetail((MainActivity) getActivity(), "PLACE_NUMBER");
                placePhoneNumber.setText(placeNumber);
            }
            else {
                placePhoneNumber.setText("");
            }

            Button dateButton = (Button)v.findViewById(R.id.datePickerButton);
            if(mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_YEAR") != null && mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_MONTH") != null &&
                    mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_DATE") != null) {
                int planYear = mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_YEAR");
                int planMonth = mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_MONTH") + 1;
                int planDay = mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_DATE");
                dateButton.setText(planDay + "/" + planMonth + "/" + planYear);
            }
            else {
                dateButton.setText("Select a date");
            }

            Button timeButton = (Button)v.findViewById(R.id.timePickerButton);
            Button reservePhoneButton = (Button)v.findViewById(R.id.reservationCallButton);
            Button reserveOnlineButton = (Button)v.findViewById(R.id.reservationOnlineButton);
            if(mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_HOUR") != null && mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_MINUTE") != null) {
                int planHour = mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_HOUR");
                int planMin = mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_MINUTE");
                timeButton.setText(planHour + ":" + planMin);
            }
            else {
                timeButton.setText("Select a time");
            }
            String currentUrl = mSqlHelper.getPlanDetail((MainActivity)getActivity(), "PLACE_URL");
            if( currentUrl == null || currentUrl.equals("")) {
                reserveOnlineButton.setEnabled(false);
                ((MainActivity) getActivity()).findOpenTableUrl(null);
            }
            else {
                //Place url is not null therefore make sure to set visibility to true
                Log.i(TAG, "Thinks the URL is not null/empty");
                reserveOnlineButton.setEnabled(true);
            }

            if(mSqlHelper.getPlanDetail((MainActivity)getActivity(), "PLACE_NUMBER") == null) {
                Log.i(TAG, "phoneButton is disabled");
                reservePhoneButton.setEnabled(false);
            }
            else {
                Log.i(TAG, "phoneButton is enabled");
                reservePhoneButton.setEnabled(true);
            }

            //handling when to enable/disable share button
            Button shareButton = (Button) v.findViewById(R.id.planShareButton);
            if(mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_DATE") == null || mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_HOUR") == null ||
            mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_MINUTE") == null || mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_YEAR") == null  ||
            mSqlHelper.getReservationInfo((MainActivity)getActivity(), "RESERVATION_MONTH") == null || mSqlHelper.getPlanDetail((MainActivity)getActivity(), "PLAN_NAME") == null) {

                shareButton.setEnabled(false);
            }
            else {
                shareButton.setEnabled(true);
            }
        }
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
    public interface OnPlanDetailsListener {
        // send things in fragment to listener, which MainActivity extends
        public void makeOnlineReservation(Object something);
        public void makeCallReservation(Object something);
        public void openGoogleMaps(Object something);
        public void callSharePlan(Object something);
        public void showTimePickerDialog(Object something);
        public void showDatePickerDialog(Object something);
        public void updateReservationStatus(boolean isReserved);
    }

    private String name = "";
    private String streetAddress = "";
    private String city = "";
    private String state = "";
    private String zipCode = "";
    private String country = "";
    private String phoneNumber = "Phone # Unknown";
    private String placeID = null;
    private String lat = null;
    private String lng = null;

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        resetInternalDestFields();
        String choice = (String) adapterView.getItemAtPosition(position);
        // [name, street, city, state, country]; may not have all elements, start from right
        String[] splitChoice = choice.split(", ");
        switch (splitChoice.length) {
            case 1:
                country = splitChoice[0];
                break;
            case 2:
                country = splitChoice[1];
                state = splitChoice[0];
                break;
            case 3:
                country = splitChoice[2];
                state = splitChoice[1];
                city = splitChoice[0];
                break;
            case 4:
                country = splitChoice[3];
                state = splitChoice[2];
                city = splitChoice[1];
                streetAddress = splitChoice[0];
                break;
            case 5:
                country = splitChoice[4];
                state = splitChoice[3];
                city = splitChoice[2];
                streetAddress = splitChoice[1];
                name = splitChoice[0];
                break;
        }

        // Remove text input focus and hide the keyboard
        autoCompView.clearFocus();
//        TODO: CRISTHIAN
        autoCompView.setText("");
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autoCompView.getWindowToken(), 0);

        try {
            placeID = GooglePlacesAutocompleteAdapter.getPredictions().getJSONObject(((int) id)).getString("place_id");
            GoogleApiClient googleAPIClient = new GoogleApiClient.Builder(this.getActivity())
                    .addConnectionCallbacks((MainActivity) this.getActivity())
                    .addOnConnectionFailedListener((MainActivity) this.getActivity())
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
            Places.GeoDataApi.getPlaceById(googleAPIClient, placeID)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess()) {
                            final Place myPlace = places.get(0);
                            Log.i(TAG, "Place Name: " + myPlace.getName().toString());
                            if (myPlace.getName().toString().length() > 0) {
                                name = myPlace.getName().toString();
                            }
                            Log.i(TAG, myPlace.getAddress().toString());
                            String[] addressSplit = myPlace.getAddress().toString().split(", ");
                            if (addressSplit.length == 4) {
                                // Overwrite the address info from before - this is more reliable
                                streetAddress = addressSplit[0];
                                Log.i(TAG, "street address: " + streetAddress);
                                city = addressSplit[1];
                                state = addressSplit[2].split(" ")[0];
                                zipCode = addressSplit[2].split(" ")[1];
                                country = addressSplit[3];
                            }
                            if (myPlace.getPhoneNumber().toString().length() > 0) {
                                phoneNumber = myPlace.getPhoneNumber().toString();
                            }
                            try {
                                zipCode = addressSplit[addressSplit.length - 2].split(" ")[1];
                                Log.i(TAG, "zip code: " + zipCode);

                            } catch (ArrayIndexOutOfBoundsException e) {
                                // TODO: Maybe add a toast to tell them they clicked a dumb option
                                Log.e(TAG, "Can't find zip code.", e);
                            }
                            lat = String.valueOf(myPlace.getLatLng().latitude);
                            lng = String.valueOf(myPlace.getLatLng().longitude);
                        }
                        places.release();
                        mSqlHelper.updatePlanPlaceInfo((MainActivity)getActivity(), "PLACE_NAME", name);

                        mSqlHelper.updatePlanPlaceInfo((MainActivity)getActivity(), "PLACE_ADDRESS", streetAddress + "|" + formatCityStateZip(city, state, zipCode));
                        mSqlHelper.updatePlanPlaceInfo((MainActivity)getActivity(), "PLACE_NUMBER", phoneNumber);
                        // TODO: Do we need to worry about stale values here, in case the Places call failed or something?
                        mSqlHelper.updatePlanPlaceInfo((MainActivity)getActivity(), "PLACE_ID", placeID);
                        mSqlHelper.updatePlanPlaceInfo((MainActivity)getActivity(), "PLACE_LAT", lat);
                        mSqlHelper.updatePlanPlaceInfo((MainActivity)getActivity(), "PLACE_LONG", lng);

                        //TODO: Cristhian's work here
                        ((MainActivity) getActivity()).findOpenTableUrl("");

                        TextView placeName = (TextView)getActivity().findViewById(R.id.destinationName);
                        TextView placeAddress = (TextView)getActivity().findViewById(R.id.planAddressText);
                        TextView placeCityStateZip = (TextView)getActivity().findViewById(R.id.destinationCityStateZip);
                        TextView placeNumber = (TextView)getActivity().findViewById(R.id.destinationNumber);

                        placeName.setText(name);
                        placeAddress.setText(streetAddress);
                        placeCityStateZip.setText(formatCityStateZip(city, state, zipCode));
                        placeNumber.setText(phoneNumber);
                        refreshDetailFragmentView(getView());

                        // Find the directions fragment and notify it of the changes
                        Log.i(TAG, "changed destination");
                        ((MainActivity) getActivity()).getSectionsPagerAdapter().getDirectionsFrag().onDestinationChanged();
                    }
                });
            ((MainActivity) getActivity()).notifyDirFragOfDestChange();
            googleAPIClient.connect();
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

    }

    private void resetInternalDestFields() {
        name = "";
        streetAddress = "";
        city = "";
        state = "";
        zipCode = "";
        country = "";
        phoneNumber = "Phone # Unknown";
        placeID = null;
        lat = null;
        lng = null;
    }

    private String formatCityStateZip(String city, String state, String zip) {
        String formattedAddress = "";
        if (!city.equals("")) {
            formattedAddress = city + ", ";
        }
        if (!state.equals("")) {
            formattedAddress = formattedAddress + state + ", ";
        }
        if (!zip.equals("")) {
            formattedAddress = formattedAddress + zip;
        }
        if (formattedAddress.endsWith(", ")) {
            formattedAddress = formattedAddress.substring(0, formattedAddress.length()-2);
        }
        return formattedAddress;
    }

}
