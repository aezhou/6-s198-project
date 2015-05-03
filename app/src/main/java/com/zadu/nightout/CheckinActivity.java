package com.zadu.nightout;

import com.zadu.nightout.MainActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
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
//        int misses = mSqlHelper.getPingMisses();
//        Log.i("check", getCallingActivity());
    }

    public String getPlanName() {
        String planName = getIntent().getExtras().getString("plan");
        if(planName == null) {
            //do nothing
        }
        else {
            //TODO: get plan from DB here and update the misses
            Log.i(TAG, "plane name is: " + planName);
        }

        return planName;
    }

//    public void stopAlarm() {
//        if(alarmManager != null) {
//            alarmManager.cancel(operation);
//        }
//    }
//
//    public void resetAlarm(int interval) {
//        stopAlarm();
//        Calendar now = Calendar.getInstance();
//        int hour = now.get(Calendar.HOUR_OF_DAY);
//        int minute = now.get(Calendar.MINUTE);
//        Log.i(TAG, "current time: " + hour + ":" + minute);
//        setAlarm(interval, true);
//    }
//
//    public void setAlarm(int durationMinute, boolean startFromNow) {
//        /** This intent invokes the activity CheckinActivity, which in turn opens the CheckinAlert window */
//
//        Intent i = new Intent("com.zadu.nightout.checkinactivity");
//        i.putExtra("plan", getPlanName());
//        /** Creating a Pending Intent */
//        operation = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
//        /** Getting a reference to the System Service ALARM_SERVICE */
//        alarmManager = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
//
//        Calendar now = Calendar.getInstance();
//
//        int year = now.get(Calendar.YEAR);
//        int month = now.get(Calendar.MONTH);
//        int day = now.get(Calendar.DAY_OF_MONTH);
//        int hour = now.get(Calendar.HOUR_OF_DAY);
//        int minute = now.get(Calendar.MINUTE);
//
//        /** Creating a calendar object corresponding to the date and time set by the user */
//        GregorianCalendar calendar = new GregorianCalendar(year,month,day, hour, minute);
//
//        /** Converting the date and time in to milliseconds elapsed since epoch */
//        long alarm_time = calendar.getTimeInMillis();
//
//        /** Setting an alarm, which invokes the operation at alarm_time */
//        long duration = 10000; //60000*durationMinute;
//        Log.i(TAG, "set start time to (int milliseconds): " +alarm_time + duration);
//        Log.i(TAG, "set interval to: " + duration);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP  , alarm_time + duration, duration, operation);
//
//        /** Alert is set successfully */
//        Toast.makeText(getBaseContext(), "Alarm is set successfully", Toast.LENGTH_SHORT).show();
//    }
//
//    public void userCheckin() {
//        int interval = mSqlHelper.getPingInterval(getPlanName());
//        mSqlHelper.updatePingMisses(getPlanName(), 0);
//        resetAlarm(interval);
//        Toast.makeText(getBaseContext(), "Successfully checked in!", Toast.LENGTH_SHORT).show();
//    }
}