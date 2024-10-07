package com.taskPlanner.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Calendar;
import java.util.Date;

public class Converters {
    @TypeConverter
    public static Calendar fromTimestamp(Long value) {
        if(value == null) {
            return null;
        }else{
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(value));
            return calendar;
        }
    }

    @TypeConverter
    public static Long dateToTimestamp(Calendar date) {
        if(date == null){
            return null;
        }else{
            return date.getTime().getTime();
        }
    }
}