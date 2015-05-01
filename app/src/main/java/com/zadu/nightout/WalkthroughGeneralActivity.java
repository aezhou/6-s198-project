package com.zadu.nightout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WalkthroughGeneralActivity extends ActionBarActivity {
    private LinearLayout mPhoneNumber;
    private LinearLayout mAddress;
    private TextView mOwnAddress;
    private TextView mOwnPhone;
    private Button mNextButton;


    private SharedPreferences preferences;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_general);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mOwnAddress = (TextView) findViewById(R.id.own_address);
        mOwnPhone = (TextView) findViewById(R.id.own_phone_number);

        mPhoneNumber = (LinearLayout) findViewById(R.id.phone_number_wrapper);
        mPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View enterPhoneNum = getLayoutInflater().inflate(R.layout.dialog_phone_number, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(WalkthroughGeneralActivity.this);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(WalkthroughGeneralActivity.this);
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

        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WalkthroughGeneralActivity.this,
                        WalkthroughContactsActivity.class);
                startActivity(intent);
            }
        });
    }
}
