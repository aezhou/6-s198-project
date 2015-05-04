package com.zadu.nightout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class WalkthroughContactsActivity extends ActionBarActivity{
    private SimpleCursorAdapter mAdapter;
    private MyOpenHelper mSqlHelper;
    private ListView mEmergencyListView;
    private Button mAddButton;
    private Button mNextButton;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSqlHelper = MyOpenHelper.getInstance(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_welcome_contacts);
        mEmergencyListView = (ListView) findViewById(R.id.emergency_contacts);
        Cursor c = mSqlHelper.getDefaultContacts();
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.list_item_settings_contact,
                c,
                new String[] { mSqlHelper.CONTACT_NAME, mSqlHelper.CONTACT_NUMBER },
                new int[] { R.id.contactNameTextView, R.id.contactDescriptionTextView },
                0);
        mEmergencyListView.setAdapter(mAdapter);
        mEmergencyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int i, long l) {
                if (mSqlHelper.getNumDefaultContacts() > 1) {
                    final View planDeleteView = getLayoutInflater().inflate(R.layout.dialog_contact_delete, null);

                    TextView nameView = (TextView) view.findViewById(R.id.contactNameTextView);
                    String name = nameView.getText().toString();
                    TextView text = (TextView) planDeleteView.findViewById(R.id.description);
                    String content = String.format("Deleting a default emergency contact will remove the contact " +
                            "from all plans. Are you sure you wish to delete %s from your default " +
                            "emergency contacts?", name);
                    text.setText(content);

                    AlertDialog.Builder builder = new AlertDialog.Builder(WalkthroughContactsActivity.this);
                    builder.setView(planDeleteView);

                    builder.setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    TextView numberView = (TextView) view.findViewById(R.id.contactDescriptionTextView);
                                    String number = numberView.getText().toString();
                                    mSqlHelper.deleteDefaultContact(number);

                                    mAdapter.changeCursor(mSqlHelper.getDefaultContacts());
                                    mEmergencyListView.setAdapter(mAdapter);
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
                } else {
                    Toast.makeText(getApplication(), "You must keep at least one emergency contact.", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        mAddButton = (Button) findViewById(R.id.add_new_contact);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setEnabled(preferences.getBoolean("added_first_contact", false));
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WalkthroughContactsActivity.this,
                        WalkthroughNamePlanActivity.class);
                startActivity(intent);
            }
        });
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

            Set<String> set = new HashSet<>();
            set.add(contactName);
            set.add(contactNumber);

            if (reqCode == 0) {
                if (contactName != null && contactNumber != null) {
                    mSqlHelper.insertDefaultContact(contactName, contactNumber);
                    mAdapter.changeCursor(mSqlHelper.getDefaultContacts());
                    mEmergencyListView.setAdapter(mAdapter);
                    preferences.edit().putBoolean("added_first_contact", true).apply();
                    mNextButton.setEnabled(true);
                }
            }
        }

    }
}
