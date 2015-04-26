package com.zadu.nightout;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.zadu.nightout.PlanDetailsFragment.OnPlanDetailsListener} interface
 * to handle interaction events.
 * Use the {@link PlanDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlanDetailsFragment extends Fragment implements TimePickerFragment.OnFragmentInteractionListener{

    String TAG = "PlanDetailsFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnPlanDetailsListener mListener;
//    private Button mSaveButton;
    private Button reserveOnlineButton;
    private Button reserveCallButton;
    private ImageView openMapImage;
    private Button sharePlanButton;
    private Button timePickerButton;
    private Button datePickerButton;
    private Button findButton;
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

        findButton = (Button) v.findViewById(R.id.findButton);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findLocationInfo(view);
            }
        });

        reservationMadeBox = (CheckBox) v.findViewById(R.id.checkReservationCheckBox);
        reservationMadeBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateReservationStatus(reservationMadeBox.isChecked());
            }
        });

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

    public void findLocationInfo(Object something) {
        if(mListener != null) {
            mListener.findLocation(something);
        }
    }

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
        public void findLocation(Object something);
        public void updateReservationStatus(boolean isReserved);
    }

}
