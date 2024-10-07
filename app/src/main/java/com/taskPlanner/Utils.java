package com.taskPlanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.taskPlanner.database.DatabaseConnection;
import com.taskPlanner.database.dao.EventDao;
import com.taskPlanner.database.model.Event;
import com.taskPlanner.database.model.SMS;
import com.taskPlanner.database.model.Service;
import com.taskPlanner.pickers.DayOfWeek;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class Utils {
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static void setListViewHeightBasedOnChildren
            (ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) view.setLayoutParams(new
                    ViewGroup.LayoutParams(desiredWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

        listView.setLayoutParams(params);
        listView.requestLayout();
    }



    public static String getCorrectTime(int hourOfDay, int minute){
        String hourOfDayString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String timeString = hourOfDayString + ":" + minuteString;

        return timeString;
    }

    public static String formatSMS(Event event, Context context){
        try {
            SMS sms = DatabaseConnection.getInstance().smsDao().findLastSMS();
            String smsTemplate = sms.getTemplate();

            Calendar startDateCal = event.getStartDate();
            Calendar endDateCal = event.getEndDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String startDate = sdf.format(event.getStartDate().getTime());
            startDate = DayOfWeek.getDayNameByNumber(startDateCal.get(Calendar.DAY_OF_WEEK)) + ", " + startDate;
            int duration = (endDateCal.get(Calendar.HOUR) * 60 + endDateCal.get(Calendar.MINUTE)) - (startDateCal.get(Calendar.HOUR) * 60 + startDateCal.get(Calendar.MINUTE));

            String services = "";
            Type collectionService = new TypeToken<Collection<Service>>() {
            }.getType();
            List<Service> serviceList = new Gson().fromJson(event.getServices(), collectionService);
            for (Service service : serviceList) {
                services += service.getName() + ", ";
            }
            String firstName = event.getClientName().split(" ")[0];

            smsTemplate = smsTemplate.replace(context.getString(R.string.sms_name), firstName)
                    .replace(context.getString(R.string.sms_start), startDate)
                    .replace(context.getString(R.string.sms_duration), "" + duration)
                    .replace(context.getString(R.string.sms_price), "" + event.getPrice())
                    .replace(context.getString(R.string.sms_services), services)
                    .replace(context.getString(R.string.sms_notes), event.getNotes() == null ? "" : event.getNotes());

            return smsTemplate;
        }catch(Exception e){

            Log.e("error", e.getMessage());
            Utils.sendEmail(null, e);
        }
        return null;
    }


    public static Calendar stringToCalendar(String date){
        Calendar calendar = Calendar.getInstance();
        if(date != null) {
            try {
                calendar.setTime(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return calendar;
    }

    public static void insertTestEvents(Context context, int eventNumber){

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MINUTE, 90);

        EventDao eventDao = DatabaseConnection.getInstance().eventDao();
        for(int i = 0; i < eventNumber ; i++) {
            startDate.add(Calendar.MINUTE, 90);
            endDate.add( Calendar.MINUTE,90);

            String services = "[{\"colorId\":2131034143,\"colorName\":\"blue\",\"duration\":30,\"id\":1,\"name\":\"test\",\"price\":22},{\"colorId\":2131034231,\"colorName\":\"yellow\",\"duration\":60,\"id\":2,\"name\":\"test2\",\"price\":22}]";
            String reminders = new Gson().toJson(new ArrayList<Integer>());

            Event event = new Event();
            event.setStartDate(startDate);
            event.setEndDate(endDate);
            event.setClientName("Radu");
            event.setClientPhoneNumber("0722633959");
            event.setPrice(20);
            event.setServices(services);
            event.setReminders(reminders);

            eventDao.insertEvent(event);
        }
    }

    public static String calendarToString(Calendar cal){
        return sdf.format(cal.getTime());
    }

    public static void sendEmail(Activity activity, Exception e) {

        String[] TO = {"burlacuradu1990@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Log " + Calendar.getInstance().getTime().toString());

        //stacktrace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();

        emailIntent.putExtra(Intent.EXTRA_TEXT, e.getMessage() + " " + sStackTrace);

        try {
            if(activity != null) {
                activity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                activity.finish();
            }else{
                AppContext.getInstance().startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void sendEmail(Activity activity, String text) {

        String[] TO = {"burlacuradu1990@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Log " + Calendar.getInstance().getTime().toString());
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);

        try {
            if(activity != null) {
                activity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                activity.finish();
            }else{
                AppContext.getInstance().startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void updateColors(Service service) {
        if (service.getColorId() == 2131034215) { //red
            service.setColorId(R.color.red);
        } else if (service.getColorId() == 2131034214) { //purple
            service.setColorId(R.color.purple);
        } else if (service.getColorId() == 2131034230) { //turqoise
            service.setColorId(R.color.turquoise);
        } else if (service.getColorId() == 2131034232) { //yellow
            service.setColorId(R.color.yellow);
        } else if (service.getColorId() == 2131034189) { //Maroon
            service.setColorId(R.color.maroon);
        } else if (service.getColorId() == 2131034138) { //Aqua
            service.setColorId(R.color.aqua);
        } else if (service.getColorId() == 2131034188) { //Lime
            service.setColorId(R.color.lime);
        } else if (service.getColorId() == 2131034143) { //Blue
            service.setColorId(R.color.blue);
        } else if (service.getColorId() == 2131034184) { //Green
            service.setColorId(R.color.green);
        } else if (service.getColorId() == 2131034205) { //Orange
            service.setColorId(R.color.orange);
        } else if (service.getColorId() == 2131034187) { //Light green
            service.setColorId(R.color.light_green);
        }
    }

    public static void writeToFile(String data) {
        try {
            if(isExternalStorageWritable()) {
                File file = getPublicAlbumStorageDir("events.txt");
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(data.getBytes());
                outputStream.close();
                Toast.makeText(AppContext.getInstance(),"File saved successfully!",Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private static File getPublicAlbumStorageDir(String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
