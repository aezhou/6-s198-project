package com.zadu.nightout;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;


public class SettingsActivityTwo extends ActionBarActivity {
    private LinearLayout mPhoneNumber;
    private LinearLayout mAddress;
    private CursorAdapter mAdapter;
    private ListView mEmergencyListView;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_activity_two);

        mPhoneNumber = (LinearLayout) findViewById(R.id.phone_number_wrapper);
        mPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: open dialog
                Log.e("find me", "clicked phone number");
            }
        });

        mAddress = (LinearLayout) findViewById(R.id.home_address_wrapper);
        mAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: open dialog
                Log.e("find me", "clicked home address");
            }
        });

        mEmergencyListView = (ListView) findViewById(R.id.emergency_contacts);
        mEmergencyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO: delete item from list
                return false;
            }
        });

        mButton = (Button) findViewById(R.id.add_new_contact);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: launch contacts, save to list view
                Log.e("find me", "trying to add new contact");
            }
        });
    }
}
