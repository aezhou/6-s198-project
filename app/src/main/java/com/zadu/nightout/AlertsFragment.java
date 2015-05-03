package com.zadu.nightout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.zadu.nightout.AlertsFragment.OnAlertsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AlertsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlertsFragment extends Fragment implements PlanChangedListener,
            SharedPreferences.OnSharedPreferenceChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SimpleCursorAdapter mContactsAdapter;
    private MyOpenHelper mSqlHelper;
    private View mView;

    private OnAlertsFragmentInteractionListener mListener;
    private SharedPreferences mPrefs;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlertsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlertsFragment newInstance(String param1, String param2) {
        AlertsFragment fragment = new AlertsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AlertsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_alerts, container, false);

        ListView list = (ListView) v.findViewById(R.id.contactsListView);
        Cursor c = mSqlHelper.getContactsToDisplay((MainActivity) getActivity());
        isDummyContactSet = c.moveToFirst();
        if (!isDummyContactSet) {
            setDummyContact();
            isDummyContactSet = true;
            c = mSqlHelper.getContactsToDisplay((MainActivity) getActivity());
            c.moveToFirst();
        }
        Log.d("ALERT FRAG", c.getString(c.getColumnIndex(mSqlHelper.CONTACT_NAME)));
        Log.d("ALERT FRAG", c.getString(c.getColumnIndex(mSqlHelper.CONTACT_NUMBER)));
        Log.d("ALERT FRAG", String.valueOf(c.getInt(c.getColumnIndex(mSqlHelper.IS_ON))));
        mContactsAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.list_item_contact,
                c,
                new String[] { mSqlHelper.CONTACT_NAME, mSqlHelper.CONTACT_NUMBER, mSqlHelper.IS_ON },
                new int[] { R.id.contactNameTextView, R.id.contactDescriptionTextView, R.id.contactCheckBox },
                0);

        list.setAdapter(mContactsAdapter);
        final View.OnClickListener contactsCheckListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              onContactChecked((CheckBox) v, true);
            }
        };
        mContactsAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.contactCheckBox) {
                    view.setOnClickListener(contactsCheckListener);
                    if (cursor.getInt(columnIndex) == 1) {
                        ((CheckBox) view).setChecked(true);
                    } else {
                        ((CheckBox) view).setChecked(false);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        LinearLayout contactsListHeader = (LinearLayout) v.findViewById(R.id.contactsListHeader);
        contactsListHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View headerView) {
                onCollapseContacts(false);
            }
        });

        CheckBox contactsListToggle = (CheckBox) v.findViewById(R.id.collapseContactsToggle);
        contactsListToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View toggleView) {
                onCollapseContacts(true);
            }
        });

        // set button and switch listeners

        Switch pingSwitch = (Switch) v.findViewById(R.id.pingSwitch);
        boolean pingsOn = mSqlHelper.arePingsOn((MainActivity) getActivity());
        pingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onTogglePings(buttonView, v);
            }
        });
        pingSwitch.setChecked(pingsOn);
        onTogglePings(pingSwitch, v);


        TextView pingIntervalText = (TextView) v.findViewById(R.id.pingIntervalText);
        int savedInterval = mSqlHelper.getPingInterval((MainActivity) getActivity());
        pingIntervalText.setText(String.valueOf(savedInterval));
        pingIntervalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String initValue = ((TextView) v).getText().toString();
                ((MainActivity) getActivity()).showPingIntervalDialog(initValue, (TextView) v);
            }
        });
        TextView pingAllowanceText = (TextView) v.findViewById(R.id.pingAllowanceText);
        int savedAllowance = mSqlHelper.getPingAllowance((MainActivity) getActivity());
        pingAllowanceText.setText(String.valueOf(savedAllowance));
        pingAllowanceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String initValue = ((TextView) v).getText().toString();
                ((MainActivity) getActivity()).showPingAllowanceDialog(initValue, (TextView) v);
            }
        });

        Button checkInButton = (Button) v.findViewById(R.id.checkInButton);
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckIn();
            }
        });

        Button otherContactButton = (Button) v.findViewById(R.id.otherContactButton);
        otherContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });

        // set texting button listeners

        Button ThereSafeButton = (Button) v.findViewById(R.id.ThereSafeButton);
        ThereSafeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessageButton("I made it there safe!", false);
            }
        });

        Button HomeSafeButton = (Button) v.findViewById(R.id.HomeSafeButton);
        HomeSafeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessageButton("I made it home safe!", false);
            }
        });

        Button AllClearButton = (Button) v.findViewById(R.id.AllClearButton);
        AllClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessageButton("I'm safe! You can ignore my previous messages.", false);
            }
        });

        Button GetMeButton = (Button) v.findViewById(R.id.GetMeButton);
        GetMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessageButton("Come get me ASAP.", true);
            }
        });

        Button FakeCallButton = (Button) v.findViewById(R.id.FakeCallButton);
        FakeCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fakeCall();
            }
        });

        Button PanicButton = (Button) v.findViewById(R.id.PanicButton);
        PanicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessageButton("I'm in danger! HELP!", true);
            }
        });

        mView = v;
        return v;
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (data != null) {
            Uri contactData = data.getData();
            String contactNumber = null;
            String contactName = null;

            if (resultCode == getActivity().RESULT_OK) {
                Cursor cursor = getActivity().getContentResolver().query(contactData, null, null, null, null);
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                cursor.close();
            }


            if (reqCode == 0 && contactName != null && contactNumber != null) {
                mSqlHelper.addPlanContactNumber((MainActivity) getActivity(), contactName, contactNumber);
                mContactsAdapter.changeCursor(mSqlHelper.getContactsToDisplay((MainActivity) getActivity()));
                ListView list = (ListView) mView.findViewById(R.id.contactsListView);
                list.setAdapter(mContactsAdapter);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSqlHelper = ((MainActivity) getActivity()).getSqlHelper();
        try {
            mListener = (OnAlertsFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onCollapseContacts(boolean wasToggleClicked) {
        View contactsView = getActivity().findViewById(R.id.contactsView);
        CheckBox toggle = (CheckBox) getActivity().findViewById(R.id.collapseContactsToggle);
        if (contactsView.getVisibility() == View.GONE) {
            contactsView.setVisibility(View.VISIBLE);
            if (!wasToggleClicked) {toggle.setChecked(true);}
        } else {
            contactsView.setVisibility(View.GONE);
            if (!wasToggleClicked) {toggle.setChecked(false);}
        }
    }

    public void onContactChecked(CheckBox v, boolean isDefault) {
        // might be an artifact of weird initial test db entries??
        int numChecked = mSqlHelper.getNumCheckedContacts((MainActivity) getActivity());
        if (numChecked > 1 || v.isChecked()) {
            LinearLayout l = (LinearLayout) v.getParent();
            TextView numberView = (TextView) l.findViewById(R.id.contactDescriptionTextView);
            String number = numberView.getText().toString();
            mSqlHelper.checkPlanContactNumber((MainActivity) getActivity(), number, v.isChecked());
        } else {
            v.setChecked(true); // reset the checkbox back to on
            Toast.makeText(getActivity(), "You must keep at least one emergency contact checked.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onTogglePings(CompoundButton toggleView, View masterView) {
        if (masterView == null) {
            masterView = getView();
        }

        if (masterView != null) {
            Switch toggle = (Switch) toggleView;
            View detailView = masterView.findViewById(R.id.pingDetailsLayout);
            View offView = masterView.findViewById(R.id.pingOffLayout);

            mSqlHelper.updatePingsOnOff((MainActivity) getActivity(), true);

            if (toggle.isChecked()) {
                Log.i("togglePings", "toggle is checked");
                detailView.setVisibility(View.VISIBLE);
                offView.setVisibility(View.GONE);

                int duration = mSqlHelper.getPingInterval((MainActivity) getActivity());
                ((MainActivity) getActivity()).setAlarm(duration, false);

            } else {
                Log.i("togglePings", "toggle off");
                detailView.setVisibility(View.GONE);
                offView.setVisibility(View.VISIBLE);
                mSqlHelper.updatePingsOnOff((MainActivity) getActivity(), false);
                ((MainActivity) getActivity()).stopAlarm();
            }
        }
    }

    public void onCheckIn() {
        Toast.makeText(getActivity(), "You Checked In!", Toast.LENGTH_SHORT).show();
        ((MainActivity)getActivity()).userCheckin();
    }

    public ArrayList<String> getSetContactNumbers() {
        return mSqlHelper.getContactNumbers((MainActivity) getActivity());
    }

    public void onMessageButton(String message, boolean sendGPS) {
        if (getSetContactNumbers().size() == 0) {
            Toast.makeText(getActivity(), "No contact numbers to send to!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sendGPS) {
            String locationProvider = LocationManager.NETWORK_PROVIDER;
            LocationManager locationManager = ((MainActivity) getActivity()).getLocationManager();
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();
                String locationMsg = " My last known GPS coordinates were: " +
                    latitude + ", " + longitude;
                message = message + locationMsg;
            }
        }

        String separator = "; ";
//        if(android.os.Build.MANUFACTURER.equalsIgnoreCase("Samsung")){
//            separator = ", ";
//        }
        String smsto = "";
        for (String phoneNumber : getSetContactNumbers()) {
            smsto = smsto + phoneNumber + separator;
        }
        smsto = smsto.substring(0, smsto.length()-1); // remove trailing semicolon
        Log.d("SendMessage", "to: " + smsto);


        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra("address", smsto);
        intent.putExtra("sms_body", message);
        intent.setData(Uri.parse("sms:" + smsto));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            Log.d("SendMessage", "starting SMS activity");
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "No SMS Application Found!", Toast.LENGTH_LONG).show();
        }
    }

    public void missedCheckinMessage(ArrayList<String> numbers, String message) {
        if (numbers.size() == 0) {
            Toast.makeText(getActivity(), "No contact numbers to send to!", Toast.LENGTH_SHORT).show();
            return;
        }

        String locationProvider = LocationManager.NETWORK_PROVIDER;
        LocationManager locationManager = ((MainActivity) getActivity()).getLocationManager();
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {
            double latitude = lastKnownLocation.getLatitude();
            double longitude = lastKnownLocation.getLongitude();
            String locationMsg = " My last known GPS coordinates were: " +
                    latitude + ", " + longitude;
            message = message + locationMsg;
        }

        SmsManager smsManager = SmsManager.getDefault();
        if (smsManager != null) {
            for (String phoneNumber : numbers) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            }
        }

    }

    public String getMyNum() {
        if (mPrefs==null) {return null;}
        String unformattedNum = mPrefs.getString("phone_number", null);
        if (unformattedNum==null) {return null;}
        return PhoneNumberUtils.stripSeparators(unformattedNum);
    }

    public void fakeCall() {
        Toast.makeText(getActivity(), "Making Fake Call!", Toast.LENGTH_SHORT).show();
        final Button callButton = (Button) mView.findViewById(R.id.FakeCallButton);
        callButton.setEnabled(false);

        String myNum = getMyNum();
        if (myNum == null) {
            Toast.makeText(getActivity(), "Couldn't access this phone's number! Check settings.", Toast.LENGTH_SHORT).show();
            return;
        }
        final String toNum = myNum;
        String fromArea = "555";
        if (toNum.length() > 3) {
            fromArea = toNum.substring(0, 3);
        }
        final String fromNum = fromArea+"3141592";

        // TelAPI credentials
        String telapiSid = "ACbf889084aa9ca594bead45998f1506f9";
        String telapiToken = "6d7f40fb0da44503a520350fd07398ff";
        String telapiCredentials = telapiSid + ":" + telapiToken;
        String telapiBase64EncodedCredentials = Base64.encodeToString(telapiCredentials.getBytes(), Base64.NO_WRAP);

        // ion handles asynch-ness
        Ion.with(getActivity())
            .load("https://"+telapiSid+":"+telapiToken+"@api.telapi.com/v2/Accounts/"+telapiSid+"/Calls")
            .addHeader("Authorization", "Basic " + telapiBase64EncodedCredentials)
            .setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
            .setBodyParameter("To", toNum)
            .setBodyParameter("From", fromNum)
            .setBodyParameter("Url", "http://www.telapi.com/ivr/welcome/call")
            .asString()
            .setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    Log.d("ALERT FRAG", "completed post to fake call");
                    if (e!=null) {
                        Log.d("ALERT FRAG ERR", e.getMessage());
                    }
                    if (result!= null) {
                        Log.d("ALERT FRAG RESULT", result.toString());
                    }
                    Log.d("ALERT FRAG", "done printing result");
                    callButton.setEnabled(true);
                }
            });
    }

    private boolean isDummyContactSet = false;

    public void setDummyContact() {
        mSqlHelper.insertDefaultContact("Kristin", "5404464776");
    }

    public void updatePingsUIOnly() {
        boolean pingsOn = mSqlHelper.arePingsOn((MainActivity) getActivity());

        Switch pingSwitch = (Switch) mView.findViewById(R.id.pingSwitch);
        pingSwitch.setChecked(pingsOn);

        View detailView = mView.findViewById(R.id.pingDetailsLayout);
        View offView = mView.findViewById(R.id.pingOffLayout);
        if (pingSwitch.isChecked()) {
            detailView.setVisibility(View.VISIBLE);
            offView.setVisibility(View.GONE);

        } else {
            detailView.setVisibility(View.GONE);
            offView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPlanChanged() {
        if (mView != null) {
            updatePingsUIOnly();

            TextView pingIntervalText = (TextView) mView.findViewById(R.id.pingIntervalText);
            int savedInterval = mSqlHelper.getPingInterval((MainActivity) getActivity());
            pingIntervalText.setText(String.valueOf(savedInterval));

            TextView pingAllowanceText = (TextView) mView.findViewById(R.id.pingAllowanceText);
            int savedAllowance = mSqlHelper.getPingAllowance((MainActivity) getActivity());
            pingAllowanceText.setText(String.valueOf(savedAllowance));

            mContactsAdapter.changeCursor(mSqlHelper.getContactsToDisplay((MainActivity) getActivity()));
            ListView list = (ListView) mView.findViewById(R.id.contactsListView);
            list.setAdapter(mContactsAdapter);
        }
    }

    public void getLastKnownLocation() {
        mListener.getLastLoc();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i("AlertsFrag", "onSHaredPrefs called");
        if (mView != null) {
            mContactsAdapter.changeCursor(mSqlHelper.getContactsToDisplay((MainActivity) getActivity()));
            ListView list = (ListView) mView.findViewById(R.id.contactsListView);
            list.setAdapter(mContactsAdapter);

            if (key == "phone_number") {
                String newPhone = sharedPreferences.getString("phone_number", null);
                if (newPhone != null && PhoneNumberUtils.isGlobalPhoneNumber(newPhone)) {
                    mView.findViewById(R.id.FakeCallButton).setEnabled(true);
                } else {
                    // TODO: give this some toast listener or something to inform user to change settings
                    mView.findViewById(R.id.FakeCallButton).setEnabled(false);
                }
            }

            if (key == "pings_onoff_change") {
                Log.i("ALertsFrag", "Pings onoff was changed to: " + sharedPreferences.getString("pings_onoff_change", ""));
                // update toggle ui to match what's in the db for the current plan
                if (sharedPreferences.getString("pings_onoff_change", "").equals("true")) {

                    ((MainActivity) getActivity()).sendSMS("derp");
                    ((MainActivity) getActivity()).stopAlarm();
                    mSqlHelper.updatePingMisses((MainActivity)getActivity(), 0); // when alarm is turned off, reset miss count
                    Log.i("AlertsFrag", "after stop alarm");
                    updatePingsUIOnly();
                    Log.i("AlertsFrag", "post update pings ui");
                    sharedPreferences.edit().putString("pings_onoff_change", "false").apply();
                }
            }

            if (key == "checkin_change") {
                // update toggle ui to match what's in the db for the current plan
                if (sharedPreferences.getString("checkin_change", "").equals("true")) {
                    onCheckIn();
                    sharedPreferences.edit().putString("checkin_change", "false").apply();
                }
            }

            if(key == "exceeded_misses") {
                //send text message to emergency contacts
                if(!sharedPreferences.getString("exceeded_misses", "").equals("")) {
                    String planName = sharedPreferences.getString("exceeded_misses", "");
                    missedCheckinMessage(mSqlHelper.getContactNumbers(planName),
                            "I set up my NightOut app to ask me to check-in periodically "+
                            "tonight to keep me safe, but if you're getting this, I've missed "+
                            "too many check-ins and might be in trouble. Please help!");
                    ((MainActivity)getActivity()).stopAlarm();
                    sharedPreferences.edit().putString("exceeded_misses", "").apply();
                }
            }
        }

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAlertsFragmentInteractionListener {
        // TODO: Update argument type and name (you can rename listener/method)

        // send things in fragment to listener, which MainActivity extends
        public void OnAlertFragmentInteraction(Object object);
        public void getLastLoc();
        public void toggleSwitch(Switch s);
    }


// FUNCTIONAL TWILIO API CALL IF NEEDED - BUT FREE ACCOUNT IS TOO LIMITED

//            String twilioSid = "AC5c171e3338ffeb84902f0e75e2757648";
//             String twilioToken = "722687b62b430fd606d7009c8614b004";
//
//            String twilioCredentials = twilioSid + ":" + twilioToken;
//            String twilioBase64EncodedCredentials = Base64.encodeToString(twilioCredentials.getBytes(), Base64.NO_WRAP);
//
//
//            Ion.with(getActivity())
//                .load("https://api.twilio.com/2010-04-01/Accounts/" + twilioSid + "/Calls")
//                .addHeader("Authorization", "Basic " + twilioBase64EncodedCredentials)
//                .setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
//                .setBodyParameter("To", "(540) 446-4776")
//                .setBodyParameter("From", "(972) 999-7480")
//                .setBodyParameter("Url", "http://demo.twilio.com/docs/voice.xml")
//                .asString()
//                .setCallback(new FutureCallback<String>() {
//                    @Override
//                    public void onCompleted(Exception e, String result) {
//                        Log.d("ALERT FRAG", "completed post to fake call");
//                        if (e != null) {
//                            Log.d("ALERT FRAG ERR", e.getMessage());
//                        }
//                        if (result != null) {
//                            Log.d("ALERT FRAG RESULT", result.toString());
//                        }
//                        Log.d("ALERT FRAG", "done printing result");
//                    }
//                });

}
