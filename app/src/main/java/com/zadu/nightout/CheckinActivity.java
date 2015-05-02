package com.zadu.nightout;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class CheckinActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Creating an Alert Dialog Window */
        CheckinAlert alert = new CheckinAlert();

        /** Opening the Alert Dialog Window. This will be opened when the alarm goes off */
        alert.show(getSupportFragmentManager(), "CheckinAlert");
    }
}