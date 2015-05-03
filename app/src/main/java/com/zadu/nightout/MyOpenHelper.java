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

    private static MyOpenHelper sInstance;

    private static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "nightOut";
    public static final String DEFAULT_CONTACTS_TABLE_NAME = "contacts";
    public static final String CONTACT_NAME = "contact_name";
    public static final String CONTACT_NUMBER = "contact_number";
    private static final String DEFAULT_CONTACTS_TABLE_CREATE =
            "CREATE TABLE " + DEFAULT_CONTACTS_TABLE_NAME + " (" +
                    "_id" + " INTEGER PRIMARY KEY, " +
                    CONTACT_NAME + " TEXT, "+
                    CONTACT_NUMBER + " TEXT UNIQUE);";

    public static final String PLAN_TABLE_NAME = "plans";
    public static final String PLAN_NAME = "plan_name";
    public static final String PLACE_NAME = "place_name";
    public static final String PLACE_ADDRESS = "place_address";
    public static final String PLACE_NUMBER = "place_number";
    public static final String PLACE_URL = "place_url";
    public static final String PLACE_ID = "place_id";
    public static final String PLACE_LAT = "place_lat";
    public static final String PLACE_LONG = "place_long";
    public static final String RESERVATION_YEAR = "reservation_year";
    public static final String RESERVATION_MONTH = "reservation_month";
    public static final String RESERVATION_DATE = "reservation_date";
    public static final String RESERVATION_HOUR = "reservation_hour";
    public static final String RESERVATION_MINUTE = "reservation_minute";
    public static final String HAS_RESERVATION = "has_reservation";
    public static final String PINGS_ON = "pings_on";
    public static final String PING_MISSES = "ping_misses";
    public static final String PING_INTERVAL = "ping_interval";
    public static final String PING_ALLOWANCE = "ping_allowance";
    private static final String PLAN_TABLE_CREATE =
            "CREATE TABLE " + PLAN_TABLE_NAME + " (" +
                    "_id" + " INTEGER PRIMARY KEY, " +
                    PLAN_NAME + " TEXT UNIQUE, "+
                    PLACE_NAME + " TEXT,"+
                    PLACE_ADDRESS + " TEXT, "+
                    PLACE_NUMBER + " TEXT, "+
                    PLACE_URL + " TEXT, "+
                    PLACE_ID + " TEXT, "+
                    PLACE_LAT + " REAL, "+
                    PLACE_LONG + " REAL, "+
                    RESERVATION_YEAR + " INTEGER, "+
                    RESERVATION_MONTH + " INTEGER, "+
                    RESERVATION_DATE + " INTEGER, "+
                    RESERVATION_HOUR + " INTEGER, "+
                    RESERVATION_MINUTE + " INTEGER, "+
                    HAS_RESERVATION + " INTEGER, "+//one or zero
                    PINGS_ON + " INTEGER, "+//one or zero
                    PING_MISSES + " INTEGER, "+
                    PING_INTERVAL + " INTEGER, "+
                    PING_ALLOWANCE + " INTEGER"+ ");";

    public static final String PLAN_CONTACTS_TABLE_NAME = "plan_contacts";
    public static final String IS_ON = "is_on";
    public static final String IS_DEFAULT = "is_default";
    private static final String PLAN_CONTACTS_TABLE_CREATE =
            "CREATE TABLE " + PLAN_CONTACTS_TABLE_NAME + " (" +
                    "_id" + " INTEGER PRIMARY KEY, " +
                    PLAN_NAME + " TEXT, "+
                    CONTACT_NAME + " TEXT, "+
                    CONTACT_NUMBER + " TEXT, "+
                    IS_DEFAULT + " INTEGER, "+ //one or zero
                    IS_ON + " INTEGER, "+ //one or zero
                    "FOREIGN KEY ("+PLAN_NAME+") REFERENCES "+PLAN_TABLE_NAME+" ("+PLAN_NAME+"), "+
                    "CONSTRAINT unq UNIQUE ("+PLAN_NAME+", "+CONTACT_NUMBER+")"+");";


    private MyOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized MyOpenHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new MyOpenHelper(context.getApplicationContext());
        }
        return sInstance;
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
        return getReadableDatabase().rawQuery("select *"+
                " from "+DEFAULT_CONTACTS_TABLE_NAME, null);
    }

    public Cursor getContactsToDisplay(MainActivity activity) {
        String planName = activity.getCurrentPlanName();
        return getReadableDatabase().rawQuery("SELECT * FROM "+PLAN_CONTACTS_TABLE_NAME+
            " WHERE "+PLAN_NAME+" == '"+planName+"' ORDER BY "+IS_DEFAULT+" DESC", null);
    }

    public void insertDefaultContact(String name, String number) {
        ContentValues cv = new ContentValues();
        cv.put(CONTACT_NAME, name);
        cv.put(CONTACT_NUMBER, number);
        getWritableDatabase().insert(DEFAULT_CONTACTS_TABLE_NAME, null, cv);

        ArrayList<String> planNames = getPlans();
        for (String planName : planNames) {
            addDefaultContactToPlan(planName, name, number, 0);
        }
    }


    public void deleteDefaultContact(String number) {
        getWritableDatabase().delete(DEFAULT_CONTACTS_TABLE_NAME, CONTACT_NUMBER+" == ?", new String[] {number});
        getWritableDatabase().delete(PLAN_CONTACTS_TABLE_NAME, CONTACT_NUMBER+" == ?", new String[] {number});
    }

    public Cursor getPlanInfo(MainActivity activity) {
        String planName = activity.getCurrentPlanName();
        return getReadableDatabase().rawQuery("select * from " + PLAN_TABLE_NAME +
                " where " + PLAN_NAME + " == '" + planName + "'", null);
    }

    public String getPlanDetail(MainActivity activity, String param) {
        String planName = activity.getCurrentPlanName();
        Cursor c = getReadableDatabase().rawQuery("select "+param+" from " + PLAN_TABLE_NAME +
                " where " + PLAN_NAME + " == '" + planName + "'", null);
        c.moveToFirst();
        if (c.isNull(0)) return null;
        String info = c.getString(0);
        c.close();
        return info;
    }

    public String getPlanAddressNoPipe(MainActivity activity) {
        String planName = activity.getCurrentPlanName();
        Cursor c = getReadableDatabase().rawQuery("select "+PLACE_ADDRESS+" from " + PLAN_TABLE_NAME +
                " where " + PLAN_NAME + " == '" + planName + "'", null);
        c.moveToFirst();
        if (c.isNull(0)) return null;
        String info = c.getString(0);
        c.close();
        int index = info.indexOf("|"); // index of |
        if (index == -1) return info;
        return info.substring(0, index) + ", " + info.substring(index+1);
    }

    public Double getPlanLatLong(MainActivity activity, String param) {
        String planName = activity.getCurrentPlanName();
        Cursor c = getReadableDatabase().rawQuery("select "+param+" from " + PLAN_TABLE_NAME +
                " where " + PLAN_NAME + " == '" + planName + "'", null);
        c.moveToFirst();
        if (c.isNull(0)) return null;
        Double info = c.getDouble(0);
        c.close();
        return info;
    }

    public Boolean hasReservation(MainActivity activity) {
        String planName = activity.getCurrentPlanName();
        Cursor c = getReadableDatabase().rawQuery("select "+HAS_RESERVATION+" from " + PLAN_TABLE_NAME +
                " where " + PLAN_NAME + " == '" + planName + "'", null);
        c.moveToFirst();
        if (c.isNull(0)) return null;
        int reserved = c.getInt(0);
        c.close();
        if (reserved==0) {
            return false;
        } else {
            return true;
        }
    }

    public Integer getReservationInfo(MainActivity activity, String param) {
        String planName = activity.getCurrentPlanName();
        Cursor c = getReadableDatabase().rawQuery("select "+param+" from " + PLAN_TABLE_NAME +
                " where " + PLAN_NAME + " == '" + planName + "'", null);
        c.moveToFirst();
        if (c.isNull(0)) return null;
        int info = c.getInt(0);
        c.close();
        return info;
    }

    public Boolean arePingsOn(MainActivity activity) {
        String planName = activity.getCurrentPlanName();
        Cursor c = getReadableDatabase().rawQuery("select "+PINGS_ON+" from " + PLAN_TABLE_NAME +
                " where " + PLAN_NAME + " == '" + planName + "'", null);
        c.moveToFirst();
        if (c.isNull(0)) return null;
        int ping = c.getInt(0);
        c.close();
        if (ping==0) {
            return false;
        } else {
            return true;
        }
    }

    public Integer getPingMisses(MainActivity activity) {
        String planName = activity.getCurrentPlanName();
        return getPingMisses(planName);
    }

    public Integer getPingMisses(String planName) {
        Cursor c = getReadableDatabase().rawQuery("select "+PING_MISSES+" from " + PLAN_TABLE_NAME +
                " where " + PLAN_NAME + " == '" + planName + "'", null);
        c.moveToFirst();
        if (c.isNull(0)) return null;
        int interval = c.getInt(0);
        c.close();
        return interval;
    }


    public Integer getPingInterval(MainActivity activity) {
        String planName = activity.getCurrentPlanName();
        return getPingInterval(planName);
    }

    public Integer getPingInterval(String planName) {
        Cursor c = getReadableDatabase().rawQuery("select " + PING_INTERVAL+" from " + PLAN_TABLE_NAME + " where " + PLAN_NAME + " == '" + planName + "'", null);
        c.moveToFirst();
        if(c.isNull(0)) return null;
        int interval = c.getInt(0);
        c.close();
        return interval;
    }

    public Integer getPingAllowance(MainActivity activity) {
        String planName = activity.getCurrentPlanName();
        return getPingAllowance(planName);
    }

    public Integer getPingAllowance(String planName) {
        Cursor c = getReadableDatabase().rawQuery("select " +PING_ALLOWANCE+" from " + PLAN_TABLE_NAME + " where " + PLAN_NAME + " == '" + planName + "'", null);
        c.moveToFirst();
        if(c.isNull(0))return null;
        int allowance = c.getInt(0);
        c.close();
        return  allowance;
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
        String planName = activity.getCurrentPlanName();
        updatePingsOnOff(planName, on);
    }

    public void updatePingsOnOff(String planName, boolean on) {
        int onInt = 0;
        if (on) {onInt = 1;}
        ContentValues cv = new ContentValues();
        cv.put(PINGS_ON, onInt);
        getWritableDatabase().update(PLAN_TABLE_NAME, cv, PLAN_NAME+" == ?",
                new String[] {planName});
    }

    public void updatePingInterval(MainActivity activity, int interval) {
        ContentValues cv = new ContentValues();
        cv.put(PING_INTERVAL, interval);
        getWritableDatabase().update(PLAN_TABLE_NAME, cv, PLAN_NAME+" == ?",
                new String[] {activity.getCurrentPlanName()});
    }

    public void updatePingMisses(MainActivity activity, int misses) {
        String planName = activity.getCurrentPlanName();
        updatePingMisses(planName, misses);
    }

    public void updatePingMisses(String planName, int misses) {
        ContentValues cv = new ContentValues();
        cv.put(PING_MISSES, misses);
        getWritableDatabase().update(PLAN_TABLE_NAME, cv, PLAN_NAME+" == ?",
                new String[] {planName});
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
        cv.put(HAS_RESERVATION, 0);
        cv.put(PINGS_ON, 0);
        cv.put(PING_MISSES, 0);
        cv.put(PING_INTERVAL, 30);
        cv.put(PING_ALLOWANCE, 2);
        getWritableDatabase().insert(PLAN_TABLE_NAME, null, cv);
        setPlanDefaultContactNumbers(planName);
    }

    public void deletePlan(String planName) {
        getWritableDatabase().delete(PLAN_TABLE_NAME, PLAN_NAME+" == ?", new String[] {planName});
    }

    public ArrayList<String> getContactNumbers(MainActivity activity) {
        String planName = activity.getCurrentPlanName();
        return getContactNumbers(planName);
    }

    public ArrayList<String> getContactNumbers(String planName) {
        ArrayList<String> nums = new ArrayList<String>();
        Cursor c =  getReadableDatabase().rawQuery("select " + CONTACT_NUMBER + " from " + PLAN_CONTACTS_TABLE_NAME +
                " where " + PLAN_NAME + " == '" + planName + "' AND "+
                IS_ON+" == 1", null);
        boolean hasNum = c.moveToFirst();
        while (c.isAfterLast() == false) {
            nums.add(c.getString(0));
            c.moveToNext();
        }
        c.close();
        return nums;
    }

    public void setPlanDefaultContactNumbers(String planName) {
        ArrayList<String> nums = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();
        Cursor defaultContacts = getDefaultContacts();
        defaultContacts.moveToFirst();
        while (defaultContacts.isAfterLast() == false) {
            names.add(defaultContacts.getString(1));
            nums.add(defaultContacts.getString(2));
            defaultContacts.moveToNext();
        }
        defaultContacts.close();

        for (int i=0; i<names.size(); i++) {
            addDefaultContactToPlan(planName, names.get(i), nums.get(i), 1);
        }
    }

    public void addDefaultContactToPlan(String planName, String contactName, String contactNumber, int on) {
        ContentValues cv = new ContentValues();
        cv.put(PLAN_NAME, planName);
        cv.put(CONTACT_NAME, contactName);
        cv.put(CONTACT_NUMBER, contactNumber);
        cv.put(IS_DEFAULT, 1);
        cv.put(IS_ON, on);
        getWritableDatabase().insert(PLAN_CONTACTS_TABLE_NAME, null, cv);
    }

    public void addPlanContactNumber(MainActivity activity, String name, String number) {
        ContentValues cv = new ContentValues();
        cv.put(PLAN_NAME, activity.getCurrentPlanName());
        cv.put(CONTACT_NAME, name);
        cv.put(CONTACT_NUMBER, number);
        cv.put(IS_DEFAULT, 0);
        cv.put(IS_ON, 1);
        getWritableDatabase().insert(PLAN_CONTACTS_TABLE_NAME, null, cv);
    }

    public void checkPlanContactNumber(MainActivity activity, String number, boolean on) {
        ContentValues cv = new ContentValues();
        int isOn = 0;
        if (on) isOn = 1;
        cv.put(IS_ON, isOn);
        getWritableDatabase().update(PLAN_CONTACTS_TABLE_NAME, cv, PLAN_NAME+" == ? AND "+
                CONTACT_NUMBER+" == ?", new String[] {activity.getCurrentPlanName(), number});
    }

    public int getNumCheckedContacts(MainActivity activity) {
        String planName = activity.getCurrentPlanName();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM "+PLAN_CONTACTS_TABLE_NAME+
                " WHERE "+PLAN_NAME+" == '"+planName+"' AND "+IS_ON+" == 1", null);
        c.moveToFirst();
        int num = c.getCount();
        c.close();
        return num;
    }

    public int getNumDefaultContacts() {
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM "+DEFAULT_CONTACTS_TABLE_NAME, null);
        c.moveToFirst();
        int num = c.getCount();
        c.close();
        return num;
    }

}
