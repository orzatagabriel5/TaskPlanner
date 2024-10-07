package com.taskPlanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.taskPlanner.services.RoundIconService;

import java.util.Date;
import java.util.List;

public class CallReceiver extends PhoneCallReceiver {

    Context appContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        appContext = AppContext.getInstance();
        super.onReceive(context,intent);
    }

    @Override
    protected void onIncomingCallStarted(final Context ctx, String number, Date start) {
        checkClientAndStartProximity(ctx, number);
    }
    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        checkClientAndStartProximity(ctx, number);
    }
    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end){
        removeService();
    }
    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end){
        removeService();
    }

    private void checkClientAndStartProximity(Context ctx, String number) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        String speakerFeature = preferences.getString("speaker_feature", "false");
        if(speakerFeature.equals("true")) {
//            List<Client> clientList = DatabaseConnection.getInstance().clientDao().findAllClients();
//            String phoneNumber = number.trim().replace("004", "")
//                    .replaceAll("\\+4", "")
//                    .replaceAll("\\(", "")
//                    .replaceAll("\\)", "")
//                .replaceAll("-", "")
//                .replaceAll(" ", "");
//
//            for (Client client : clientList) {
//                if (phoneNumber.equals(client.getPhone())) {
                    //show icon
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        appContext.startForegroundService(new Intent(appContext, RoundIconService.class));
                    }
//                }
//            }
        }
    }

    private void removeService(){
        appContext.stopService(new Intent(appContext, RoundIconService.class));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("speaker_on");
        editor.commit();
    }
}
