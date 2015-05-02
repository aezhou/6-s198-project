package com.zadu.nightout;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimerTask;
import java.util.prefs.PreferenceChangeListener;


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
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
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
        pingSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View toggleView) {
                onTogglePings(toggleView, v);
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
        // TODO: would be good to ensure at least one is always checked
        TextView nameView = (TextView) mView.findViewById(R.id.contactNameTextView);
        String name = nameView.getText().toString();
        TextView numberView = (TextView) mView.findViewById(R.id.contactDescriptionTextView);
        String number = numberView.getText().toString();
        mSqlHelper.checkPlanContactNumber((MainActivity) getActivity(), number, ((CheckBox) v).isChecked());
    }

    public void onTogglePings(View toggleView, View masterView) {
        Switch toggle = (Switch) toggleView;
        View detailView = masterView.findViewById(R.id.pingDetailsLayout);
        View offView = masterView.findViewById(R.id.pingOffLayout);

        mSqlHelper.updatePingsOnOff((MainActivity) getActivity(), true);

        if (toggle.isChecked()) {
            // TODO: turn on ping functionality
            Log.i("togglePings", "toggle is checked");
            detailView.setVisibility(View.VISIBLE);
            offView.setVisibility(View.GONE);

            //TODO: Cristhian
            int duration = mSqlHelper.getPingInterval((MainActivity)getActivity());
            ((MainActivity)getActivity()).setAlarm(duration);


        } else {
            Log.i("togglePings", "toggle off");
            detailView.setVisibility(View.GONE);
            offView.setVisibility(View.VISIBLE);
            mSqlHelper.updatePingsOnOff((MainActivity) getActivity(), false);
            // TODO: turn off ping functionality
            ((MainActivity)getActivity()).stopAlarm();
        }
    }

    public void onCheckIn() {
        // TODO: reset check-in timer
        Toast.makeText(getActivity(), "You Checked In!", Toast.LENGTH_SHORT).show();


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

        String smsto = "";
        for (String phoneNumber : getSetContactNumbers()) {
            smsto = smsto + phoneNumber + ";";
        }
        smsto = smsto.substring(0, smsto.length()-2); // remove trailing semicolon
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

    public void fakeCall() {
        Toast.makeText(getActivity(), "Fake Call!", Toast.LENGTH_SHORT).show();
        String fromNum = "(123) 456-7890";
        String toNum = "(540) 446-4776";
        new FakeCallTask().execute(fromNum, toNum);
    }

    private boolean isDummyContactSet = false;

    public void setDummyContact() {
        mSqlHelper.insertDefaultContact("Kristin", "5404464776");
    }

    @Override
    public void onPlanChanged() {
        if (mView != null) {
            Switch pingSwitch = (Switch) mView.findViewById(R.id.pingSwitch);
            boolean pingsOn = mSqlHelper.arePingsOn((MainActivity) getActivity());
            pingSwitch.setChecked(pingsOn);
            onTogglePings(pingSwitch, mView);

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
        if (mView != null) {
            mContactsAdapter.changeCursor(mSqlHelper.getContactsToDisplay((MainActivity) getActivity()));
            ListView list = (ListView) mView.findViewById(R.id.contactsListView);
            list.setAdapter(mContactsAdapter);
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
    }

    private class FakeCallTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... strings) {
            // TODO: make it do the thing
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {}

        protected void onPostExecute(String result) {}
    }
}
