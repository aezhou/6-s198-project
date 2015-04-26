package com.zadu.nightout;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.zadu.nightout.AlertsFragment.OnAlertsFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AlertsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlertsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SimpleCursorAdapter mDefaultContactsAdapter;
    private MyOpenHelper mSqlHelper;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_alerts, container, false);

        ListView list = (ListView) v.findViewById(R.id.contactsListView);
        Cursor c = getDefaultContacts();
        isDummyContactSet = c.moveToFirst();
        if (!isDummyContactSet) {
            setDummyContact();
            isDummyContactSet = true;
        }
        mDefaultContactsAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.list_item_contact,
                c,
                new String[] { mSqlHelper.CONTACT_NAME, mSqlHelper.CONTACT_NUMBER },
                new int[] { R.id.contactNameTextView, R.id.contactDescriptionTextView },
                0);

        list.setAdapter(mDefaultContactsAdapter);
        final View.OnClickListener contactsCheckListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              onContactChecked((CheckBox) v, true);
            }
        };
        mDefaultContactsAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.contactCheckBox) {
                    view.setOnClickListener(contactsCheckListener);
                }
                return false;
            }
        });

        // TODO: set up "other" contacts list view
        // TODO: set up listeners for checkboxes on either view

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

        Button otherContactButton = (Button) v.findViewById(R.id.otherContactButton);
        otherContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOtherContact();
            }
        });

        // set button and switch listeners

        // TODO: make ping options editable (mins between and num missed)
        Switch pingSwitch = (Switch) v.findViewById(R.id.pingSwitch);
        pingSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTogglePings(v);
            }
        });

        TextView pingIntervalText = (TextView) v.findViewById(R.id.pingIntervalText);
        pingIntervalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String initValue = ((TextView) v).getText().toString();
                ((MainActivity) getActivity()).showPingIntervalDialog(initValue, (TextView) v);
            }
        });
        TextView pingAllowanceText = (TextView) v.findViewById(R.id.pingAllowanceText);
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

        // set texting button listeners

        Button ThereSafeButton = (Button) v.findViewById(R.id.ThereSafeButton);
        ThereSafeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessageButton("I made it there safe!");
            }
        });

        Button HomeSafeButton = (Button) v.findViewById(R.id.HomeSafeButton);
        HomeSafeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessageButton("I made it home safe!");
            }
        });

        Button AllClearButton = (Button) v.findViewById(R.id.AllClearButton);
        AllClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessageButton("I'm safe! You can ignore my previous messages.");
            }
        });

        Button GetMeButton = (Button) v.findViewById(R.id.GetMeButton);
        GetMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMessageButton("Come get me ASAP.");
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
                onMessageButton("I'm in danger! HELP!");
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSqlHelper = new MyOpenHelper(getActivity());
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

    public void addOtherContact() {
        // TODO: open contacts to select one
        // TODO: add result to plan_contacts db as non-default
        // TODO: update other contacts list view
    }

    public void onContactChecked(CheckBox v, boolean isDefault) {
        TextView nameView = (TextView) v.findViewById(R.id.contactNameTextView);
        String name = nameView.getText().toString();
        TextView numberView = (TextView) v.findViewById(R.id.contactDescriptionTextView);
        String number = nameView.getText().toString();
        if (v.isChecked()) {
            // they just checked it, so add to plan contacts
            mSqlHelper.setPlanContactNumber((MainActivity) getActivity(),
                    name, number, isDefault);
        } else {
            // they just unchecked it, so delete from plan contacts
            mSqlHelper.removePlanContactNumber((MainActivity) getActivity(), number);
            if (!isDefault) {
                // TODO: could remove this from other contacts list then
            }
        }
    }

    public void onTogglePings(View view) {
        Switch toggle = (Switch) view;
        View detailView = getActivity().findViewById(R.id.pingDetailsLayout);
        if (toggle.isChecked()) {
            detailView.setVisibility(View.VISIBLE);
            mSqlHelper.updatePingsOnOff((MainActivity) getActivity(), true);
            // TODO: turn on ping functionality
        } else {
            detailView.setVisibility(View.INVISIBLE);
            mSqlHelper.updatePingsOnOff((MainActivity) getActivity(), false);
            // TODO: turn off ping functionality
        }
    }

    public void onCheckIn() {
        // TODO: reset check-in timer
        Toast.makeText(getActivity(), "You Checked In!", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<String> getSetContactNumbers() {
        return mSqlHelper.getContactNumbers((MainActivity) getActivity());
    }

    public void onMessageButton(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

        if (getSetContactNumbers().size() == 0) {
            Toast.makeText(getActivity(), "No contact numbers to send to!", Toast.LENGTH_SHORT).show();
            return;
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
        // TODO: use API to make a fake call
        Toast.makeText(getActivity(), "Fake Call!", Toast.LENGTH_SHORT).show();
    }

    private boolean isDummyContactSet = false;

    public void setDummyContact() {
        mSqlHelper.insertDefaultContact("Kristin", "15404464776");
    }

    public Cursor getDefaultContacts() {
        SQLiteDatabase db = mSqlHelper.getReadableDatabase();
        return db.rawQuery("select * from contacts", null);
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
    }
}
