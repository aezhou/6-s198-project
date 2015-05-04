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
import android.widget.Toast;


public class CheckinAlert extends DialogFragment{
    private AlertsFragment.OnAlertsFragmentInteractionListener mListener;
    private MyOpenHelper mSqlHelper;
    final String TAG = "CheckinAlert";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSqlHelper = MyOpenHelper.getInstance(getActivity());
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
        super.onCreate(savedInstanceState);
        boolean isLast = getArguments().getBoolean("isLast");
        int misses = getArguments().getInt("numMisses");
        /** Turn Screen On and Unlock the keypad when this alert dialog is displayed */
        getActivity().getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON | LayoutParams.FLAG_DISMISS_KEYGUARD);

        /** Creating a alert dialog builder */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        setUpDialog(builder, isLast, misses);
        /** Creating the alert dialog window */
        return builder.create();
    }

    private void setUpDialog(AlertDialog.Builder builder, boolean isFinal, int missed) {
        if(!isFinal) {
            /** Setting title for the alert dialog */
            builder.setTitle("Check-in Alert");
            /** Setting the content for the alert dialog */
            builder.setMessage("It's time to check in for your " + ((CheckinActivity) getActivity()).getPlanName() + " plan. You have missed " + missed + " times.");
            /** Defining button event listeners */
            builder.setPositiveButton("Check In", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    /** Exit application on click OK */
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    preferences.edit().putString("checkin_change", "true").apply();
                    Log.i(TAG, "sharedprefs changed");
                }
            });

            builder.setNegativeButton("Turn Off", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, "User turned off check-ins");
                    String planName = ((CheckinActivity) getActivity()).getPlanName();
                    mSqlHelper.updatePingsOnOff(planName, false);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    preferences.edit().putString("pings_onoff_change", "true").apply();
                    Log.i(TAG, "shared prefs changed");
                }
            });
        }
        else {
            /** Setting title for the alert dialog */
            builder.setTitle("Check-in Alert");
            builder.setMessage("You've exceeded the number of misses for " + ((CheckinActivity) getActivity()).getPlanName() + " plan. A message has been sent to your emergency contacts.");
            builder.setPositiveButton("OK", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    /** Exit application on click OK */
                    String planName = ((CheckinActivity) getActivity()).getPlanName();
                    mSqlHelper.updatePingsOnOff(planName, false);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    preferences.edit().putString("pings_onoff_change", "true").apply();
                }
            });
        }
    }
}