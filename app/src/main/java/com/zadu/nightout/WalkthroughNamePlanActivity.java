package com.zadu.nightout;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class WalkthroughNamePlanActivity extends Activity {
    private Button mButton;
    private EditText mEditText;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_new_plan);

        mEditText = (EditText) findViewById(R.id.new_name);

        mButton = (Button) findViewById(R.id.finish_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MyOpenHelper sqlHelper = MyOpenHelper.getInstance(getApplication());
                sqlHelper.insertNewPlan(mEditText.getText().toString());
                sqlHelper.deletePlan("My First Plan");

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WalkthroughNamePlanActivity.this);
                preferences.edit().putString("first_time", "false").apply();
                Intent intent = new Intent(WalkthroughNamePlanActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (charSequence.length() > 0) {
                    mButton.setEnabled(true);
                } else {
                    mButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }
}
