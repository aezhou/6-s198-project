package com.zadu.nightout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.Switch;
import android.widget.Toast;


public class CheckinAlert extends DialogFragment{
    private AlertsFragment.OnAlertsFragmentInteractionListener mListener;
    private MyOpenHelper mSqlHelper;
    final String TAG = "CheckinAlert";



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        //TODO: following line causes an error
        mSqlHelper = MyOpenHelper.getInstance(getActivity());
////        mSqlHelper = ((MainActivity) getActivity()).getSqlHelper();
//        try {
//            mListener = (AlertsFragment.OnAlertsFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /** The application should be exit, if the user presses the back button */
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().finish();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (manager.findFragmentByTag(tag) == null) {
            super.show(manager, tag);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /** Turn Screen On and Unlock the keypad when this alert dialog is displayed */
        getActivity().getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON | LayoutParams.FLAG_DISMISS_KEYGUARD);

        /** Creating a alert dialog builder */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        /** Setting title for the alert dialog */
        builder.setTitle("Check-in Alert");
        /** Setting the content for the alert dialog */
        builder.setMessage("It's time to check in for your " + ((CheckinActivity)getActivity()).getPlanName() + " plan");
        /** Defining button event listeners */
        builder.setPositiveButton("Check In", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /** Exit application on click OK */
//                getActivity().finish();
                //TODO: do things when user properly checks in
                Toast.makeText(getActivity(), "User checked it!", Toast.LENGTH_SHORT).show();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                preferences.edit().putString("checkin_change", "true").apply();
                Log.i(TAG, "sharedprefs changed");
            }
        });

        builder.setNegativeButton("Turn Off", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                getActivity().finish();
                //TODO: turn checkin toggle off
                Toast.makeText(getActivity(), "User turned off check-ins!", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "User turned off check-ins");
                String planName = ((CheckinActivity)getActivity()).getPlanName();
                mSqlHelper.updatePingsOnOff(planName, false);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                preferences.edit().putString("pings_onoff_change", "true").apply();
                Log.i(TAG, "shared prefs changed");
            }
        });

        /** Creating the alert dialog window */
        return builder.create();
    }
}