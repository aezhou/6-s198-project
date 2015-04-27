package com.zadu.nightout;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Cristhian on 4/27/2015.
 */
public class MyThread extends Thread {
    private MyOpenHelper mSqlHelper;
//    mSqlHelper = new MyOpenHelper(this);
    private Integer interval;
    private Timer t;
    private MyTimeTask tt;
    public MyThread( Integer interval){
        this.interval = interval;
        this.tt = new MyTimeTask();
        this.t = new Timer();
    }

    public void stopTimerAndTask() {
        this.tt.cancel();
        this.t.cancel();
        this.t.purge();
    }

    public void run() {
        if (this.interval == null) {
            Log.i("MyThread", "interval is null");
            return;
        }

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = dateFormatter.parse("2015-04-27 05:01:00"); //sample date and time
//            date = dateFormatter.parse(this.year+ "-" + this.month + "-" + this.day + " " + this.hour + ":" + this.minute);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Now create the time and schedule it

//        int period = 60000 * this.interval;//60secs * interval(in minutes)
        int period = 5000; //sample time period of 5 sec

        this.t.schedule(this.tt, date, period);
    };
}

