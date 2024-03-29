package com.zadu.nightout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
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
        int notifId = getArguments().getInt("notifId");
        /** Turn Screen On and Unlock the keypad when this alert dialog is displayed */
        getActivity().getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON | LayoutParams.FLAG_DISMISS_KEYGUARD);

        /** Creating a alert dialog builder */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        setUpDialog(builder, isLast, misses, notifId);
        /** Creating the alert dialog window */
        return builder.create();
    }

    private void setUpDialog(AlertDialog.Builder builder, boolean isFinal, int missed, final int notificationId) {
        final NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getActivity(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!isFinal) {
            /** Setting title for the alert dialog */
            builder.setTitle("Check-in Alert");
            /** Setting the content for the alert dialog */
            builder.setMessage("It's time to check in for your " + ((CheckinActivity) getActivity()).getPlanName() + " plan. You have missed " + missed + " times.");
            /** Defining button event listeners */
            builder.setPositiveButton("Check In", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mNotificationManager.cancel(notificationId);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    preferences.edit().putString("checkin_change", "true").apply();
                }
            });

            builder.setNegativeButton("Turn Off", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mNotificationManager.cancel(notificationId);
                    String planName = ((CheckinActivity) getActivity()).getPlanName();
                    mSqlHelper.updatePingsOnOff(planName, false);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    preferences.edit().putString("pings_onoff_change", "true").apply();
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
                    mNotificationManager.cancel(notificationId);
                    String planName = ((CheckinActivity) getActivity()).getPlanName();
                    mSqlHelper.updatePingsOnOff(planName, false);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    preferences.edit().putString("pings_onoff_change", "true").apply();
                }
            });
        }

    }
}