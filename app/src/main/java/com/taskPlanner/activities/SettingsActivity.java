package com.taskPlanner.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.radu.appointment.R;
import com.taskPlanner.Utils;
import com.taskPlanner.database.DatabaseConnection;
import com.taskPlanner.database.model.SMS;

public class SettingsActivity extends AppCompatActivity {

    TextView smsView = null;
    TextView smsInfoView = null;
    Switch enableSpeaker = null;
    Switch enableChrome = null;
    TextView chromeLinkView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);

            //set toolbar actions
            android.support.v7.widget.Toolbar toolbar = findViewById(R.id.settingsToolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(this.getString(R.string.settings_activity_title));

            SMS sms = DatabaseConnection.getInstance().smsDao().findLastSMS();
            String smsTemplate = sms.getTemplate();

            smsView = findViewById(R.id.smsField);
            smsView.setText(smsTemplate);

            smsInfoView = findViewById(R.id.smsInfoField);
            smsInfoView.setText(getString(R.string.settings_sms_info_text, getString(R.string.sms_name),
                    getString(R.string.sms_duration),
                    getString(R.string.sms_start),
                    getString(R.string.sms_price),
                    getString(R.string.sms_services),
                    getString(R.string.sms_notes)));

            //read speaker feature value
            enableSpeaker = findViewById(R.id.enableSpeakerId);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            String speakerFeature = preferences.getString("speaker_feature", "false");
            if(speakerFeature.equals("true")) {
                enableSpeaker.setChecked(true);
            }

            //read chrome feature value
            enableChrome = findViewById(R.id.enableChrome);
            String chromeFeature = preferences.getString("chrome_feature", "false");
            if(chromeFeature.equals("true")) {
                enableChrome.setChecked(true);
            }

            String chromeLink = preferences.getString("chrome_link", "https://www.google.ro");
            chromeLinkView = findViewById(R.id.chromeLinkView);
            chromeLinkView.setText(chromeLink);


        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }

    }

    public void saveSettings(View view){
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            final SharedPreferences.Editor editor = preferences.edit();

            //enable/disable speaker feature
            if(enableSpeaker.isChecked()){
                editor.putString("speaker_feature", "true");
            }else{
                editor.putString("speaker_feature", "false");
            }

            //enable/disable chrome feature;
            if(enableChrome.isChecked()){
                editor.putString("chrome_feature", "true");
            }else{
                editor.putString("chrome_feature", "false");
            }
            editor.putString("chrome_link", chromeLinkView.getText().toString());

            editor.commit();

            //set sms template
            SMS sms = new SMS();
            sms.setTemplate(smsView.getText().toString());
            DatabaseConnection.getInstance().smsDao().insertSMS(sms);

            Toast.makeText(this, this.getString(R.string.settings_save_success_notification), Toast.LENGTH_SHORT).show();
            finish();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }


    @Override
    public void onResume(){
        try{
            super.onResume();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    @Override
    public void onPause(){
        try{
            super.onPause();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    @Override
    public void onStart(){
        try{
            super.onStart();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    @Override
    public void onStop(){
        try{
            super.onStop();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }
}
