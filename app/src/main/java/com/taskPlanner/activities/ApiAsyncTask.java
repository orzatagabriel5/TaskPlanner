package com.taskPlanner.activities;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.radu.appointment.R;
import com.taskPlanner.database.model.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

/**
 * An asynchronous task that handles the Google Calendar API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */

/**
 * Created by miguel on 5/29/15.
 */

public class ApiAsyncTask extends AsyncTask<Void, Void, Void> {
    private HomeActivity mActivity;

    /**
     * Constructor.
     * @param activity MainActivity that spawned this task.
     */
    ApiAsyncTask(HomeActivity activity) {
        this.mActivity = activity;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (mActivity.eventMode.equals("save")) {
                insertEventInGoogleCalendar();
            }else if(mActivity.eventMode.equals("edit")){
                editEventInGoogleCalendar();
            }else if(mActivity.eventMode.equals("remove")){
                removeEventInGoogleCalendar();
            }

        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());

        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    HomeActivity.REQUEST_AUTHORIZATION);

        } catch (IOException e) {
            Log.e("errror", e.getMessage());
        }
        return null;
    }

    private void insertEventInGoogleCalendar() throws IOException {
        com.google.api.services.calendar.model.Event googleEvent = new com.google.api.services.calendar.model.Event();

        EventDateTime startDate = new EventDateTime().setDateTime( new DateTime(mActivity.googleCalendarEvent.getStartDate().getTime(), TimeZone.getDefault()));
        EventDateTime endDate = new EventDateTime().setDateTime(new DateTime(mActivity.googleCalendarEvent.getEndDate().getTime(), TimeZone.getDefault()));

        //read services;
        Type collectionService = new TypeToken<Collection<Service>>() {}.getType();
        List<Service> serviceList = new Gson().fromJson(mActivity.googleCalendarEvent.getServices(), collectionService);

        String services = mActivity.getString(R.string.google_calendar_services_header) + ":\n";
        for (Service service : serviceList) {
            services += service.getName() + "\n";
        }

        googleEvent.setId(mActivity.googleCalendarEvent.getEventId());
        googleEvent.setSummary(mActivity.googleCalendarEvent.getClientName());
        googleEvent.setDescription(services);
        googleEvent.setStart(startDate);
        googleEvent.setEnd(endDate);

        mActivity.mService.events().insert("primary",googleEvent).execute();
    }

    private void editEventInGoogleCalendar()  throws IOException {
        Events events = mActivity.mService.events().list("primary")
                .setTimeMin(new DateTime(mActivity.googleCalendarEventStartTime.getTime(), TimeZone.getDefault()))
                .setTimeMax(new DateTime(mActivity.googleCalendarEventEndTime.getTime(), TimeZone.getDefault()))
                .execute();
        List<Event> items = events.getItems();

        for (Event event : items) {
            if(event.getId() != null && event.getId().equals(mActivity.googleCalendarEvent.getEventId())){
                EventDateTime startDate = new EventDateTime().setDateTime( new DateTime(mActivity.googleCalendarEvent.getStartDate().getTime(), TimeZone.getDefault()));
                EventDateTime endDate = new EventDateTime().setDateTime(new DateTime(mActivity.googleCalendarEvent.getEndDate().getTime(), TimeZone.getDefault()));

                //read services;
                Type collectionService = new TypeToken<Collection<Service>>() {}.getType();
                List<Service> serviceList = new Gson().fromJson(mActivity.googleCalendarEvent.getServices(), collectionService);

                String services = "Services:";
                for (Service service : serviceList) {
                    services += service.getName() + "\n";
                }

                event.setSummary(mActivity.googleCalendarEvent.getClientName());
                event.setDescription(services);
                event.setStart(startDate);
                event.setEnd(endDate);
                mActivity.mService.events().update("primary",event.getId(),event).execute();
            }
        }
    }

    private void removeEventInGoogleCalendar()  throws IOException {
        Events events = mActivity.mService.events().list("primary")
                .setTimeMin(new DateTime(mActivity.googleCalendarEvent.getStartDate().getTime(), TimeZone.getDefault()))
                .setTimeMax(new DateTime(mActivity.googleCalendarEvent.getEndDate().getTime(), TimeZone.getDefault()))
                .execute();
        List<Event> items = events.getItems();

        for (Event event : items) {
            if(event.getId() != null && event.getId().equals(mActivity.googleCalendarEvent.getEventId())){
                EventDateTime startDate = new EventDateTime().setDateTime( new DateTime(mActivity.googleCalendarEvent.getStartDate().getTime(), TimeZone.getDefault()));
                EventDateTime endDate = new EventDateTime().setDateTime(new DateTime(mActivity.googleCalendarEvent.getEndDate().getTime(), TimeZone.getDefault()));
                event.setSummary(mActivity.googleCalendarEvent.getClientName());
                event.setStart(startDate);
                event.setEnd(endDate);
                mActivity.mService.events().delete("primary", event.getId()).execute();
            }
        }
    }
}