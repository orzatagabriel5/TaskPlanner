package com.taskPlanner.activities;

import android.Manifest;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.radu.appointment.R;
import com.taskPlanner.Utils;
import com.taskPlanner.adapters.OldClientsAdapter;
import com.taskPlanner.database.DatabaseConnection;
import com.taskPlanner.database.model.Client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OldClientsActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_old_clients);

            //set toolbar actions
            android.support.v7.widget.Toolbar toolbar = findViewById(R.id.oldClientsToolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(this.getString(R.string.old_clients_activity_title));

            //populate old clients list
            Calendar last30Days = Calendar.getInstance();
            last30Days.add(Calendar.DAY_OF_MONTH, -30);
            List<Client> oldClientsList = DatabaseConnection.getInstance().clientDao().findClientsOlderThan(last30Days);

            OldClientsAdapter oldClientsAdapter = new OldClientsAdapter(this, oldClientsList);
            ListView oldClientsListView = findViewById(R.id.oldClientsListView);
            oldClientsListView.setAdapter(oldClientsAdapter);

            //request permision for call
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
            }
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
