package com.taskPlanner.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.taskPlanner.AppContext;
import com.radu.appointment.R;
import com.taskPlanner.Utils;
import com.taskPlanner.activities.HomeActivity;

public class RoundIconService extends Service {

    WindowManager windowManager;
    ImageButton icon;
    WindowManager.LayoutParams params;
    Context appContext;
    Thread thread;
    boolean appOpened = false;
    AudioManager audioManager;
    SensorManager mySensorManager;
    SensorEventListener proximitySensorEventListener;
    Sensor myProximitySensor;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onStart(Intent intent, int startId) {
        try {
            super.onStart(intent, startId);

            appContext = AppContext.getInstance();
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mySensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
            createIconOnScreen();
            //createSensorListener();

            String channelId = "my_channel1";
            NotificationChannel channel = new NotificationChannel(channelId, "System", NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            Intent notificationIntent = new Intent(this, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_verbeauty)
                    .setContentTitle("VerBeauty")
                    .setContentText("Checking client...")
                    .setContentIntent(pendingIntent).build();

            startForeground(1, notification);
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(null, e);
        }
    }


    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            // TODO Auto-generated method stub
            super.onDestroy();
            mySensorManager.unregisterListener(proximitySensorEventListener, myProximitySensor);
            removeIcon();
            stopForeground(true);
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(null, e);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private void createIconOnScreen(){
        windowManager = (WindowManager) appContext.getSystemService(WINDOW_SERVICE);

        icon = new ImageButton(appContext);
        icon.setImageResource(R.mipmap.ic_verbeauty);
        icon.setBackgroundResource(android.R.color.transparent);
        icon.setScaleX(0.8f);
        icon.setScaleY(0.8f);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x =  Resources.getSystem().getDisplayMetrics().widthPixels;
        params.y = Resources.getSystem().getDisplayMetrics().heightPixels/3;

        //this code is for icon actions

        icon.setOnTouchListener(new View.OnTouchListener() {
            float startX;
            float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(appContext, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("speaker_on", "true");
                        editor.commit();

                        appContext.startActivity(intent);
                        appOpened = true;
                        hideIcon();
                        return true;
                }
                return false;
            }
        });
        windowManager.addView(icon, params);
    }

//    private void createSensorListener(){
//
//        //proximity sensor
//        myProximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
//        proximitySensorEventListener = new SensorEventListener() {
//            @Override
//            public void onAccuracyChanged(Sensor sensor, int accuracy) {
//                // TODO Auto-generated method stub
//            }
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void onSensorChanged(SensorEvent event) {
//                // TODO Auto-generated method stub
//                if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
//                    if (event.values[0] == 0) {
//                        //Near
//                        if (audioManager.isSpeakerphoneOn()) {
//                            audioManager.setMode(AudioManager.MODE_NORMAL);
//                            audioManager.setSpeakerphoneOn(false);
//                        }
//                    } else {
//                        //Away
//                        if (!audioManager.isSpeakerphoneOn() && appOpened) {
//                            audioManager.setMode(AudioManager.MODE_IN_CALL);
//                            audioManager.setSpeakerphoneOn(true);
//                        }
//                    }
//                }
//            }
//        };
//
//        if (myProximitySensor != null) {
//            mySensorManager.registerListener(proximitySensorEventListener,
//                    myProximitySensor,
//                    SensorManager.SENSOR_DELAY_NORMAL);
//        }
//
//    }


    private void hideIcon(){
        icon.setVisibility(View.INVISIBLE);
    }

    private void removeIcon(){
        if (icon != null)
            windowManager.removeView(icon);
    }

}
