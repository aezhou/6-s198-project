package com.zadu.nightout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        WakeLocker.acquire(context);
        /** This intent invokes the activity CheckinActivity, which in turn opens the CheckinAlert window */
        String planName = intent.getExtras().getString("plan");

        Intent checkinIntent = new Intent("com.zadu.nightout.checkinactivity");
        checkinIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        checkinIntent.putExtra("plan", planName);
        context.startActivity(checkinIntent);
    }

}