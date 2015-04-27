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
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private Integer interval;

    public MyThread( Integer interval){
//        this.year = year;
//        this.month = month;
//        this.day = day;
//        this.hour = hour;
//        this.minute = minute;
        this.interval = interval;
    }

    public void run() {
        if(this.interval == null) {
            Log.i("MyThread", "interval is null");
            return;
        }
//        int year = mSqlHelper.getReservationInfo(MainActivity.this, "RESERVATION_YEAR");
//        int month = mSqlHelper.getReservationInfo(MainActivity.this, "RESERVATION_MONTH");
//        int day = mSqlHelper.getReservationInfo(MainActivity.this, "RESERVATION_DATE");
//        int hour = mSqlHelper.getReservationInfo(MainActivity.this, "RESERVATION_HOUR");
//        int min = mSqlHelper.getReservationInfo(MainActivity.this, "RESERVATION_MINUTE");
//        int intervalTime = mSqlHelper.getReservationInfo(MainActivity.this, "PING_INTERVAL");

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = dateFormatter.parse("2015-04-27 05:01:00"); //sample date and time
//            date = dateFormatter.parse(this.year+ "-" + this.month + "-" + this.day + " " + this.hour + ":" + this.minute);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Now create the time and schedule it
        Timer timer = new Timer();

        int period = 60000 * this.interval;//60secs * interval(in minutes)
//        int period = 10000; //sample time period
        timer.schedule(new MyTimeTask(), date, period);
    };
}

