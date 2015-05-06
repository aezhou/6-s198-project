package com.zadu.nightout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // For our recurring task, we'll just display a message
        Toast.makeText(context, "Alarm receiver doing stuff!", Toast.LENGTH_SHORT).show();
        //TODO: do stuff here
        /** This intent invokes the activity CheckinActivity, which in turn opens the CheckinAlert window */
        String planName = intent.getExtras().getString("plan");
        Intent checkinIntent = new Intent("com.zadu.nightout.checkinactivity");
        checkinIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        checkinIntent.putExtra("plan", planName);
        context.startActivity(checkinIntent);
    }

}