package com.taskPlanner.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.persistence.room.Database;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.RectF;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.radu.appointment.R;
import com.taskPlanner.SendSMSAlarm;
import com.taskPlanner.Utils;
import com.taskPlanner.weekView.MonthLoader;
import com.taskPlanner.weekView.WeekView;
import com.taskPlanner.weekView.WeekViewEvent;
import com.taskPlanner.database.DatabaseConnection;
import com.taskPlanner.database.dao.EventDao;
import com.taskPlanner.database.memory.ContactsInMemory;
import com.taskPlanner.database.model.Client;
import com.taskPlanner.database.model.Event;
import com.taskPlanner.database.model.SMS;
import com.taskPlanner.database.model.Service;

import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    public static final int MY_EVENT_REQUEST_CODE = 5002;
    public static final int MY_PERMISSIONS_MULTIPLE_REQUEST = 5000;

    WeekView weekView = null;
    private ArrayList<WeekViewEvent> weekEvents;
    CalendarView monthView = null;
    ImageButton speakerToogle;

    com.google.api.services.calendar.Calendar mService;
    GoogleAccountCredential credential;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    Event googleCalendarEvent;
    Calendar googleCalendarEventStartTime;
    Calendar googleCalendarEventEndTime;
    String eventMode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);
            final Context context = this;
            final Activity activity = this;

            //Utils.insertTestEvents(this, 100);

            //check all permisions needed
            checkPermissions();

            //configure navigation view
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            // Get a reference for the week view in the layout.
            weekView = (WeekView) findViewById(R.id.weekView);
            weekEvents = new ArrayList<WeekViewEvent>();

            // Set an action when any event is clicked.
            WeekView.EventClickListener mEventClickListener = new WeekView.EventClickListener() {
                @Override
                public void onEventClick(WeekViewEvent event, RectF eventRect) {
                    Intent intent = new Intent(context, EventDetailActivity.class);
                    intent.putExtra("event_id", event.getId());
                    startActivityForResult(intent, MY_EVENT_REQUEST_CODE);
                }
            };
            weekView.setOnEventClickListener(mEventClickListener);

            //Set action when event is long pressed
            WeekView.EventLongPressListener mEventLongPressListener = new WeekView.EventLongPressListener() {
                @Override
                public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
                    Intent intent = new Intent(context, AddEventActivity.class);
                    intent.putExtra("extra_current_date", Utils.calendarToString(event.getStartTime()));
                    startActivityForResult(intent, MY_EVENT_REQUEST_CODE);
                }
            };
            weekView.setEventLongPressListener(mEventLongPressListener);

            // The week view has infinite scrolling horizontally. We have to provide the events of a
            // month every time the month changes on the week view.
            final EventDao eventDao = DatabaseConnection.getInstance().eventDao();
            MonthLoader.MonthChangeListener mMonthChangeListener = new MonthLoader.MonthChangeListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                    weekEvents = new ArrayList<WeekViewEvent>();
                    // Get the starting point and ending point of the given month. We need this to find the
                    // events of the given month.
                    Calendar startOfMonth = Calendar.getInstance();
                    startOfMonth.set(Calendar.YEAR, newYear);
                    startOfMonth.set(Calendar.MONTH, newMonth - 1);
                    startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
                    startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
                    startOfMonth.set(Calendar.MINUTE, 0);
                    startOfMonth.set(Calendar.SECOND, 0);
                    startOfMonth.set(Calendar.MILLISECOND, 0);
                    Calendar endOfMonth = (Calendar) startOfMonth.clone();
                    endOfMonth.add(Calendar.MONTH, 1);
                    endOfMonth.set(Calendar.DATE, 1);
                    endOfMonth.add(Calendar.DATE, -1);
                    endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
                    endOfMonth.set(Calendar.MINUTE, 59);
                    endOfMonth.set(Calendar.SECOND, 59);

                    List<Event> eventList = eventDao.findEventsBetween(startOfMonth, endOfMonth);

                    for (Event e : eventList) {
                        Type collectionService = new TypeToken<Collection<Service>>() {
                        }.getType();
                        List<Service> serviceList = new Gson().fromJson(e.getServices(), collectionService);
                        int totalDuration = 0;

                        for (Service service : serviceList) {
//                           Utils.updateColors(service);

                           totalDuration += service.getDuration();
                        }

//                        e.setServices( new Gson().toJson(serviceList));
//                        DatabaseConnection.getInstance().eventDao().updateEvent(e);


                        WeekViewEvent weekEvent = new WeekViewEvent();
                        weekEvent.setId(e.getId());
                        weekEvent.setName(e.getClientName());
                        weekEvent.setStartTime(e.getStartDate());
                        weekEvent.setEndTime(e.getEndDate());
                        weekEvent.setTotalDuration(totalDuration);
                        weekEvent.setServiceList(serviceList);
                        weekEvents.add(weekEvent);
                    }
                    // Find the events that were added by tapping on empty view and that occurs in the given
                    // time frame.
                    return weekEvents;
                }
            };
            weekView.setMonthChangeListener(mMonthChangeListener);
            weekView.goToHour(8);

            //click empty view listener
            WeekView.EmptyViewClickListener mEmptyViewClickListener = new WeekView.EmptyViewClickListener() {
                @Override
                public void onEmptyViewClicked(Calendar time) {
                    Intent intent = new Intent(context, AddEventActivity.class);
                    intent.putExtra("extra_current_date", Utils.calendarToString(time));
                    startActivityForResult(intent, MY_EVENT_REQUEST_CODE);
                }
            };
            weekView.setEmptyViewClickListener(mEmptyViewClickListener);

            //configure date and hour format
            DateTimeInterpreter dateTimeInterpreter = new DateTimeInterpreter() {
                @Override
                public String interpretDate(Calendar date) {
                    SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                    String weekday = weekdayNameFormat.format(date.getTime());

                    SimpleDateFormat format = null;
                    if (date.get(Calendar.DAY_OF_MONTH) < 10 && date.get(Calendar.MONTH) + 1 < 10) {
                        format = new SimpleDateFormat("0d/0M", Locale.getDefault());
                    } else if (date.get(Calendar.DAY_OF_MONTH) < 10 && date.get(Calendar.MONTH) + 1 >= 10) {
                        format = new SimpleDateFormat("0d/M", Locale.getDefault());
                    } else if (date.get(Calendar.DAY_OF_MONTH) >= 10 && date.get(Calendar.MONTH) + 1 < 10) {
                        format = new SimpleDateFormat("d/0M", Locale.getDefault());
                    } else {
                        format = new SimpleDateFormat("d/M", Locale.getDefault());
                    }

                    if (weekView.getNumberOfVisibleDays() == 7) {
                        return format.format(date.getTime());
                    } else {
                        return weekday.toUpperCase() + " " + format.format(date.getTime());
                    }
                }

                @Override
                public String interpretTime(int hour) {
                    if (hour == 24) hour = 0;
                    if (hour == 0) hour = 0;
                    return hour + ":00";
                }
            };
            weekView.setDateTimeInterpreter(dateTimeInterpreter);

            //configure first sms template
            if (DatabaseConnection.getInstance().smsDao().findLastSMS() == null) {
                SMS sms = new SMS();
                sms.setTemplate(getString(R.string.sms_template, getString(R.string.sms_name), getString(R.string.sms_start)));
                DatabaseConnection.getInstance().smsDao().insertSMS(sms);
            }

            //configure google authentication for Calendar API
            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            credential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff())
                    .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getString(R.string.app_name))
                    .build();

            //set month view listener
            monthView = findViewById(R.id.monthCalendarView);
            monthView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                    try{
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        view.setVisibility(View.INVISIBLE);
                        weekView.setVisibility(View.VISIBLE);
                        weekView.setNumberOfVisibleDays(1);
                        weekView.goToDate(selectedDate);
                    }catch(Exception e){
                        Log.e("error", e.getMessage());
                        Utils.sendEmail(null, e);
                    }
                }
            });

        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS
                + ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)
                +  ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE,
                                                                        Manifest.permission.READ_CONTACTS,
                                                                        Manifest.permission.SEND_SMS,
                                                                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_MULTIPLE_REQUEST);
        }else{
            readContacts();
            startSMSAlarm();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try{
            switch (requestCode) {
                case MY_PERMISSIONS_MULTIPLE_REQUEST:
                    if (grantResults.length > 0) {
                        boolean read_phone_state = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        boolean read_contacts = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                        boolean send_sms = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                        boolean modify_audio = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                        if (read_contacts) {
                            readContacts();
                        }
                        if (send_sms) {
                            startSMSAlarm();
                        }

                        if(read_phone_state){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (!Settings.canDrawOverlays(this)) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getApplicationContext().getPackageName()));
                                    startActivity(intent);
                                }
                            }
                        }
                    }
            }
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    //update calendar after creating an event
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        try{
            super.onActivityResult(requestCode, resultCode, data);
            switch(requestCode) {
                case MY_EVENT_REQUEST_CODE:
                    if(resultCode == RESULT_OK){
                        //set data for google calendar api
                        if(data!= null && data.getStringExtra("mode") != null){
                            eventMode = data.getStringExtra("mode");
                            int eventId = Integer.parseInt(data.getStringExtra("eventId"));
                            googleCalendarEvent = DatabaseConnection.getInstance().eventDao().findEventId(eventId);
                            if(eventMode.equals("edit")){
                                googleCalendarEventStartTime = Utils.stringToCalendar(data.getStringExtra("calStartTime"));
                                googleCalendarEventEndTime = Utils.stringToCalendar(data.getStringExtra("calEndTime"));
                            }else if(eventMode.equals("remove")){
                                DatabaseConnection.getInstance().eventDao().removeEvent(googleCalendarEvent);
                            }
                            changeGoogleCalendarData();
                            weekView.notifyDatasetChanged();
                            weekView.goToHour(8);
                        }
                    }
                    break;
                case REQUEST_GOOGLE_PLAY_SERVICES:
                    if (resultCode == RESULT_OK) {
                        changeGoogleCalendarData();
                    } else {
                        isGooglePlayServicesAvailable();
                    }
                    break;
                case REQUEST_ACCOUNT_PICKER:
                    if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            credential.setSelectedAccountName(accountName);
                            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(PREF_ACCOUNT_NAME, accountName);
                            editor.commit();
                            changeGoogleCalendarData();
                        }
                    } else if (resultCode == RESULT_CANCELED) {
                    }
                    break;
                case REQUEST_AUTHORIZATION:
                    if (resultCode == RESULT_OK) {
                        changeGoogleCalendarData();
                    } else {
                        chooseAccount();
                    }
                    break;

            }
            super.onActivityResult(requestCode, resultCode, data);
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    private void readContacts(){
        if(ContactsInMemory.clientList.size() == 0) {
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
            Set<Client> clientSet = new HashSet<Client>();
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneNumber = phoneNumber.trim().replace("^004", "")
                        .replaceAll("\\+4", "")
                        .replaceAll("\\(", "")
                        .replaceAll("\\)", "")
                        .replaceAll("-", "")
                        .replaceAll(" ", "");

                Client client = new Client();
                client.setName(name);
                client.setPhone(phoneNumber);
                clientSet.add(client);
            }
            ContactsInMemory.clientList.addAll(clientSet);

            phones.close();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        try{
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            monthView.setVisibility(View.INVISIBLE);
            weekView.setVisibility(View.VISIBLE);

            if (id == R.id.select1day) {
                weekView.setNumberOfVisibleDays(1);
                weekView.goToHour(8);
                weekView.notifyDatasetChanged();
            } else if (id == R.id.select3day) {
                weekView.setNumberOfVisibleDays(3);
                weekView.goToHour(8);
                weekView.notifyDatasetChanged();
            } else if (id == R.id.select7day) {
                weekView.setNumberOfVisibleDays(7);
                weekView.goToHour(8);
                weekView.notifyDatasetChanged();
            }else if (id == R.id.selectMonth) {
                weekView.setVisibility(View.INVISIBLE);
                monthView.setVisibility(View.VISIBLE);
                monthView.setDate(Calendar.getInstance().getTimeInMillis());
            }  else if(id == R.id.services){
                Intent intent = new Intent(this, AddServiceActivity.class);
                this.startActivity(intent);
            } else if(id == R.id.settings){
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
            }else if(id == R.id.payments){
                Intent intent = new Intent(this, PaymentsActivity.class);
                this.startActivity(intent);
            }else if(id == R.id.oldClients){
                Intent intent = new Intent(this, OldClientsActivity.class);
                this.startActivity(intent);
            }else if(id == R.id.messageClients){
                Intent intent = new Intent(this, MessageClientsActivity.class);
                this.startActivity(intent);
            }
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startSMSAlarm(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(this, SendSMSAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, pendingIntent); // 5 minutes
    }

    //google calendar api
    private void changeGoogleCalendarData() {
        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new ApiAsyncTask(this).execute();
            }
        }
    }

    private void chooseAccount() {
        startActivityForResult(
                credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        HomeActivity.this,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }



    @Override
    public void onResume(){
        try{
            super.onResume();
            startChromeActivity();

            speakerToogle = findViewById(R.id.speakerToogleButton);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String speakerOn = preferences.getString("speaker_on","false");
            if(speakerOn != null && speakerOn.equals("true")){
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (!audioManager.isSpeakerphoneOn()) {
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                    audioManager.setSpeakerphoneOn(true);
                    speakerToogle.setImageResource(R.drawable.ic_volume_up_black_24dp);
                }
                String speakerFeature = preferences.getString("speaker_feature", "false");
                if(speakerFeature.equals("false")){
                    speakerToogle.setVisibility(View.GONE);
                }else{
                    speakerToogle.setVisibility(View.VISIBLE);
                }
            }

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

    public void sendAllData(View v){
        try {
            //Utils.insertTestEvents(this, 200);
            List<Event> allEvents = DatabaseConnection.getInstance().eventDao().findAllEvents();
            Gson gson = new Gson();
            String allEventsString = gson.toJson(allEvents);

            Utils.writeToFile(allEventsString);
        }catch(Exception e){
                Log.e("error", e.getMessage());
                Utils.sendEmail(this, e);
        }
    }

    public void toogleSpeaker(View v){
        speakerToogle = findViewById(R.id.speakerToogleButton);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
            speakerToogle.setImageResource(R.drawable.ic_volume_off_black_24dp);
        }else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);
            speakerToogle.setImageResource(R.drawable.ic_volume_up_black_24dp);
        }
    }

    private void startChromeActivity(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String chromeFeature = preferences.getString("chrome_feature", "false");

        if(chromeFeature.equals("true")) {
            Calendar currentDate = Calendar.getInstance();
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DATE, -1);

            String date = preferences.getString("date_for_chrome", Utils.calendarToString(yesterday));
            Calendar readDate = Utils.stringToCalendar(date);

            boolean sameDay = false;
            if (currentDate.get(Calendar.YEAR) == readDate.get(Calendar.YEAR)
                    && currentDate.get(Calendar.MONTH) == readDate.get(Calendar.MONTH)
                    && currentDate.get(Calendar.DAY_OF_MONTH) == readDate.get(Calendar.DAY_OF_MONTH)) {
                sameDay = true;
            }

            if (!sameDay) {
                String chromeLink = preferences.getString("chrome_link", "https://www.google.ro");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(chromeLink));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.android.chrome");
                try {
                    this.startActivity(intent);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("date_for_chrome", Utils.calendarToString(currentDate));
                    editor.commit();

                } catch (ActivityNotFoundException ex) {
                    // Chrome browser presumably not installed so allow user to choose instead
                    intent.setPackage(null);
                    this.startActivity(intent);
                }
            }
        }
    }
}
