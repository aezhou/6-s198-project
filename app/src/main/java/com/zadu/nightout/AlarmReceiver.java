package com.zadu.nightout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        WakeLocker.acquire(context);
        // For our recurring task, we'll just display a message
        Toast.makeText(context, "Alarm receiver doing stuff!", Toast.LENGTH_SHORT).show();
        //TODO: do stuff here
        /** This intent invokes the activity CheckinActivity, which in turn opens the CheckinAlert window */
        String planName = intent.getExtras().getString("plan");

//        Intent mainIntent = new Intent(context, MainActivity.class);
//        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        mainIntent.putExtra("isAlert", true); //boolean is passed to tell main to run the checkin activity
//        mainIntent.putExtra("planName", planName);
//        context.startActivity(mainIntent);

        Intent checkinIntent = new Intent("com.zadu.nightout.checkinactivity");
        checkinIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        checkinIntent.putExtra("plan", planName);
        context.startActivity(checkinIntent);
    }

}