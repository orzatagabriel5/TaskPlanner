package com.taskPlanner.database.memory;

import android.content.res.Resources;

import com.taskPlanner.AppContext;
import com.radu.appointment.R;

import java.util.ArrayList;
import java.util.List;

public class ColorInMemory {

    public static List<Color> colorList = new ArrayList<Color>();

    static{
        colorList.add(new Color(R.color.yellow, AppContext.getInstance().getString(R.string.yellow)));
        colorList.add(new Color(R.color.red, AppContext.getInstance().getString(R.string.red)));
        colorList.add(new Color(R.color.purple, AppContext.getInstance().getString(R.string.purple)));
        colorList.add(new Color(R.color.maroon, AppContext.getInstance().getString(R.string.maroon)));
        colorList.add(new Color(R.color.aqua, AppContext.getInstance().getString(R.string.aqua)));
        colorList.add(new Color(R.color.lime, AppContext.getInstance().getString(R.string.lime)));
        colorList.add(new Color(R.color.blue, AppContext.getInstance().getString(R.string.blue)));
        colorList.add(new Color(R.color.green, AppContext.getInstance().getString(R.string.green)));
        colorList.add(new Color(R.color.orange, AppContext.getInstance().getString(R.string.orange)));
        colorList.add(new Color(R.color.light_green, AppContext.getInstance().getString(R.string.light_green)));
        colorList.add(new Color(R.color.turquoise, AppContext.getInstance().getString(R.string.turquoise)));
    }
}
