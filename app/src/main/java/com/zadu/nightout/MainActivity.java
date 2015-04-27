package com.zadu.nightout;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.actions.ReserveIntents;
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
    private ArrayAdapter locationArrayAdapter;
    private MyOpenHelper mSqlHelper;
    private LocationManager mLocationManager;
    private Dialog dialog;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    String TAG = "MainActivity";
    String openTableApiUrl = "http://opentable.herokuapp.com/api/";

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
            startActivity(intent);
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mSqlHelper = new MyOpenHelper(this);

        actionBar.setCustomView(R.layout.custom_actionbar);
        actionBar.setDisplayShowCustomEnabled(true);

        //Set up Google Client API
        buildGoogleApiClient();

        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (int i=0; i<mSectionsPagerAdapter.getCount(); i++) {
                    Fragment f = mSectionsPagerAdapter.getItem(i);
                    ((PlanChangedListener) f).onPlanChanged();
                }
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

//        button1.setOnClickListener(onClickListener);
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
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_new_plan) {
            final View nameNewPlan = getLayoutInflater().inflate(R.layout.dialog_newplan, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(nameNewPlan);

            EditText name = (EditText) nameNewPlan.findViewById(R.id.new_plan_edittext);

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
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            final AlertDialog alert = builder.create();
            alert.show();

            if(name.getText().toString().isEmpty()) {
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }

            name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

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
        String initialUrl = "https://www.opentable.com/";
        TextView destination = (TextView) findViewById(R.id.destinationName);
        String dest = destination.getText().toString();

        String url = initialUrl + dest.replace(' ', '-');

        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if(intent.resolveActivity(getPackageManager()) != null) {
            Log.i(TAG, "Webb browser intent about to be called");
            startActivity(intent);
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
        String address = planAddressText.getText().toString() + destinationCityStateZip.getText().toString();
        boolean isReserved = reservationMade.isChecked();
        Button dateButton = (Button)findViewById(R.id.datePickerButton);
        String date = dateButton.getText().toString();
        Button timeButton = (Button)findViewById(R.id.timePickerButton);
        String time = timeButton.getText().toString();
        String reservationMessage = "";
        if(isReserved) {
            reservationMessage = "A reservation has already been made.";
        }

        String messageBody = "Hi, I will be going to " + destination + " " + address+ " on " + date + " at " + time + ". I hope to see " +
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
//                TextView timeView = (TextView)findViewById(R.id.selectedTimeText);
//                timeView.setText(hourOfDay + " : " + minute);
                Button timePickerButton = (Button)findViewById(R.id.timePickerButton);
                timePickerButton.setText(hourOfDay + " : " + minute);
                updatePlanReservationTime(mHour, mMinute);
            }
        }, mHour, mMinute, false);
        tpd.show();
    }

    @Override
    public void showDatePickerDialog(Object something) {
        Log.i(TAG, "called showDatePickerDialog()");
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
//                        TextView dateView = (TextView)findViewById(R.id.selectedDateText);
//                        dateView.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        Button datePickerButton = (Button) findViewById(R.id.datePickerButton);
                        datePickerButton.setText(dayOfMonth + "/" + (monthOfYear+1) + "/" +year);
                        updatePlanReservationDate(mYear, mMonth, mDay);
                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

    @Override
    public void OnAlertFragmentInteraction(Object object) {
        //TODO: interact with any passed info in Object
    }

    @Override
    public void OnDirectionsFragmentInteraction(Object object) {
        //TODO: interact with any passed info in Object
    }

    @Override
    public void callRide(Object something) {
        Log.i(TAG, "callRide() called");
        TextView currentAddressTextView = (TextView)findViewById(R.id.current_address);
        String currentAddress = currentAddressTextView.getText().toString();
        Intent intent = new Intent(ReserveIntents.ACTION_RESERVE_TAXI_RESERVATION);
        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.i(TAG, "resolving taxi intent");
            startActivity(intent);
        }
    }

    @Override
    public void openGoogleMapsDirections(Object something) {
        Log.i(TAG, "openGoogleMapsDirections() called");
        TextView destAddressTextView = (TextView)findViewById(R.id.dest_address);
        String destAddress = destAddressTextView.getText().toString();
        String destAddressFormatted = destAddress.replace(" ", "+");
        Log.i(TAG, destAddressFormatted);
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destAddressFormatted);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
        if(mapIntent.resolveActivity(getPackageManager()) != null) {
            Log.i(TAG, "Google Maps Navigation about to be called");
            startActivity(mapIntent);
        }
    }

/*    @Override
    public void findLocation(Object something) {
        Log.i(TAG, "findLocation() called");
        EditText searchField = (EditText) findViewById(R.id.searchField);
        String searchText = searchField.getText().toString();
        String encodedInput = null;
        try{
            encodedInput = URLEncoder.encode(searchText, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Encoding exception");
            e.printStackTrace();
        }
        if(encodedInput != null) {
            String apiUrl = openTableApiUrl + "restaurants?name=" + encodedInput;
            new CallAPI().execute(apiUrl);
        }
    }*/

    @Override
    public void getLastLoc() {
        mGoogleApiClient.connect();

//        Location recentLoc = new Location(mLastLocation); //copy of location
//        Log.i(TAG, "location: " + String.valueOf(recentLoc.getLatitude()) + ", " + String.valueOf(recentLoc.getLongitude()));
    }


    @Override
    public void updateReservationStatus(boolean isReserved) {
        updateHasReservation(isReserved);
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
                    //TODO: reset timer interval [CRISTHIAN]
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
                        //TODO: reset timer for checkin [Cristhian]
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
            Log.i(TAG, "the location should be below: ");
            Log.i(TAG, String.valueOf(mLastLocation.getLatitude()) + String.valueOf(mLastLocation.getLongitude()));
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "connection failed");
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

            String urlString = params[0]; // URL to call

            HttpURLConnection urlConnection = null;

            InputStream in = null;
            StringBuilder sb = new StringBuilder();

            char[] buf = new char[4096];

            // do the HTTP Get
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());

                Log.i(TAG, "got input stream");

                int read;
                while ((read = reader.read(buf)) != -1) {
                    sb.append(buf, 0, read);
                }
            } catch (Exception e) {
                // if any I/O error occurs
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
            Log.i(TAG, "starting onPostExecute");

            JSONArray resultEntries = null;
            //TODO: Filter based upon the API used
            // separate this out so people can work on it.
            try {
                JSONObject jObject = new JSONObject(result);

            } catch (JSONException e) {
            }

            if (resultEntries != null) {
            }
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
            Log.i(TAG, "starting onPostExecute");

            JSONArray restaurantEntries = null;
            // separate this out so people can work on it.
            try {
                JSONObject jObject = new JSONObject(result);
                Log.i(TAG, result);
                restaurantEntries = jObject.getJSONArray("restaurants");

            } catch (JSONException e) {
                Log.e(TAG, "Could not find restaurants entry in JSON result");
                Log.i(TAG, e.getMessage());
            }

            if (restaurantEntries != null) {
                displayRestaurants(restaurantEntries);
            }
        }
    }

    /**
     * Private class for connecting to OpenTable API
     */
    private class GooglePlacesCallApi extends CallAPI {
        /**
         * Method ran after receiving response from API
         * @param result string result returned from API
         */
        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "starting onPostExecute");

            JSONArray placesResults = null;
            try {
                JSONObject jObject = new JSONObject(result);
                Log.i(TAG, result);
                placesResults = jObject.getJSONArray("restaurants");

            } catch (JSONException e) {
                Log.e(TAG, "Could not find restaurants entry in JSON result");
                Log.i(TAG, e.getMessage());
            }

            if (placesResults != null) {
                displayRestaurants(placesResults);
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

//    TODO: Finish the function below
    /**
     * Filters and displays restaurant options
     * @param restaurants JSONArray of restaurants and their relevant information
     */
    private void displayRestaurants(final JSONArray restaurants) {
        final ArrayList<String> restaurantNames = new ArrayList<>();
        final ArrayList<String> restaurantAddresses = new ArrayList<>();
        ArrayList<Integer> restaurantIDs = new ArrayList<>();
        final ArrayList<String> restaurantPhones = new ArrayList<>();

        for(int i = 0; i < restaurants.length(); i++) {
            try {
                JSONObject restInfo = (JSONObject) restaurants.get(i);
                restaurantNames.add(restInfo.getString("name"));
                String finaAddress = restInfo.getString("address") +  " " + restInfo.getString("city") + ", " + restInfo.getString("state") + " " + restInfo.getString("postal_code");
                restaurantAddresses.add(finaAddress);
                restaurantIDs.add(restInfo.getInt("id"));
                restaurantPhones.add(restInfo.getString("phone"));

            }
            catch (JSONException e){
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Location");

        final ListView modeList = new ListView(this);
        locationArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, restaurantNames);
        modeList.setAdapter(locationArrayAdapter);

        builder.setView(modeList);
        modeList.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int pos, long id) {
                String itemName = restaurantNames.get(pos);
                String itemAddress = restaurantAddresses.get(pos);
                String itemPhone = restaurantPhones.get(pos);

                Log.i(TAG, "item clicked" + itemName);
                updatePlaceInfo("PLACE_NAME", itemName);
                updatePlaceInfo("PLACE_ADDRESS", itemAddress);
                updatePlaceInfo("PLACE_NUMBER", itemPhone);


                dialog.dismiss();
            }
        });
        dialog = builder.create();

        dialog.show();
    }

    public void updatePlaceInfo(String infoType, String infoVal) {
        mSqlHelper.updatePlanPlaceInfo(this, infoType, infoVal);
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

    private class RunCheckInTimer extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            //the Date and time at which you want to execute
            int year = mSqlHelper.getReservationInfo(MainActivity.this, "RESERVATION_YEAR");
            int month = mSqlHelper.getReservationInfo(MainActivity.this, "RESERVATION_MONTH");
            int day = mSqlHelper.getReservationInfo(MainActivity.this, "RESERVATION_DATE");
            int hour = mSqlHelper.getReservationInfo(MainActivity.this, "RESERVATION_HOUR");
            int min = mSqlHelper.getReservationInfo(MainActivity.this, "RESERVATION_MINUTE");
            int intervalTime = mSqlHelper.getReservationInfo(MainActivity.this,"PING_INTERVAL");

            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = null;
            try {
//                date = dateFormatter.parse("2012-07-06 13:05:45");
                date = dateFormatter.parse(year+ "-" + month + "-" + day + " " + hour + ":" + min);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Now create the time and schedule it
            Timer timer = new Timer();

            //Use this if you want to execute it once
            timer.schedule(new MyTimeTask(), date);

            //Use this if you want to execute it repeatedly
            int period = 60000 * intervalTime;//60secs * interval(in minutes)
            timer.schedule(new MyTimeTask(), date, period );
            return null;
        }
    }

    private static class MyTimeTask extends TimerTask {
        public void run() {
            //TODO: send notification here
            Log.i("timer task", "derp");
        }
    }

    public LocationManager getLocationManager() {
        return mLocationManager;
    }

    public void notifyDirFragOfDestChange(String destName, String destAddress) {
        DirectionsFragment f = (DirectionsFragment) mSectionsPagerAdapter.getItem(1);
        f.onDestinationChanged(destName, destAddress);
    }
}

