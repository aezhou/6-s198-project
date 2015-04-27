package com.zadu.nightout;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;

import java.util.HashSet;
import java.util.Set;

public class WalkthroughContactsSettingsActivity extends PreferenceActivity{
    Preference contact1;
    Preference contact2;
    Preference contact3;
    Preference contact4;
    Preference contact5;

    static final int PICK_CONTACT_1 = 1;
    static final int PICK_CONTACT_2 = 2;
    static final int PICK_CONTACT_3 = 3;
    static final int PICK_CONTACT_4 = 4;
    static final int PICK_CONTACT_5 = 5;

    private Button mButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_contacts);
        setContentView(R.layout.activity_welcome_contacts);

        mButton = (Button) findViewById(R.id.next_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WalkthroughContactsSettingsActivity.this,
                        WalkthroughNamePlanActivity.class);
                startActivity(intent);
            }
        });

        contact1 = findPreference("Contact1");
        contact2 = findPreference("Contact2");
        contact3 = findPreference("Contact3");
        contact4 = findPreference("Contact4");
        contact5 = findPreference("Contact5");

        contact1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT_1);
                return false;
            }
        });

        contact1.setTitle("Contact Not Set");
        contact1.setSummary("Click to Add Default Contact");

        contact2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT_2);
                return false;
            }
        });

        contact2.setTitle("Contact Not Set");
        contact2.setSummary("Click to Add Default Contact");

        contact3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT_3);
                return false;
            }
        });

        contact3.setTitle("Contact Not Set");
        contact3.setSummary("Click to Add Default Contact");

        contact4.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT_4);
                return false;
            }
        });

        contact4.setTitle("Contact Not Set");
        contact4.setSummary("Click to Add Default Contact");

        contact5.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT_5);
                return false;
            }
        });

        contact5.setTitle("Contact Not Set");
        contact5.setSummary("Click to Add Default Contact");


        bindPreferenceSummaryToValue(findPreference("Contact1"));
        bindPreferenceSummaryToValue(findPreference("Contact2"));
        bindPreferenceSummaryToValue(findPreference("Contact3"));
        bindPreferenceSummaryToValue(findPreference("Contact4"));
        bindPreferenceSummaryToValue(findPreference("Contact5"));
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (data != null) {
            Uri contactData = data.getData();
            String contactNumber = null;
            String contactName = null;

            if (resultCode == RESULT_OK) {
                Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                cursor.close();
            }

            MyOpenHelper sqlHelper = MyOpenHelper.getInstance(getApplication());

            Set<String> set = new HashSet<>();
            set.add(contactName);
            set.add(contactNumber);

            switch (reqCode) {
                case (PICK_CONTACT_1):
                    if (contactName != null && contactNumber != null) {
                        if (!contact1.getSummary().equals("Click to Add Default Contact")) {
                            sqlHelper.deleteDefaultContact(contact1.getSummary().toString());
                        }
                        contact1.setTitle(contactName);
                        contact1.setSummary(contactNumber);
                        sqlHelper.insertDefaultContact(contactName, contactNumber);
                    }
                    break;
                case (PICK_CONTACT_2):
                    if (contactName != null && contactNumber != null) {
                        if (!contact2.getSummary().equals("Click to Add Default Contact")) {
                            sqlHelper.deleteDefaultContact(contact2.getSummary().toString());
                        }
                        contact2.setTitle(contactName);
                        contact2.setSummary(contactNumber);
                        sqlHelper.insertDefaultContact(contactName, contactNumber);
                    }
                    break;
                case (PICK_CONTACT_3):
                    if (contactName != null && contactNumber != null) {
                        if (!contact3.getSummary().equals("Click to Add Default Contact")) {
                            sqlHelper.deleteDefaultContact(contact3.getSummary().toString());
                        }
                        contact3.setTitle(contactName);
                        contact3.setSummary(contactNumber);
                        sqlHelper.insertDefaultContact(contactName, contactNumber);
                    }
                    break;
                case (PICK_CONTACT_4):
                    if (contactName != null && contactNumber != null) {
                        if (!contact4.getSummary().equals("Click to Add Default Contact")) {
                            sqlHelper.deleteDefaultContact(contact4.getSummary().toString());
                        }
                        contact4.setTitle(contactName);
                        contact4.setSummary(contactNumber);
                        sqlHelper.insertDefaultContact(contactName, contactNumber);
                    }
                    break;
                case (PICK_CONTACT_5):
                    if (contactName != null && contactNumber != null) {
                        if (!contact5.getSummary().equals("Click to Add Default Contact")) {
                            sqlHelper.deleteDefaultContact(contact5.getSummary().toString());
                        }
                        contact5.setTitle(contactName);
                        contact5.setSummary(contactNumber);
                        sqlHelper.insertDefaultContact(contactName, contactNumber);
                    }
                    break;
            }
        }

    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("Contact1"));
            bindPreferenceSummaryToValue(findPreference("Contact2"));
            bindPreferenceSummaryToValue(findPreference("Contact3"));
            bindPreferenceSummaryToValue(findPreference("Contact4"));
            bindPreferenceSummaryToValue(findPreference("Contact5"));
        }
    }
}
