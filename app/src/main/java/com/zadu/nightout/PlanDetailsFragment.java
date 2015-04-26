package com.zadu.nightout;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.zadu.nightout.PlanDetailsFragment.OnPlanDetailsListener} interface
 * to handle interaction events.
 * Use the {@link PlanDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class PlanDetailsFragment extends Fragment implements TimePickerFragment.OnFragmentInteractionListener, AdapterView.OnItemClickListener, PlanChangedListener{

    private static String TAG = "PlanDetailsFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnPlanDetailsListener mListener;
    private AutoCompleteTextView destinationInput;
    private Button reserveOnlineButton;
    private Button reserveCallButton;
    private ImageView openMapImage;
    private Button sharePlanButton;
    private Button timePickerButton;
    private Button datePickerButton;
//    private Button findButton;
    private CheckBox reservationMadeBox;

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

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_plan_details, container, false);

        destinationInput = (AutoCompleteTextView) v.findViewById(R.id.searchField);

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

        openMapImage = (ImageView) v.findViewById(R.id.planAddressMap);
        openMapImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDirections(view);
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

/*        findButton = (Button) v.findViewById(R.id.findButton);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findLocationInfo(view);
            }
        });*/

        reservationMadeBox = (CheckBox) v.findViewById(R.id.checkReservationCheckBox);
        reservationMadeBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateReservationStatus(reservationMadeBox.isChecked());
            }
        });

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) v.findViewById(R.id.searchField);

        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(v.getContext(), R.layout.list_item_places));
        autoCompView.setOnItemClickListener(this);

        return v;
    }

//    // TODO: Update argument something to reflect plan information
//    public void onSaveButtonPressed(Object something) {
//        if (mListener != null) {
//            mListener.onPlanSaved(something);
//        }
//    }

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

    public void openDirections(Object something) {
        if(mListener != null) {
            mListener.openGoogleMaps(something);
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

/*    public void findLocationInfo(Object something) {
        if(mListener != null) {
            mListener.findLocation(something);
        }
    }*/

    public void updateReservationStatus(boolean isReserved) {
        if(mListener != null) {
            mListener.updateReservationStatus(reservationMadeBox.isChecked());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onPlanChanged() {
        // TODO: update ui with info from database for place info and reservation info
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

        // TODO: Update argument type and name

        // send things in fragment to listener, which MainActivity extends
//        public void onPlanSaved(Object something);
        public void makeOnlineReservation(Object something);
        public void makeCallReservation(Object something);
        public void openGoogleMaps(Object something);
        public void callSharePlan(Object something);
        public void showTimePickerDialog(Object something);
        public void showDatePickerDialog(Object something);
//        public void findLocation(Object something);
        public void updateReservationStatus(boolean isReserved);
    }

    // Places Autocomplete Stuff Starts Here
    // Places interactions based on tutorial at http://examples.javacodegeeks.com/android/android-google-places-autocomplete-api-example/
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyD3xH-kCCFsSPonGRRi7isV-O5ejZWIts8";
    private static JSONArray predictions = new JSONArray();

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String choice = (String) adapterView.getItemAtPosition(position);
        Log.i(TAG, "clicked: " + choice);

        // [name, street, city, state, country]
        // TODO: Use Places API to get street address and phone number, and update those in UI
        String[] splitChoice = choice.split(", ");
        // Remove text input focus and hide the keyboard
        destinationInput.clearFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(destinationInput.getWindowToken(), 0);

        try {
            String placeID = predictions.getJSONObject(((int) id)).getString("place_id");
            Log.i(TAG, "placeID: " + placeID);
            // Probably get rid of this and use CallAPI
/*            Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeID)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess()) {
                            final Place myPlace = places.get(0);
                            Log.i(TAG, "Place found: " + myPlace.getName());
                        }
                        places.release();
                    }
                });*/
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

    }

    public static ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            // TODO: Could change this to determine current country; for now just supports US
            sb.append("&components=country:us");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());

            System.out.println("URL: "+url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // TODO: Clean this up

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            Log.i(TAG, jsonObj.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            predictions = predsJsonArray;

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

}
