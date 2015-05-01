package com.zadu.nightout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;


public class SettingsActivityTwo extends ActionBarActivity {
    private LinearLayout mPhoneNumber;
    private LinearLayout mAddress;
    private TextView mOwnAddress;
    private TextView mOwnPhone;
    private SimpleCursorAdapter mAdapter;
    private ListView mEmergencyListView;
    private Button mButton;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_activity_two);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mOwnAddress = (TextView) findViewById(R.id.own_address);
        mOwnAddress.setText(preferences.getString("home_address", getString(R.string.pref_default_display_address)));
        mOwnPhone = (TextView) findViewById(R.id.own_phone_number);
        mOwnPhone.setText(preferences.getString("phone_number", getString(R.string.pref_default_display_phone)));

        mPhoneNumber = (LinearLayout) findViewById(R.id.phone_number_wrapper);
        mPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View enterPhoneNum = getLayoutInflater().inflate(R.layout.dialog_phone_number, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivityTwo.this);
                builder.setView(enterPhoneNum);

                EditText phone = (EditText) enterPhoneNum.findViewById(R.id.new_phone_num);

                builder.setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText phone = (EditText) enterPhoneNum.findViewById(R.id.new_phone_num);
                                String newNumber = phone.getText().toString();
                                preferences.edit().putString("phone_number", newNumber).apply();
                                mOwnPhone.setText(newNumber);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                final AlertDialog alert = builder.create();
                alert.show();

                if(phone.getText().toString().isEmpty()) {
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }

                phone.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (editable.length() != 0 && PhoneNumberUtils.isGlobalPhoneNumber(editable.toString())) {
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        } else {
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }
                    }
                });
            }
        });

        mAddress = (LinearLayout) findViewById(R.id.home_address_wrapper);
        mAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View enterHomeAddress = getLayoutInflater().inflate(R.layout.dialog_home_address, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivityTwo.this);
                builder.setView(enterHomeAddress);

                AutoCompleteTextView address = (AutoCompleteTextView)
                        enterHomeAddress.findViewById(R.id.searchField);

                //TODO: @Cristhian set up autocomplete


                builder.setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AutoCompleteTextView address = (AutoCompleteTextView)
                                        enterHomeAddress.findViewById(R.id.searchField);
                                String newAddress = address.getText().toString();
                                preferences.edit().putString("home_address", newAddress).apply();
                                mOwnAddress.setText(newAddress);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                final AlertDialog alert = builder.create();
                alert.show();

                if(address.getText().toString().isEmpty()) {
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }

                address.addTextChangedListener(new TextWatcher() {
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
        });

        mEmergencyListView = (ListView) findViewById(R.id.emergency_contacts);
        //TODO: initialize mAdapter (view is R.layout.list_item_settings_contact)
        mEmergencyListView.setAdapter(mAdapter);
        mEmergencyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO: delete item from listview and db
                return false;
            }
        });

        mButton = (Button) findViewById(R.id.add_new_contact);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, 0);
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
                    //TODO: add to listview, db
                }
            }
        }

    }
}