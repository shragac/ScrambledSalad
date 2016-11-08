package com.studios.hell.scrambledsalad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    private void setPasswordTitle(TextView titleField) {
        if (this.prefs.getAll().isEmpty()) {
            titleField.setText(getString(R.string.new_password_title));
        }
        else {
            titleField.setText(getString(R.string.password_title));
        }
    }

    private void setPrefs() {
        this.prefs = this.getSharedPreferences(
                getString(R.string.system_prefs),
                Context.MODE_PRIVATE
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText passwordField = (EditText) findViewById(R.id.passwordField);
        final TextView titleField = (TextView) findViewById(R.id.titleField);
        titleField.setEnabled(false);

        this.setPrefs();
        this.setPasswordTitle(titleField);

        passwordField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged (Editable s){
                prefs = MainActivity.this.prefs;
                Editable password = passwordField.getText();
                if(password.length() == 4){
//                    Intent intent = new Intent(Intent.ACTION_MAIN);
//                    intent.addCategory(Intent.CATEGORY_HOME);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
                    if (prefs.getAll().isEmpty()) {
                        SharedPreferences.Editor prefs_editor = prefs.edit();
                        prefs_editor.putString(getString(R.string.password_key), password.toString());
                        prefs_editor.clear();
                        prefs_editor.apply();
                    }

                    if (password.toString().equals(prefs.getString(getString(R.string.password_key), ""))) {
                        Intent intent = new Intent(MainActivity.this, SecondaryActivity.class);
                        startActivity(intent);
                        password.clear();
                        titleField.setText(getString(R.string.password_title));
                    }
                    else {
                        titleField.setText(getString(R.string.wrong_password_message));
                        password.clear();
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }
}
