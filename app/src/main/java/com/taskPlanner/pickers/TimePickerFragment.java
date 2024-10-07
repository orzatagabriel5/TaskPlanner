package com.taskPlanner.pickers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import com.taskPlanner.Utils;
import com.taskPlanner.database.model.Service;

import java.util.Calendar;
import java.util.List;

@SuppressLint("ValidFragment")
public class TimePickerFragment extends android.support.v4.app.DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    TextView timeFromView;
    TextView timeToView;
    List<Service> serviceList;

    public TimePickerFragment(TextView timeFromView, TextView timeToView, List<Service> serviceList){
        this.timeFromView = timeFromView;
        this.timeToView = timeToView;
        this.serviceList = serviceList;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hourOfDayFromInt = Integer.valueOf(timeFromView.getText().toString().split(":")[0]);
        int minuteFromInt = Integer.valueOf(timeFromView.getText().toString().split(":")[1]);
        return new TimePickerDialog(getActivity(), this, hourOfDayFromInt, minuteFromInt,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        timeFromView.setText(Utils.getCorrectTime(hourOfDay,minute));

        int addedTime = 0;
        if(serviceList != null && serviceList.size() > 0){
            for(Service service : serviceList){
                addedTime += service.getDuration();
            }
        }
        if(addedTime != 0) {
            hourOfDay +=  addedTime / 60;
            minute += addedTime % 60;

            if (minute / 60 != 0) {
                hourOfDay += (minute / 60);
                minute = minute % 60;
            }
        }else if(addedTime == 0){
            minute += 10;
            if(minute >= 60){
                hourOfDay++;
                minute = minute % 60;
            }
        }
        if(hourOfDay >= 24){
            hourOfDay = 23;
            minute = 59;
            int timeFromMinutes = hourOfDay * 60 + minute - addedTime;
            int newHour = timeFromMinutes / 60;
            int newMinute = timeFromMinutes % 60;
            if(hourOfDay == newHour && minute - newMinute <= 10){
                timeFromView.setText(Utils.getCorrectTime(hourOfDay, minute-10));
            }else{
                timeFromView.setText(Utils.getCorrectTime(newHour, newMinute));
            }
        }
        timeToView.setText(Utils.getCorrectTime(hourOfDay,minute));
    }

}
