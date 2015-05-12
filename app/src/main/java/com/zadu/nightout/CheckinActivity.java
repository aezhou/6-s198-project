package com.zadu.nightout;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class CheckinActivity extends FragmentActivity {
    final String TAG = "CheckinActivity";
    private MyOpenHelper mSqlHelper;
    private static CheckinAlert sAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String planName = getPlanName();
        mSqlHelper = MyOpenHelper.getInstance(getApplicationContext());
        int misses = 0;
        if(mSqlHelper.getPingMisses(planName) != null) {
            misses = mSqlHelper.getPingMisses(planName);
        }


        /** Creating an Alert Dialog Window */
        if (sAlert != null) {
             sAlert.dismissAllowingStateLoss();
        }
        sAlert = new CheckinAlert();
        Bundle args = new Bundle();
        Boolean isLast = false;

        int missAllowance = mSqlHelper.getPingAllowance(planName);

        misses++;
        mSqlHelper.updatePingMisses(planName, misses);

        if(misses > missAllowance) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putString("exceeded_misses", planName).apply();
            isLast = true;
        }

        args.putBoolean("isLast", isLast);
        args.putInt("numMisses", misses-1);
        sAlert.setArguments(args);
        /** Opening the Alert Dialog Window. This will be opened when the alarm goes off */
        sAlert.show(getSupportFragmentManager(), "CheckinAlert");
    }

    public String getPlanName() {
        String planName = getIntent().getExtras().getString("plan");
        return planName;
    }

}