package com.zadu.nightout;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by kasmus on 4/21/15.
 */
public class MyOpenHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "nightOut";
    private static final String DEFAULT_CONTACTS_TABLE_NAME = "contacts";
    private static final String CONTACT_NAME = "contact_name";
    private static final String CONTACT_NUMBER = "contact_number";
    private static final String DEFAULT_CONTACTS_TABLE_CREATE =
            "CREATE TABLE " + DEFAULT_CONTACTS_TABLE_NAME + " (" +
                    "_id" + " INTEGER PRIMARY KEY, " +
                    CONTACT_NAME + " TEXT, "+
                    CONTACT_NUMBER + " TEXT UNIQUE);";

    private static final String PLAN_TABLE_NAME = "plans";
    private static final String PLAN_NAME = "plan_name";
    private static final String PLACE_NAME = "place_name";
    private static final String PLACE_ADDRESS = "place_address";
    private static final String PLACE_NUMBER = "place_number";
    private static final String PLACE_URL = "place_url";
    private static final String RESERVATION_YEAR = "reservation_year";
    private static final String RESERVATION_MONTH = "reservation_month";
    private static final String RESERVATION_DATE = "reservation_date";
    private static final String RESERVATION_HOUR = "reservation_hour";
    private static final String RESERVATION_MINUTE = "reservation_minute";
    private static final String HAS_RESERVATION = "has_reservation";
    private static final String PINGS_ON = "pings_on";
    private static final String PING_INTERVAL = "ping_interval";
    private static final String PING_ALLOWANCE = "ping_allowance";
    private static final String PLAN_TABLE_CREATE =
            "CREATE TABLE " + PLAN_TABLE_NAME + " (" +
                    "_id" + " INTEGER PRIMARY KEY, " +
                    PLAN_NAME + " TEXT UNIQUE, "+
                    PLACE_NAME + " TEXT,"+
                    PLACE_ADDRESS + " TEXT, "+
                    PLACE_NUMBER + " TEXT, "+
                    PLACE_URL + " TEXT, "+
                    RESERVATION_YEAR + " INTEGER, "+
                    RESERVATION_MONTH + " INTEGER, "+
                    RESERVATION_DATE + " INTEGER, "+
                    RESERVATION_HOUR + " INTEGER, "+
                    RESERVATION_MINUTE + " INTEGER, "+
                    HAS_RESERVATION + " INTEGER, "+//one or zero
                    PINGS_ON + " INTEGER, "+//one or zero
                    PING_INTERVAL + " INTEGER, "+
                    PING_ALLOWANCE + " INTEGER"+ ");";

    private static final String PLAN_CONTACTS_TABLE_NAME = "plan_contacts";
    private static final String IS_DEFAULT = "is_default";
    private static final String PLAN_CONTACTS_TABLE_CREATE =
            "CREATE TABLE " + PLAN_CONTACTS_TABLE_NAME + " (" +
                    "_id" + " INTEGER PRIMARY KEY, " +
                    PLAN_NAME + " TEXT, "+
                    CONTACT_NAME + " TEXT, "+
                    CONTACT_NUMBER + " TEXT,"+
                    IS_DEFAULT + " INTEGER,"+ //one or zero
                    " FOREIGN KEY ("+PLAN_NAME+") REFERENCES "+PLAN_TABLE_NAME+" ("+PLAN_NAME+"),"+
                    "CONSTRAINT unq UNIQUE ("+PLAN_NAME+", "+CONTACT_NUMBER+"))"+");";


    MyOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DEFAULT_CONTACTS_TABLE_CREATE);
        db.execSQL(PLAN_TABLE_CREATE);
        db.execSQL(PLAN_CONTACTS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public Cursor getDefaultContacts() {
        return getReadableDatabase().rawQuery("select * from "+DEFAULT_CONTACTS_TABLE_NAME, null);
    }

    public void insertDefaultContact(String name, String number) {
        ContentValues cv = new ContentValues();
        cv.put(CONTACT_NAME, name);
        cv.put(CONTACT_NUMBER, number);
        getWritableDatabase().insert(DEFAULT_CONTACTS_TABLE_NAME, null, cv);
    }

    public void deleteDefaultContact(String number) {
        getWritableDatabase().delete(DEFAULT_CONTACTS_TABLE_NAME, CONTACT_NUMBER+" == ?", new String[] {number});
    }

    public Cursor getPlanInfo(String planName) {
        return getReadableDatabase().rawQuery("select * from " + PLAN_TABLE_NAME +
                " where " + PLAN_NAME + " == " + planName, null);
    }

    public void updatePlanPlaceInfo(MainActivity activity, String infoType, String value) {
        ContentValues cv = new ContentValues();
        cv.put(infoType, value);
        getWritableDatabase().update(PLAN_TABLE_NAME, cv, PLAN_NAME+" == ?",
                new String[] {activity.getCurrentPlanName()});
    }

    public void updatePlanReservationTime(MainActivity activity, int hour, int min) {
        ContentValues cv = new ContentValues();
        cv.put(RESERVATION_HOUR, hour);
        cv.put(RESERVATION_MINUTE, min);
        getWritableDatabase().update(PLAN_TABLE_NAME, cv, PLAN_NAME + " == ?",
                new String[]{activity.getCurrentPlanName()});
    }

    public void updatePlanReservationDate(MainActivity activity, int year, int month, int date) {
        ContentValues cv = new ContentValues();
        cv.put(RESERVATION_YEAR, year);
        cv.put(RESERVATION_MONTH, month);
        cv.put(RESERVATION_DATE, date);
        getWritableDatabase().update(PLAN_TABLE_NAME, cv, PLAN_NAME + " == ?",
                new String[]{activity.getCurrentPlanName()});
    }

    public void updateHasReservation(MainActivity activity, boolean reserved) {
        int reservedInt = 0;
        if (reserved) {reservedInt = 1;}
        ContentValues cv = new ContentValues();
        cv.put(HAS_RESERVATION, reservedInt);
        getWritableDatabase().update(PLAN_TABLE_NAME, cv, PLAN_NAME+" == ?",
                new String[] {activity.getCurrentPlanName()});
    }

    public void updatePingsOnOff(MainActivity activity, boolean on) {
        int onInt = 0;
        if (on) {onInt = 1;}
        ContentValues cv = new ContentValues();
        cv.put(PINGS_ON, onInt);
        getWritableDatabase().update(PLAN_TABLE_NAME, cv, PLAN_NAME+" == ?",
                new String[] {activity.getCurrentPlanName()});
    }

    public void updatePingInterval(MainActivity activity, int interval) {
        ContentValues cv = new ContentValues();
        cv.put(PING_INTERVAL, interval);
        getWritableDatabase().update(PLAN_TABLE_NAME, cv, PLAN_NAME+" == ?",
                new String[] {activity.getCurrentPlanName()});
    }

    public void updatePingAllowance(MainActivity activity, int allow) {
        ContentValues cv = new ContentValues();
        cv.put(PING_ALLOWANCE, allow);
        getWritableDatabase().update(PLAN_TABLE_NAME, cv, PLAN_NAME+" == ?",
                new String[] {activity.getCurrentPlanName()});
    }

    public ArrayList<String> getPlans() {
        Cursor c = getReadableDatabase().rawQuery("select " + PLAN_NAME + " from " + PLAN_TABLE_NAME, null);
        ArrayList<String> plans = new ArrayList<String>();
        boolean hasPlan = c.moveToFirst();
        if (!hasPlan) return null;
        while (c.isAfterLast() == false) {
            plans.add(c.getString(0));
            c.moveToNext();
        }
        c.close();
        return plans;
    }

    public void insertNewPlan(String planName) {
        ContentValues cv = new ContentValues();
        cv.put(PLAN_NAME, planName);
        getWritableDatabase().insert(PLAN_TABLE_NAME, null, cv);
        setPlanDefaultContactNumbers(planName);
    }

    public void deletePlan(String planName) {
        getWritableDatabase().delete(PLAN_TABLE_NAME, PLAN_NAME+" == ?", new String[] {planName});
    }

    public ArrayList<String> getContactNumbers(MainActivity activity) {
        ArrayList<String> nums = new ArrayList<String>();
        Cursor c =  getReadableDatabase().rawQuery("select " + CONTACT_NUMBER + " from " + PLAN_CONTACTS_TABLE_NAME +
                " where " + PLACE_NAME + " == " + activity.getCurrentPlanName(), null);
        boolean hasNum = c.moveToFirst();
        if (!hasNum) return null;
        while (c.isAfterLast() == false) {
            nums.add(c.getString(1));
            c.moveToNext();
        }
        c.close();
        return nums;
    }

    public void setPlanDefaultContactNumbers(String planName) {
        ArrayList<String> nums = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();
        Cursor defaultContacts = getDefaultContacts();
        while (defaultContacts.isAfterLast() == false) {
            names.add(defaultContacts.getString(0));
            nums.add(defaultContacts.getString(1));
            defaultContacts.moveToNext();
        }
        defaultContacts.close();

        for (int i=0; i<names.size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(PLAN_NAME, planName);
            cv.put(CONTACT_NAME, names.get(i));
            cv.put(CONTACT_NUMBER, nums.get(i));
            getWritableDatabase().insert(PLAN_CONTACTS_TABLE_NAME, null, cv);
        }
    }

    public void setPlanContactNumber(MainActivity activity, String name, String number, boolean isDefault) {
        ContentValues cv = new ContentValues();
        cv.put(PLAN_NAME, activity.getCurrentPlanName());
        cv.put(CONTACT_NAME, name);
        cv.put(CONTACT_NUMBER, number);
        int isDefaultInt = 0;
        if (isDefault) {isDefaultInt=1;}
        cv.put(IS_DEFAULT, isDefaultInt);
        getWritableDatabase().insert(PLAN_CONTACTS_TABLE_NAME, null, cv);
    }

    public void removePlanContactNumber(MainActivity activity, String number) {
        getWritableDatabase().delete(PLAN_CONTACTS_TABLE_NAME,
                CONTACT_NUMBER+" == ? AND "+PLAN_NAME+" == ?",
                new String[] {number, activity.getCurrentPlanName()});
    }

    public Cursor getOtherPlanContacts(MainActivity activity) {
        return getReadableDatabase().rawQuery("select " + CONTACT_NAME + ", " + CONTACT_NUMBER +
                " from " + PLAN_CONTACTS_TABLE_NAME +
                " where " + PLACE_NAME + " == " + activity.getCurrentPlanName() +
                " AND " + IS_DEFAULT + " == 0", null);
    }

}
