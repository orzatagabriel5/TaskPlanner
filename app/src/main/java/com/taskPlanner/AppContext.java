package com.taskPlanner;

import android.app.Application;
import android.content.Context;
import android.graphics.Paint;
import android.util.Log;

public class AppContext extends Application {
    private static AppContext singleton;

    public static AppContext getInstance(){
        return singleton;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        Utils.sendEmail(null, new Exception("Low memory"));
    }

}
