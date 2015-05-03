package com.zadu.nightout;

import com.zadu.nightout.MainActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CheckinActivity extends FragmentActivity {
    final String TAG = "CheckinActivity";
    private MyOpenHelper mSqlHelper;
    private PendingIntent operation;
    private AlarmManager alarmManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSqlHelper = MyOpenHelper.getInstance(getApplicationContext());

        String planName = getPlanName();

        /** Creating an Alert Dialog Window */
        CheckinAlert alert = new CheckinAlert();

        /** Opening the Alert Dialog Window. This will be opened when the alarm goes off */
        alert.show(getSupportFragmentManager(), "CheckinAlert");

        //TODO: Cristhian perform other check-in functions?
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
            Log.i(TAG, "after adjusting prefs");
        }
    }

    public String getPlanName() {
        String planName = getIntent().getExtras().getString("plan");
        if(planName != null) {
            //TODO: get plan from DB here and update the misses
            Log.i(TAG, "plane name is: " + planName);
        }

        return planName;
    }

}