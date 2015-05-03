package com.zadu.nightout;

import com.zadu.nightout.MainActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class CheckinActivity extends FragmentActivity {
    final String TAG = "CheckinActivity";
    private MyOpenHelper mSqlHelper;
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
}