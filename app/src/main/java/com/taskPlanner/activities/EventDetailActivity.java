package com.taskPlanner.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.radu.appointment.R;
import com.taskPlanner.Utils;
import com.taskPlanner.adapters.DetailServiceAdapter;
import com.taskPlanner.database.DatabaseConnection;
import com.taskPlanner.database.model.Event;
import com.taskPlanner.database.model.Service;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    public static final int MY_EVENT_REQUEST_CODE = 5001;
    Event currentEvent = null;
    TextView nameField;
    TextView dateField;
    TextView timeFromField;
    TextView timeToField;
    ListView serviceListView;
    TextView priceField;
    TextView notesField;
    long eventId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_event_detail);

            //set toolbar actions
            android.support.v7.widget.Toolbar toolbar = findViewById(R.id.eventDetailToolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(this.getString(R.string.event_detail_activity_title));

            //read event details;
            readEvent();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    public void removeEvent(View view){
        if(currentEvent != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(this.getString(R.string.event_detail_remove_button_text));

            final Context context = this;
            // Add the buttons
            builder.setPositiveButton(this.getString(R.string.general_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent();
                    intent.putExtra("eventId", "" + currentEvent.getId());
                    intent.putExtra("mode","remove");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
            builder.setNegativeButton(this.getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            // Create the AlertDialog
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void editEvent(View view){
        Intent intent = new Intent(this, AddEventActivity.class);
        intent.putExtra("event_mode","edit");
        intent.putExtra("event_id", eventId);
        intent.putExtra("client_name", currentEvent.getClientName());
        intent.putExtra("client_phone", currentEvent.getClientPhoneNumber());
        intent.putExtra("event_date", dateField.getText().toString());
        intent.putExtra("event_time_from", timeFromField.getText().toString());
        intent.putExtra("event_time_to", timeToField.getText().toString());
        intent.putExtra("event_price", priceField.getText().toString());
        intent.putExtra("event_services", currentEvent.getServices());
        intent.putExtra("event_reminders", currentEvent.getReminders());
        intent.putExtra("event_notes", currentEvent.getNotes());

        String calStartTime = Utils.calendarToString(currentEvent.getStartDate());
        String calEndTime =  Utils.calendarToString(currentEvent.getEndDate());

        intent.putExtra("calStartTime", calStartTime);
        intent.putExtra("calEndTime", calEndTime);

        startActivityForResult(intent, MY_EVENT_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            readEvent();
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void readEvent(){
        eventId = getIntent().getLongExtra("event_id", 0);
        if(eventId != 0) {
            currentEvent = DatabaseConnection.getInstance().eventDao().findEventId((int) eventId);
            if (currentEvent != null) {
                nameField = findViewById(R.id.clientNameField);
                dateField = findViewById(R.id.dateField);
                timeFromField = findViewById(R.id.timeFromField);
                timeToField = findViewById(R.id.timeToField);
                serviceListView = findViewById(R.id.servicesListView);
                priceField = findViewById(R.id.priceField);
                notesField = findViewById(R.id.notesField);

                CheckBox reminder1 = findViewById(R.id.reminder1h);
                CheckBox reminder2 = findViewById(R.id.reminder2h);
                CheckBox reminder3 = findViewById(R.id.reminder3h);
                CheckBox reminder8 = findViewById(R.id.reminder8h);
                CheckBox reminder12 = findViewById(R.id.reminder12h);
                CheckBox reminder24 = findViewById(R.id.reminder24h);

                //read name
                nameField.setText(currentEvent.getClientName());

                //read dates
                dateField.setText(currentEvent.getStartDate().get(Calendar.DAY_OF_MONTH) + "/" + (currentEvent.getStartDate().get(Calendar.MONTH) + 1) + "/" + currentEvent.getStartDate().get(Calendar.YEAR));

                int hourOfDay = currentEvent.getStartDate().get(Calendar.HOUR_OF_DAY);
                int minute = currentEvent.getStartDate().get(Calendar.MINUTE);
                timeFromField.setText(Utils.getCorrectTime(hourOfDay, minute));

                hourOfDay = currentEvent.getEndDate().get(Calendar.HOUR_OF_DAY);
                minute = currentEvent.getEndDate().get(Calendar.MINUTE);
                timeToField.setText(Utils.getCorrectTime(hourOfDay, minute));

                //read services
                Type collectionService = new TypeToken<Collection<Service>>() {
                }.getType();
                List<Service> serviceList = new Gson().fromJson(currentEvent.getServices(), collectionService);

                DetailServiceAdapter serviceAdapter = new DetailServiceAdapter(this, serviceList);
                serviceListView.setAdapter(serviceAdapter);
                serviceListView.setDivider(null);
                Utils.setListViewHeightBasedOnChildren(serviceListView);

                //read price
                priceField.setText("" + currentEvent.getPrice());

                //read notes
                notesField.setText(currentEvent.getNotes());

                //read reminders
                Type collectionReminder = new TypeToken<Collection<Integer>>() {
                }.getType();
                List<Integer> reminderList = new Gson().fromJson(currentEvent.getReminders(), collectionReminder);

                for (Integer reminder : reminderList) {
                    if (reminder == 1) {
                        reminder1.setChecked(true);
                    }
                    if (reminder == 2) {
                        reminder2.setChecked(true);
                    }
                    if (reminder == 3) {
                        reminder3.setChecked(true);
                    }
                    if (reminder == 8) {
                        reminder8.setChecked(true);
                    }
                    if (reminder == 12) {
                        reminder12.setChecked(true);
                    }
                    if (reminder == 24) {
                        reminder24.setChecked(true);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
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
