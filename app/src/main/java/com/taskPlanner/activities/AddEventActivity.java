package com.taskPlanner.activities;


import android.annotation.TargetApi;
import android.app.Activity;
import android.arch.persistence.room.Transaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.radu.appointment.R;
import com.taskPlanner.Utils;
import com.taskPlanner.adapters.ContactAdapter;
import com.taskPlanner.adapters.EventServiceAdapter;
import com.taskPlanner.database.AppDatabase;
import com.taskPlanner.database.DatabaseConnection;
import com.taskPlanner.database.memory.ContactsInMemory;
import com.taskPlanner.database.model.Client;
import com.taskPlanner.database.model.Event;
import com.taskPlanner.database.model.Service;
import com.taskPlanner.pickers.DatePickerFragment;
import com.taskPlanner.pickers.TimePickerFragment;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class AddEventActivity extends AppCompatActivity {

    AutoCompleteTextView autoCompleteTextView = null;
    TextView dateView = null;
    TextView timeFromView = null;
    TextView timeToView = null;
    TextView priceField = null;
    TextView notesView = null;
    String selectedPhone = null;
    List<Service> eventServices = new ArrayList<Service>();
    String eventMode;
    Event event;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_event);

            // //read intent data
            Intent intent = getIntent();
            eventMode = intent.getStringExtra("event_mode");
            String currentDate = intent.getStringExtra("extra_current_date");

            //set toolbar actions
            android.support.v7.widget.Toolbar toolbar = findViewById(R.id.addEventToolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if (eventMode != null && eventMode.equals("edit")) {
                getSupportActionBar().setTitle(this.getString(R.string.edit_event_activity_title));
            } else {
                getSupportActionBar().setTitle(this.getString(R.string.add_event_activity_title));
            }

            //set default event time
            dateView = (TextView) findViewById(R.id.dateField);
            timeFromView = (TextView) findViewById(R.id.timeFromField);
            timeToView = (TextView) findViewById(R.id.timeToField);

            Calendar now = Calendar.getInstance();
            if (currentDate != null) {
                String clickedTime = intent.getStringExtra("extra_current_date");
                now = Utils.stringToCalendar(clickedTime);
            }
            dateView.setText(now.get(Calendar.DAY_OF_MONTH) + "/" + (now.get(Calendar.MONTH) + 1) + "/" + now.get(Calendar.YEAR));
            int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
            int minute = 0;
            timeFromView.setText(Utils.getCorrectTime(hourOfDay, minute));
            minute = 30;
            timeToView.setText(Utils.getCorrectTime(hourOfDay, minute));

            //populate clients in autocomplete
            final Activity activity = this;
            ContactAdapter contactAdapter = new ContactAdapter(this,
                    R.layout.clientlistitemlayout, ContactsInMemory.clientList);

            autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.clientNameField);
            autoCompleteTextView.setAdapter(contactAdapter);
            autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    autoCompleteTextView.showDropDown();
                }
            });
            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Client client = (Client) parent.getItemAtPosition(position);

                    autoCompleteTextView.setText(client.getName());
                    selectedPhone = client.getPhone();
                    Utils.hideKeyboard(activity);
                }
            });


            //set services dialog and updates
            final EventServiceAdapter serviceAdapter = new EventServiceAdapter(this, eventServices);

            final ListView listServicesView = findViewById(R.id.servicesListView);
            listServicesView.setAdapter(serviceAdapter);
            Utils.setListViewHeightBasedOnChildren(listServicesView);

            final Context context = this;
            Button addServiceButton = (Button) findViewById(R.id.attachServicesButton);
            addServiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getString(R.string.add_event_add_services_dialog_title));

                    final List<Integer> mSelectedItems = new ArrayList();

                    //Add content
                    final List<Service> serviceList = DatabaseConnection.getInstance().serviceDao().findAllServices();
                    String[] serviceListArray = new String[serviceList.size()];
                    boolean[] serviceSelectedArray = new boolean[serviceList.size()];

                    for (int i = 0; i < serviceList.size(); i++) {
                        serviceListArray[i] = serviceList.get(i).getName();
                    }

                    //check attached services
                    for (int i = 0; i < serviceList.size(); i++) {
                        for (int j = 0; j < eventServices.size(); j++) {
                            if (serviceList.get(i).getId() == eventServices.get(j).getId()) {
                                serviceSelectedArray[i] = true;
                                mSelectedItems.add(i);
                            }
                        }
                    }

                    builder.setMultiChoiceItems(serviceListArray, serviceSelectedArray, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            // If the user checked the item, add it to the selected items
                            if (isChecked) {
                                mSelectedItems.add(which);
                            } else if (mSelectedItems.contains(which)) {
                                // Else, if the item is already in the array, remove it
                                mSelectedItems.remove(Integer.valueOf(which));
                            }
                        }
                    });

                    // Add the buttons
                    builder.setPositiveButton(context.getString(R.string.general_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //attach services to event
                            eventServices.clear();
                            for (int i = 0; i < mSelectedItems.size(); i++) {
                                eventServices.add(serviceList.get(mSelectedItems.get(i)));
                            }
                            serviceAdapter.notifyDataSetChanged();
                            Utils.setListViewHeightBasedOnChildren(listServicesView);

                            //modify time to reflect all services
                            TextView timeFromView = findViewById(R.id.timeFromField);
                            TextView timeToView = findViewById(R.id.timeToField);
                            int hourOfDayFromInt = Integer.valueOf(timeFromView.getText().toString().split(":")[0]);
                            int minuteFromInt = Integer.valueOf(timeFromView.getText().toString().split(":")[1]);

                            int hourOfDayToInt = hourOfDayFromInt;
                            int minuteToInt = minuteFromInt;

                            int addedTime = 0;
                            if (eventServices != null && eventServices.size() > 0) {
                                for (Service service : eventServices) {
                                    addedTime += service.getDuration();
                                }
                            }
                            if (addedTime != 0) {
                                hourOfDayToInt += addedTime / 60;
                                minuteToInt += addedTime % 60;

                                if (minuteToInt / 60 != 0) {
                                    hourOfDayToInt += (minuteToInt / 60);
                                    minuteToInt = minuteToInt % 60;
                                }
                            } else {
                                minuteToInt += 10;
                                if (minuteToInt / 60 != 0) {
                                    hourOfDayToInt++;
                                    minuteToInt = minuteToInt % 60;
                                }
                            }

                            if (hourOfDayToInt >= 24) {
                                hourOfDayToInt = 23;
                                minuteToInt = 59;
                                int timeFromMinutes = hourOfDayToInt * 60 + minuteToInt - addedTime;
                                int newHour = timeFromMinutes / 60;
                                int newMinute = timeFromMinutes % 60;
                                if (hourOfDayFromInt == newHour && minuteFromInt - newMinute <= 10) {
                                    timeFromView.setText(Utils.getCorrectTime(hourOfDayFromInt, minuteFromInt - 10));
                                } else {
                                    timeFromView.setText(Utils.getCorrectTime(newHour, newMinute));
                                }
                            }
                            timeToView.setText(Utils.getCorrectTime(hourOfDayToInt, minuteToInt));

                            //modify price to reflect services
                            int totalPrice = 0;
                            for (Service service : eventServices) {
                                totalPrice += service.getPrice();
                            }
                            priceField.setText("" + totalPrice);
                        }
                    });
                    builder.setNegativeButton(context.getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

                    // Create the AlertDialog
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            //get reference for priceField
            priceField = findViewById(R.id.priceField);

            //get reference for notesField
            notesView = findViewById(R.id.notesView);

            if (eventMode != null && eventMode.equals("edit")) {

                autoCompleteTextView.setText(intent.getStringExtra("client_name"));
                selectedPhone = intent.getStringExtra("client_phone");
                dateView.setText(intent.getStringExtra("event_date"));
                timeFromView.setText(intent.getStringExtra("event_time_from"));
                timeToView.setText(intent.getStringExtra("event_time_to"));
                priceField.setText(intent.getStringExtra("event_price"));
                notesView.setText(intent.getStringExtra("event_notes"));

                //read services
                Type collectionService = new TypeToken<Collection<Service>>() {
                }.getType();
                List<Service> serviceList = new Gson().fromJson(intent.getStringExtra("event_services"), collectionService);
                eventServices.clear();
                for (Service service : serviceList) {
                    eventServices.add(service);
                }
                serviceAdapter.notifyDataSetChanged();
                Utils.setListViewHeightBasedOnChildren(listServicesView);

                //read reminders
                Type collectionReminder = new TypeToken<Collection<Integer>>() {
                }.getType();
                List<Integer> reminderList = new Gson().fromJson(intent.getStringExtra("event_reminders"), collectionReminder);

                CheckBox reminder1 = findViewById(R.id.reminder1h);
                CheckBox reminder2 = findViewById(R.id.reminder2h);
                CheckBox reminder3 = findViewById(R.id.reminder3h);
                CheckBox reminder8 = findViewById(R.id.reminder8h);
                CheckBox reminder12 = findViewById(R.id.reminder12h);
                CheckBox reminder24 = findViewById(R.id.reminder24h);

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
        }catch(Exception e){
                Log.e("error", e.getMessage());
                Utils.sendEmail(this, e);
        }
    }

    public void saveEvent(View v){
        try {
            AppDatabase dbConnection = DatabaseConnection.getInstance();
            event = new Event();
            if (eventMode != null && eventMode.equals("edit")) {
                long eventId = getIntent().getLongExtra("event_id", 0);
                event = dbConnection.eventDao().findEventId((int) eventId);
            }

            //validate if name is selected from list
            boolean validName = false;
            String clientName = autoCompleteTextView.getText().toString();
            for (Client client : ContactsInMemory.clientList) {
                if (clientName.equals(client.getName()) && selectedPhone != null && selectedPhone.equals(client.getPhone())) {
                    validName = true;
                }
            }
            if (!validName) {
                Toast.makeText(this, this.getString(R.string.add_event_error_name_not_selected), Toast.LENGTH_SHORT).show();
                return;
            }


            event.setClientName(clientName);
            event.setClientPhoneNumber(selectedPhone);

            //set start and end date
            String eventDate = dateView.getText().toString();
            String timeFrom = timeFromView.getText().toString();
            String timeTo = timeToView.getText().toString();

            //validate start time between 8 and 22
            boolean validTime = false;
            int timeFromHour = Integer.parseInt(timeFrom.split(":")[0]);
            if (timeFromHour >= 8 && timeFromHour < 22) {
                validTime = true;
            }
            if (!validTime) {
                Toast.makeText(this, this.getString(R.string.add_event_error_time_interval), Toast.LENGTH_SHORT).show();
                return;
            }

            event.setNotes(notesView.getText().toString());
            event.setStartDate(Utils.stringToCalendar(eventDate + " " + timeFrom));
            event.setEndDate(Utils.stringToCalendar(eventDate + " " + timeTo));
            event.setPrice(Integer.parseInt(priceField.getText().toString()));

            //set services
            String services = new Gson().toJson(eventServices);
            event.setServices(services);

            //set reminders
            CheckBox reminder1 = findViewById(R.id.reminder1h);
            CheckBox reminder2 = findViewById(R.id.reminder2h);
            CheckBox reminder3 = findViewById(R.id.reminder3h);
            CheckBox reminder8 = findViewById(R.id.reminder8h);
            CheckBox reminder12 = findViewById(R.id.reminder12h);
            CheckBox reminder24 = findViewById(R.id.reminder24h);

            List<Integer> reminderList = new ArrayList<Integer>();
            if (reminder1.isChecked())
                reminderList.add(1);
            if (reminder2.isChecked())
                reminderList.add(2);
            if (reminder3.isChecked())
                reminderList.add(3);
            if (reminder8.isChecked())
                reminderList.add(8);
            if (reminder12.isChecked())
                reminderList.add(12);
            if (reminder24.isChecked())
                reminderList.add(24);

            String reminderString = new Gson().toJson(reminderList);
            event.setReminders(reminderString);

            Intent intent = new Intent();
            if (eventMode != null && eventMode.equals("edit")) {
                dbConnection.eventDao().updateEvent(event);
                //update client once with event
                Client client = dbConnection.clientDao().findClientByName(event.getClientName());
                if (client != null) {
                    if (client.getLastEventDate().compareTo(event.getStartDate()) <= 0) {
                        client.setLastEventDate(event.getStartDate());
                        client.setPhone(event.getClientPhoneNumber());
                        dbConnection.clientDao().updateClient(client);
                    }
                }

                Toast.makeText(this, this.getString(R.string.edit_event_success_notification), Toast.LENGTH_SHORT).show();
                intent.putExtra("mode", "edit");
                intent.putExtra("eventId", "" + event.getId());
                intent.putExtra("calStartTime", getIntent().getStringExtra("calStartTime"));
                intent.putExtra("calEndTime", getIntent().getStringExtra("calEndTime"));
            } else {
                //check for double event register
                List<Event> events = dbConnection.eventDao().findEventsByAllFields(event.getClientName(), event.getClientPhoneNumber(), event.getStartDate(), event.getEndDate(), event.getServices(), event.getReminders(), event.getPrice());
                if (events != null && events.size() > 0) {
                    finish();
                    return;
                }

                String eventUUID = UUID.randomUUID().toString().replaceAll("-", "");
                event.setEventId(eventUUID);
                long eventId = dbConnection.eventDao().insertEvent(event);

                //add client once with event
                Client client = dbConnection.clientDao().findClientByName(event.getClientName());
                if (client == null) {
                    client = new Client();
                    client.setName(event.getClientName());
                    client.setPhone(event.getClientPhoneNumber());
                    client.setLastEventDate(event.getStartDate());

                    dbConnection.clientDao().insertClient(client);
                } else {
                    if (client.getLastEventDate().compareTo(event.getStartDate()) <= 0) {
                        client.setPhone(event.getClientPhoneNumber());
                        client.setLastEventDate(event.getStartDate());
                        dbConnection.clientDao().updateClient(client);
                    }
                }
                Toast.makeText(this, this.getString(R.string.add_event_success_notification), Toast.LENGTH_SHORT).show();
                intent.putExtra("mode", "save");
                intent.putExtra("eventId", "" + eventId);
            }

            CheckBox reminderInstant = findViewById(R.id.reminderInstant);
            if (reminderInstant.isChecked()) {
                String smsText = Utils.formatSMS(event, this);
                SmsManager smsManager = SmsManager.getDefault();
                ArrayList<String> parts = smsManager.divideMessage(smsText);
                smsManager.sendMultipartTextMessage(event.getClientPhoneNumber(), null, parts, null, null);
            }

            setResult(RESULT_OK, intent);
            finish();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    public void showTimePickerFromDialog(View v) {
        try {
            DialogFragment newFragment = new TimePickerFragment(timeFromView, timeToView, eventServices);
            newFragment.show(getSupportFragmentManager(), "timePicker");
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    public void showDatePickerDialog(View v) {
        try {
            DialogFragment newFragment = new DatePickerFragment(dateView);
            newFragment.show(getSupportFragmentManager(), "datePicker");
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

