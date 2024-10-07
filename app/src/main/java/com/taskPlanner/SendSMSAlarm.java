package com.taskPlanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.taskPlanner.database.DatabaseConnection;
import com.taskPlanner.database.model.Event;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SendSMSAlarm  extends BroadcastReceiver{

    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            SmsManager smsManager = SmsManager.getDefault();

            Calendar calendarFrom = Calendar.getInstance();
            Calendar calendarTo = Calendar.getInstance();
            calendarTo.add(Calendar.DAY_OF_MONTH, 2);

            List<Event> eventList = DatabaseConnection.getInstance().eventDao().findEventsBetween(calendarFrom, calendarTo);

            for (Event event : eventList) {
                Type collectionReminder = new TypeToken<Collection<Integer>>() {
                }.getType();
                List<Integer> reminderList = new Gson().fromJson(event.getReminders(), collectionReminder);

                Calendar currentDate = Calendar.getInstance();

                Iterator<Integer> reminderIterator = reminderList.iterator();
                while (reminderIterator.hasNext()) {
                    Calendar startDate = (Calendar) event.getStartDate().clone();
                    startDate.add(Calendar.HOUR, -reminderIterator.next());
                    long diffInMillis = currentDate.getTimeInMillis() - startDate.getTimeInMillis();
                    if (diffInMillis >= 0 && diffInMillis <= 1000 * 60 * 60) {
                        String smsText = Utils.formatSMS(event, context);
                        ArrayList<String> parts = smsManager.divideMessage(smsText);
                        smsManager.sendMultipartTextMessage(event.getClientPhoneNumber(), null, parts, null, null);
                        reminderIterator.remove();
                    }
                }

                String newReminders = new Gson().toJson(reminderList);
                event.setReminders(newReminders);
                DatabaseConnection.getInstance().eventDao().updateEvent(event);
            }
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(null, e);
        }
    }
}