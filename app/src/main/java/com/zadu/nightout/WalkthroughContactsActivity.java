package com.zadu.nightout;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.HashSet;
import java.util.Set;

public class WalkthroughContactsActivity extends ActionBarActivity{
    private SimpleCursorAdapter mAdapter;
    private ListView mEmergencyListView;
    private Button mAddButton;
    private Button mNextButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_contacts2);
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

        mAddButton = (Button) findViewById(R.id.add_new_contact);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);
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
                    //TODO: add to listview, db
                }
            }
        }

    }
}
