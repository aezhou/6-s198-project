package com.zadu.nightout;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

public class CheckinActivity extends FragmentActivity {
    final String TAG = "CheckinActivity";
    private MyOpenHelper mSqlHelper;
    private PendingIntent operation;
    private AlarmManager alarmManager;
    private static CheckinAlert sAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSqlHelper = MyOpenHelper.getInstance(getApplicationContext());

        String planName = getPlanName();

        /** Creating an Alert Dialog Window */
        if (sAlert != null) {
             sAlert.dismiss();
        }
        sAlert = new CheckinAlert();
        Bundle args = new Bundle();
        Boolean isLast = false;

        int missAllowance = mSqlHelper.getPingAllowance(planName);
        int misses = mSqlHelper.getPingMisses(planName);
        misses++;
        mSqlHelper.updatePingMisses(planName, misses);
        Toast.makeText(this, "number of misses: " + misses, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "num misses: " + misses);

        if(misses > missAllowance) {
            //TODO: call send message to emergency contacts
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putString("exceeded_misses", planName).apply();
            isLast = true;
        }

        args.putBoolean("isLast", isLast);
        sAlert.setArguments(args);
        /** Opening the Alert Dialog Window. This will be opened when the alarm goes off */
        sAlert.show(getSupportFragmentManager(), "CheckinAlert");
    }

    public String getPlanName() {
        String planName = getIntent().getExtras().getString("plan");
        if(planName != null) {
            Log.i(TAG, "plane name is: " + planName);
        }

        return planName;
    }

}