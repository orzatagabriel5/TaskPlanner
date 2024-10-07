package com.taskPlanner.pickers;

import com.taskPlanner.AppContext;
import com.radu.appointment.R;

public enum DayOfWeek {

    DAY1(AppContext.getInstance().getString(R.string.day1), 1),
    DAY2(AppContext.getInstance().getString(R.string.day2), 2),
    DAY3(AppContext.getInstance().getString(R.string.day3), 3),
    DAY4(AppContext.getInstance().getString(R.string.day4), 4),
    DAY5(AppContext.getInstance().getString(R.string.day5), 5),
    DAY6(AppContext.getInstance().getString(R.string.day6), 6),
    DAY7(AppContext.getInstance().getString(R.string.day7), 7);

    private String name;
    private int day;

    private DayOfWeek(String name, int day){
        this.name = name;
        this.day = day;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public static String getDayNameByNumber(int day){
        for(DayOfWeek dayOfWeek : DayOfWeek.values()){
            if(dayOfWeek.getDay() == day){
                return dayOfWeek.getName();
            }
        }
        return null;
    }
}
