package com.zadu.nightout;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener,
        PlanDetailsFragment.OnPlanDetailsListener,
        AlertsFragment.OnAlertsFragmentInteractionListener,
        DirectionsFragment.OnDirectionsFragmentInteractionListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private Spinner mSpinner;
    private ArrayAdapter mArrayAdapter;
    private MyOpenHelper mSqlHelper;
    private LocationManager mLocationManager;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    String TAG = "MainActivity";
    String openTableApiUrl = "http://opentable.herokuapp.com/api/";
    public GoogleGeocodingCallApi googleGeocodingCallApi;
    public GoogleDistanceMatrixCallApi googleDistanceMatrixCallApi;

    private Intent i;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String first = preferences.getString("first_time", "true");
        if (!first.equalsIgnoreCase("false")) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mSqlHelper = MyOpenHelper.getInstance(this);

        actionBar.setCustomView(R.layout.custom_actionbar);
        actionBar.setDisplayShowCustomEnabled(true);

        //Set up Google Client API
        buildGoogleApiClient();

        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                planChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        List dropdown = mSqlHelper.getPlans();
        if (dropdown.size() == 0) {
            mSqlHelper.insertNewPlan("My First Plan");
            dropdown = mSqlHelper.getPlans();
        }
        mArrayAdapter = new ArrayAdapter(this, R.layout.custom_textview, dropdown);
//                ArrayAdapter.createFromResource(this,
//                R.array.temporary_array, android.R.layout.simple_spinner_item);
        mArrayAdapter.setDropDownViewResource(R.layout.custom_textview);
        mSpinner.setAdapter(mArrayAdapter);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

    }

    public void planChanged() {
        for (int i=0; i<mSectionsPagerAdapter.getCount(); i++) {
            Fragment f = mSectionsPagerAdapter.getItem(i);
            ((PlanChangedListener) f).onPlanChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // TODO: save current tab, current plan

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // TODO: reselect tab, reselect plan, repopulate tab from plan db
    }

    public SectionsPagerAdapter getSectionsPagerAdapter() {
        return mSectionsPagerAdapter;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, 222);
            return true;
        }

        if (id == R.id.action_delete_plan) {
            showDeletePlanDialog(getCurrentPlanName());
        }

        if (id == R.id.action_new_plan) {
            showNewPlanDialog(true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void makeOnlineReservation(Object something ) {
        Log.i(TAG, "makeOnlineReservation() called");
        String url = mSqlHelper.getPlanDetail(this, "PLACE_URL");
        if(url != null) {
            Uri webpage = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if(intent.resolveActivity(getPackageManager()) != null) {
                Log.i(TAG, "Web browser intent about to be called");
                startActivity(intent);
            }
        }
        else {
            Log.i(TAG, "Whoops! you don't have a URL saved!");
        }

    }

    @Override
    public void makeCallReservation(Object something) {
        Log.i(TAG, "makeCallReservation() called");
        TextView number = (TextView)findViewById(R.id.destinationNumber);
        String phoneNumber = number.getText().toString();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if(intent.resolveActivity(getPackageManager()) != null) {
            Log.i(TAG, "Call intent about to be called"); //Ignore the pun
            startActivity(intent);
        }
    }

    @Override
    public void openGoogleMaps(Object something) {
        Log.i(TAG, "openGoogleMaps() called");
        TextView streetAddressText = (TextView)findViewById(R.id.planAddressText);
        TextView otherAddressText = (TextView)findViewById(R.id.destinationCityStateZip);
        String streetAddress = streetAddressText.getText().toString();
        String otherAddress = otherAddressText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=" + streetAddress + otherAddress));
        if(intent.resolveActivity(getPackageManager()) != null) {
            Log.i(TAG, "Google Maps about to be called");
            startActivity(intent);
        }
    }

    @Override
    public void callSharePlan(Object something) {
        Log.i(TAG, "calling callSharePlan()");
        TextView destinationName = (TextView) findViewById(R.id.destinationName);
        TextView planAddressText = (TextView) findViewById(R.id.planAddressText);
        TextView destinationCityStateZip = (TextView) findViewById(R.id.destinationCityStateZip);
        CheckBox reservationMade = (CheckBox) findViewById(R.id.checkReservationCheckBox);

        String destination = destinationName.getText().toString();
        String address = planAddressText.getText().toString() + ", " + destinationCityStateZip.getText().toString();
        boolean isReserved = reservationMade.isChecked();
        Button dateButton = (Button)findViewById(R.id.datePickerButton);
        String date = dateButton.getText().toString();
        Button timeButton = (Button)findViewById(R.id.timePickerButton);
        String time = timeButton.getText().toString();
        String reservationMessage = "";
        if(isReserved) {
            reservationMessage = "A reservation has already been made.";
        }

        String messageBody = "Hi, I will be going to " + destination + " " + address + " on " + date + " at " + time + ". I hope to see " +
                "you there! " + reservationMessage;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, messageBody);
        startActivity(intent);
    }

    @Override
    public void showTimePickerDialog(Object something) {
        Log.i(TAG, "called showTimePickerDialog()");
        final Calendar c = Calendar.getInstance();
        final int mHour = c.get(Calendar.HOUR_OF_DAY);
        final int mMinute = c.get(Calendar.MINUTE);
        TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Button timePickerButton = (Button)findViewById(R.id.timePickerButton);
                Log.i("MAGICAL TAG", ""+minute);
                String realMinute = "" + minute;
                if(minute < 10) {
                    Log.i(TAG, "minute was less than 10");
                    realMinute = "0" + minute;
                }
                timePickerButton.setText(hourOfDay + " : " + realMinute);
                updatePlanReservationTime(hourOfDay, minute);
                PlanDetailsFragment f = (PlanDetailsFragment) mSectionsPagerAdapter.getItem(0);
                f.onPlanChanged();
            }
        }, mHour, mMinute, false);
        tpd.show();
    }

    @Override
    public void showDatePickerDialog(Object something) {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Button datePickerButton = (Button) findViewById(R.id.datePickerButton);
                        datePickerButton.setText((monthOfYear+1) + "/" + dayOfMonth + "/" +year);
                        updatePlanReservationDate(year, monthOfYear, dayOfMonth);

                        PlanDetailsFragment f = (PlanDetailsFragment) mSectionsPagerAdapter.getItem(0);
                        f.onPlanChanged();
                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

    @Override
    public void OnAlertFragmentInteraction(Object object) {

    }

    @Override
    public void callRide(Object something) {
        // TODO: Make the links also transfer information or be more specific if possible.
        PackageManager pm = getPackageManager();
        try
        {
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
            // Launch App
            Intent launchIntent = pm.getLaunchIntentForPackage("com.ubercab");
            startActivity(launchIntent);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            // No Uber app! Open Mobile Website.
            Log.i(TAG, "No Uber App installed - opening website");
            String uberUrl = "https://www.uber.com/";
            Uri webpage = Uri.parse(uberUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    @Override
    public void openGoogleMapsDirections(Object something) {
        Spinner destSpinner = (Spinner) findViewById(R.id.dest_spinner);
        String selection = destSpinner.getSelectedItem().toString();
        String address = "";
        if (selection.equals("Home")) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            address = preferences.getString("home_address", "");
        }
        else if (selection.equals("Other")) {
            EditText otherAddressInput = (EditText) findViewById(R.id.dest_address_other);
            address = otherAddressInput.getText().toString();
        }
        else {
            TextView destAddressTextView = (TextView) findViewById(R.id.dest_address);
            address = destAddressTextView.getText().toString();
        }
        String destAddressEncoded = null;
        try {
            destAddressEncoded = URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Encoding exception - unable to parse destination address for use");
            e.printStackTrace();
        }
        if (destAddressEncoded != null) {
            Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=" + destAddressEncoded));
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        }
    }

    public void findOpenTableUrl(Object something) {
        Log.i(TAG, "findOpenTableUrl() called");
        String name = mSqlHelper.getPlanDetail(this, "PLACE_NAME");
        String address = mSqlHelper.getPlanDetail(this, "PLACE_ADDRESS");
        Log.i(TAG, "address from DB: " + address);
        if(address != null && !address.equals("")) {
            Log.i(TAG, "search addresss not null");
            String [] addressSplit = address.split("\\|");
            String streetAddress = addressSplit[0];
            String cityStateZipDB = addressSplit[1];

            String [] cityZipSplit = cityStateZipDB.split(",");

            String zipCode;
            String searchText;
            if(cityZipSplit.length > 1) {
                zipCode = cityZipSplit[1].substring(1);
                searchText = "address=" + streetAddress + ";postal_code=" + zipCode;
                String encodedAddress = null;
                String encodedZip = null;
                String encodedName = null;
                try {
                    encodedName = URLEncoder.encode(name, "UTF-8");
                    encodedAddress = URLEncoder.encode(streetAddress, "UTF-8");
                    encodedZip = URLEncoder.encode(zipCode, "UTF-8");
                    searchText = "address=" + encodedAddress + ";postal_code=" + encodedZip + ";name="+encodedName;
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Encoding exception");
                    e.printStackTrace();
                }
                if (encodedAddress != null && encodedZip != null) {
                    String apiUrl = openTableApiUrl + "restaurants?" + searchText;
                    new OpenTableCallApi().execute(apiUrl);
                }
            }
        }
        else {
            Button reserveOnlineButton = (Button) findViewById(R.id.reservationOnlineButton);
            if(reserveOnlineButton != null) {
                reserveOnlineButton.setEnabled(false);
            }
        }
    }

    @Override
    public void getLastLoc() {
        mGoogleApiClient.connect();
    }

    @Override
    public void toggleSwitch(Switch p) {
        mSectionsPagerAdapter.getAlertsFrag().onTogglePings(p, null);
    }

    public String getLastLat() {
        if (mLastLocation != null) {
            return String.valueOf(mLastLocation.getLatitude());
        }
        else {
            return null;
        }
    }

    public String getLastLng() {
        if (mLastLocation != null) {
            return String.valueOf(mLastLocation.getLongitude());
        }
        else {
            return null;
        }
    }


    @Override
    public void updateReservationStatus(boolean isReserved) {
        updateHasReservation(isReserved);
    }

    public void showNewPlanDialog(boolean cancelable) {
        final View nameNewPlan = getLayoutInflater().inflate(R.layout.dialog_newplan, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(nameNewPlan);

        EditText name = (EditText) nameNewPlan.findViewById(R.id.new_plan_edittext);

        if (cancelable) {
            builder.setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    EditText name = (EditText) nameNewPlan.findViewById(R.id.new_plan_edittext);
                    mSqlHelper.insertNewPlan(name.getText().toString());
                    mArrayAdapter.clear();
                    mArrayAdapter.addAll(mSqlHelper.getPlans());
                    mArrayAdapter.notifyDataSetChanged();
                    mSpinner.setSelection(mArrayAdapter.getPosition(name.getText().toString()));
                    planChanged();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
        } else {
            builder.setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    EditText name = (EditText) nameNewPlan.findViewById(R.id.new_plan_edittext);
                    mSqlHelper.insertNewPlan(name.getText().toString());
                    mArrayAdapter.clear();
                    mArrayAdapter.addAll(mSqlHelper.getPlans());
                    mArrayAdapter.notifyDataSetChanged();
                    mSpinner.setSelection(mArrayAdapter.getPosition(name.getText().toString()));
                    planChanged();
                    }
                });
        }

        final AlertDialog alert = builder.create();
        alert.show();

        if(name.getText().toString().isEmpty()) {
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
    }

    public void showDeletePlanDialog(final String plan) {
        final View planDeleteView = getLayoutInflater().inflate(R.layout.dialog_plan_delete, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(planDeleteView);

        builder.setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (getSqlHelper().arePingsOn(plan)) {
                            stopAlarm();
                        }
                        getSqlHelper().deletePlan(plan);
                        mArrayAdapter.clear();
                        List plans = mSqlHelper.getPlans();
                        mArrayAdapter.addAll(plans);
                        mArrayAdapter.notifyDataSetChanged();
                        mSpinner.setSelection(0);
                        if (plans.size() == 0) {
                            showNewPlanDialog(false);
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // close dialog
                    }
                }
        );
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void showPingIntervalDialog(final String initValue, final TextView v) {
        final View pingIntervalView = getLayoutInflater().inflate(R.layout.dialog_pinginterval, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(pingIntervalView);
        EditText intervalEditText = (EditText) pingIntervalView.findViewById(R.id.pingIntervalEditText);
        intervalEditText.setText(initValue);

        builder.setCancelable(true)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditText intervalEdit = (EditText) pingIntervalView.findViewById(R.id.pingIntervalEditText);
                    String interval = intervalEdit.getText().toString();
                    mSqlHelper.updatePingInterval(MainActivity.this, Integer.parseInt(interval));
                    //reset timer interval [CRISTHIAN]
                    resetAlarm(Integer.parseInt(interval));
                    v.setText(interval);
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    v.setText(initValue);
                }
            }
        );
        final AlertDialog alert = builder.create();
        alert.show();

        intervalEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
    }

    public void showPingAllowanceDialog(final String initValue, final TextView v) {
        final View pingAllowanceView = getLayoutInflater().inflate(R.layout.dialog_pingallowance, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(pingAllowanceView);
        EditText pingAllowanceEditText = (EditText) pingAllowanceView.findViewById(R.id.pingAllowanceEditText);
        pingAllowanceEditText.setText(initValue);

        builder.setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText allowanceEdit = (EditText) pingAllowanceView.findViewById(R.id.pingAllowanceEditText);
                        String allowance = allowanceEdit.getText().toString();
                        mSqlHelper.updatePingAllowance(MainActivity.this, Integer.parseInt(allowance));
                        //reset timer for checkin [Cristhian]
                        resetAlarm(getCurrentInterval());
                        v.setText(allowance);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        v.setText(initValue);
                    }
                }
        );
        final AlertDialog alert = builder.create();
        alert.show();

        pingAllowanceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
    }


    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
//            Log.i(TAG, "the location should be below: ");
//            Log.i(TAG, String.valueOf(mLastLocation.getLatitude()) + ", " + String.valueOf(mLastLocation.getLongitude()));
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "connection failed");
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private PlanDetailsFragment mPlanDetailsFrag;
        private DirectionsFragment mDirectionsFrag;
        private AlertsFragment mAlertsFrag;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public PlanDetailsFragment getPlanDetailsFrag() {
            return mPlanDetailsFrag;
        }

        public DirectionsFragment getDirectionsFrag() {
            return mDirectionsFrag;
        }

        public AlertsFragment getAlertsFrag() {
            return mAlertsFrag;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch(position) {
                case 0:
                    if (mPlanDetailsFrag == null) {
                        mPlanDetailsFrag = PlanDetailsFragment.newInstance(mSpinner.getSelectedItem().toString(), "blah");
                    }
                    return mPlanDetailsFrag;
                case 1:
                    if (mDirectionsFrag == null) {
                        mDirectionsFrag = DirectionsFragment.newInstance(mSpinner.getSelectedItem().toString(), "blah");
                    }
                    return mDirectionsFrag;
                case 2:
                    if (mAlertsFrag == null) {
                        mAlertsFrag = AlertsFragment.newInstance(mSpinner.getSelectedItem().toString(), "blah");
                    }
                    return  mAlertsFrag;
                default:
                    if (mPlanDetailsFrag == null) {
                        mPlanDetailsFrag = PlanDetailsFragment.newInstance(mSpinner.getSelectedItem().toString(), "blah");
                    }
                    return mPlanDetailsFrag;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            //TODO: try to switch these to icons
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /*
    Private class taken from WebAPIExample code from class
     */
    private class CallAPI extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, "CallAPI doInBackground called");
            String urlString = params[0]; // URL to call

            HttpURLConnection urlConnection = null;

            InputStream in = null;
            StringBuilder sb = new StringBuilder();

            char[] buf = new char[4096];

            // do the HTTP Get
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(false);
                InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());

                Log.i(TAG, "got input stream");

                int read;
                while ((read = reader.read(buf)) != -1) {
                    sb.append(buf, 0, read);
                }
            } catch (Exception e) {
                // if any I/O error occurs
                Log.e(TAG, "exception when reading input stream reader");
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                try {
                    // releases any system resources associated with the stream
                    if (in != null)
                        in.close();
                } catch (IOException e) {
                    Log.i(TAG + " Error:", e.getMessage());
                }
            }
            Log.i(TAG, "Finished reading");
            return sb.toString();
        }

        /**
         * Method ran after receiving response from API
         * @param result string result returned from API
         */
        protected void onPostExecute(String result) {
            Log.i(TAG, "starting onPostExecute CallAPI");
        }

    } // end CallAPI

    /**
     * Private class for connecting to OpenTable API
     */
    private class OpenTableCallApi extends CallAPI {
        /**
         * Method ran after receiving response from API
         * @param result string result returned from API
         */
        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "starting onPostExecute OpenTableCallApi");

            JSONArray restaurantEntries = null;
            // separate this out so people can work on it.
            try {
                JSONObject jObject = new JSONObject(result);
                Log.i(TAG, result);
                restaurantEntries = jObject.getJSONArray("restaurants");

            } catch (JSONException e) {
                Log.e(TAG, "Could not read OpenTable JSON result");
                Log.i(TAG, e.getMessage());
            }

            if (restaurantEntries != null) {
                setReservationInfo(restaurantEntries);
            }
        }
    }

    /**
     * Private class for connecting to Google Distance Matrix API
     */
    public class GoogleDistanceMatrixCallApi extends CallAPI {
        String delimiter = "~~~";
        @Override
        protected String doInBackground(String... params) {
            String superResult = super.doInBackground(params);
            String mode = params[1];
            return superResult + delimiter + mode;
        }

        /**
         * Method ran after receiving response from API
         * @param result string result returned from API
         */
        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "starting Distance Matrix onPostExecute");
            String[] resultSplit = result.split(delimiter);
            String queryResult = resultSplit[0];
            String mode = resultSplit[1];

            JSONObject rowResult = null;
            JSONObject distanceResult = null;
            String durationResultText = "?";

            try {
                JSONObject jObject = new JSONObject(queryResult);
                rowResult = jObject.getJSONArray("rows").getJSONObject(0);
                distanceResult = rowResult.getJSONArray("elements").getJSONObject(0);
                JSONObject durationResult = distanceResult.getJSONObject("duration");
                durationResultText = durationResult.getString("text");

            } catch (JSONException e) {
                Log.e(TAG, "Could not read Distance Matrix JSON result");
                Log.i(TAG, e.getMessage());
            }

            if (distanceResult == null) {
                Log.i(TAG, "could not calculate distance, using ?");
                durationResultText = "?";
            }

            switch (mode) {
                case "driving":
                    DirectionsFragment.setDrivingETA(durationResultText);
                    break;
                case "transit":
                    DirectionsFragment.setTransitETA(durationResultText);
                    break;
                case "walking":
                    DirectionsFragment.setWalkingETA(durationResultText);
                    break;
            }
        }
    }

    public void resetDistanceMatrixApiCaller() {
        googleDistanceMatrixCallApi = new GoogleDistanceMatrixCallApi();
    }

    /**
     * Private class for connecting to Google Geocoding API
     */
    public class GoogleGeocodingCallApi extends CallAPI {
        /**
         * Method ran after receiving response from API
         * @param result string result returned from API
         */
        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "starting Geocoding onPostExecute");
            JSONObject geocodingResult = null;
            String lat = null;
            String lng = null;
            String placeID = null;

            try {
                JSONObject jObject = new JSONObject(result);
                geocodingResult = jObject.getJSONArray("results").getJSONObject(0);
                JSONObject geometryResult = geocodingResult.getJSONObject("geometry");
                JSONObject locationResult = geometryResult.getJSONObject("location");
                lat = String.valueOf(locationResult.getDouble("lat"));
                lng = String.valueOf(locationResult.getDouble("lng"));
                placeID = geocodingResult.getString("place_id");

            } catch (JSONException e) {
                Log.e(TAG, "Could not read Geocoding JSON result", e);
                Log.i(TAG, e.getMessage());
            }

            if (geocodingResult != null) {
                // TODO: Call helper to send data back to fragment
                DirectionsFragment.storeHomeInfo(lat, lng, placeID);
            }
        }
    }

    public void resetGeocodingApiCaller() {
        googleGeocodingCallApi = new GoogleGeocodingCallApi();
    }

    /**
     * Private class for connecting to Google Places API
     */
    private class GooglePlacesCallApi extends CallAPI {
        /**
         * Method ran after receiving response from API
         * @param result string result returned from API
         */
        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "starting Places onPostExecute");
            JSONArray placesResults = null;
            try {
                JSONObject jObject = new JSONObject(result);
                placesResults = jObject.getJSONArray("results");

            } catch (JSONException e) {
                Log.e(TAG, "Could not read Places JSON result");
                Log.i(TAG, e.getMessage());
            }

            if (placesResults != null) {
//                displayRestaurants(placesResults);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    /**
     * Method that sets reservation URL when it finds the chosen location via OpenTable API
     * @param restaurants JSONArray of the found restaurant
     */
    private void setReservationInfo(final JSONArray restaurants) {
        Log.i(TAG, "calling setReservationInfo");
        String reservationUrl = null;

        Button reserveOnlineButton = (Button)findViewById(R.id.reservationOnlineButton);
        if(restaurants.length() == 0) {
            //There is no entry for this restaurant on OpenTable
            //Set button to not usable

            reserveOnlineButton.setEnabled(false);
        }
        else if(restaurants.length() > 1) {
            Log.i(TAG, "OH NO! More than one place matches that description!");
        }
        else {
            try {
                JSONObject restInfo = (JSONObject) restaurants.get(0);
                reservationUrl = restInfo.getString("mobile_reserve_url");
                if (reservationUrl != null) {
                    mSqlHelper.updatePlanPlaceInfo(this, "PLACE_URL", reservationUrl);
                    Log.i(TAG, "just added a url to the DB");
                    reserveOnlineButton.setEnabled(true);
                } else {
                    Log.i(TAG, "WELP! no reservation URL exists!");
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

    }

    public void updatePlanReservationTime(int hour, int min) {
        mSqlHelper.updatePlanReservationTime(this, hour, min);
    }

    public void updatePlanReservationDate(int year, int month, int day) {
        mSqlHelper.updatePlanReservationDate(this, year, month, day);
    }

    public void updateHasReservation(boolean reserved) {
        mSqlHelper.updateHasReservation(this, reserved);
    }

    public String getCurrentPlanName() {
        return mSpinner.getSelectedItem().toString();
    }


    public LocationManager getLocationManager() {
        return mLocationManager;
    }

    public MyOpenHelper getSqlHelper() {
        return mSqlHelper;
    }

    public void notifyDirFragOfDestChange() {
        DirectionsFragment f = (DirectionsFragment) mSectionsPagerAdapter.getItem(1);
        f.onDestinationChanged();
    }


    public void stopAlarm() {
        if(alarmManager != null) {
            mSqlHelper.updatePingMisses(this, 0);
            Intent alarmIntent = new Intent(this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
            alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }


    }

    public void resetAlarm(int interval) {
        stopAlarm();
        setAlarm(interval);
    }

    public void setAlarm(int durationMinute) {
        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("plan", getCurrentPlanName());
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        /** Setting an alarm, which invokes the operation at alarm_time */
        long duration = 60000*durationMinute;
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, duration + SystemClock.elapsedRealtime(), duration, pendingIntent);
    }

    public void userCheckin() {
        int interval = mSqlHelper.getPingInterval(this);
        mSqlHelper.updatePingMisses(this, 0);
        resetAlarm(interval);
        Toast.makeText(getBaseContext(), "Successfully checked in!", Toast.LENGTH_SHORT).show();
    }

    public int getCurrentInterval() {
        int interval = mSqlHelper.getPingInterval(this);
        return interval;
    }

}
