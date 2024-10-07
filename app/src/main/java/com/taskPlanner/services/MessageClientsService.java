package com.taskPlanner.services;

import android.app.IntentService;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.taskPlanner.Utils;
import com.taskPlanner.database.model.Client;

import java.util.ArrayList;
import java.util.List;

public class MessageClientsService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    public MessageClientsService(String name) {
        super(name);
    }

    public MessageClientsService() {
        super("MessageClientsServiceThread");
    }

    public MessageClientsService(List<Client> clientList) {
        super("MessageClientsServiceThread");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try{
            Gson gson = new Gson();
            String clientListString = intent.getExtras().getString("clientList");
            String promotionMessage = intent.getExtras().getString("promotionMessage");
            List<Client> clientList = gson.fromJson(clientListString,  new TypeToken<List<Client>>(){}.getType());
            SmsManager smsManager = SmsManager.getDefault();
            for(Client client: clientList) {
                try{
                    Thread.sleep(10000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ArrayList<String> parts = smsManager.divideMessage(promotionMessage);
                smsManager.sendMultipartTextMessage(client.getPhone(), null, parts, null, null);
            }

            stopSelf();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(null, e);
        }
    }
}
