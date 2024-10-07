package com.taskPlanner.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.radu.appointment.R;
import com.taskPlanner.Utils;
import com.taskPlanner.adapters.ContactAdapter;
import com.taskPlanner.adapters.IgnoreListAdapter;
import com.taskPlanner.database.DatabaseConnection;
import com.taskPlanner.database.memory.ContactsInMemory;
import com.taskPlanner.database.model.Client;
import com.taskPlanner.database.model.IgnoreClient;
import com.taskPlanner.services.MessageClientsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class MessageClientsActivity extends AppCompatActivity {

    String selectedPhone = null;
    AutoCompleteTextView autoCompleteTextView = null;
    TextView promotionMessageTextView = null;
    List<IgnoreClient> ignoreList = null;
    IgnoreListAdapter ignoreListAdapter = null;
    ListView ignoreListView = null;
    Gson gson = new Gson();
    SharedPreferences preferences = null;
    List<Client> clientList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_message_clients);

            preferences = PreferenceManager.getDefaultSharedPreferences(this);

            //set toolbar actions
            android.support.v7.widget.Toolbar toolbar = findViewById(R.id.messageClientsToolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(this.getString(R.string.messageClients_activity_title));

            //load last promotion message
            promotionMessageTextView = findViewById(R.id.promotionMessageTextView);
            String promotionMessage = preferences.getString("promotionMessage", "");
            promotionMessageTextView.setText(promotionMessage);

            //populate ignore list
            String ignoreListString = preferences.getString("ignoreClientList", "");
            if (ignoreListString != null && !ignoreListString.isEmpty()) {
                ignoreList = gson.fromJson(ignoreListString, new TypeToken<List<IgnoreClient>>() {
                }.getType());
            } else {
                ignoreList = new ArrayList<IgnoreClient>();
            }
            ignoreListAdapter = new IgnoreListAdapter(this, ignoreList);

            ignoreListView = findViewById(R.id.ignoreContactsList);
            ignoreListView.setAdapter(ignoreListAdapter);
        }catch(Exception e){
                Log.e("error", e.getMessage());
                Utils.sendEmail(this, e);
        }
    }

    public void sendMessage(final View view){
        try{
            //read contacts needed
            clientList = new ArrayList<Client>();
            CheckBox oldClientsCheckBox = findViewById(R.id.oldClientsCheckbox);
            if(oldClientsCheckBox.isChecked()){
                clientList = DatabaseConnection.getInstance().clientDao().findAllClients();
            }else{
                Calendar last30Days = Calendar.getInstance();
                last30Days.add(Calendar.DAY_OF_MONTH, -30);
                clientList = DatabaseConnection.getInstance().clientDao().findClientsNotOlderThan(last30Days);
            }

            //remove ignore clients
            Iterator<Client> clientIterator = clientList.iterator();
            while(clientIterator.hasNext()){
                Client client = clientIterator.next();
                for(IgnoreClient ignoreClient : ignoreList){
                    if(client.getPhone() != null && client.getPhone().equals(ignoreClient.getPhone())){
                        clientIterator.remove();
                    }
                }
            }

            Intent intent = new Intent(this, MessageClientsService.class);
            String clientListString = gson.toJson(clientList);
            intent.putExtra("clientList",clientListString);
            intent.putExtra("promotionMessage",promotionMessageTextView.getText().toString());
            startService(intent);

            //save promotion message before leaving page
            savePromotionMessage();
            finish();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    public void ignoreClient(final View view) {
        try{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            //Add content
            LayoutInflater inflater = getLayoutInflater();
            final View inflaterView = inflater.inflate(R.layout.add_ignore_client, null);
            builder.setView(inflaterView);

            //populate clients in autocomplete
            final Activity activity = this;
            ContactAdapter contactAdapter = new ContactAdapter(this,
                    R.layout.clientlistitemlayout, ContactsInMemory.clientList);

            autoCompleteTextView = (AutoCompleteTextView) inflaterView.findViewById(R.id.contactsAutocomplete);
            autoCompleteTextView.setAdapter(contactAdapter);
            autoCompleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    autoCompleteTextView.showDropDown();
                }
            });
            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Client client = (Client) parent.getItemAtPosition(position);

                    autoCompleteTextView.setText(client.getName());
                    selectedPhone = client.getPhone();
                    Utils.hideKeyboard(activity);
                }
            });

            // Add the buttons
            builder.setPositiveButton(this.getString(R.string.general_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //overriden below
                }
            });
            builder.setNegativeButton(this.getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            // Create the AlertDialog
            final Context currentContext = this;
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    try{
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(selectedPhone != null) {
                                    //add and save new ignore contact
                                    IgnoreClient ignoreClient = new IgnoreClient(autoCompleteTextView.getText().toString(), selectedPhone);

                                    if(ignoreList  != null){
                                        if(!ignoreList.contains(ignoreClient)){
                                            ignoreList.add(ignoreClient);
                                        }else{
                                            Toast.makeText(inflaterView.getContext(), currentContext.getString(R.string.ignore_client_exist_error), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    String  ignoreListString = gson.toJson(ignoreList);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("ignoreClientList", ignoreListString);
                                    editor.commit();
                                    ignoreListAdapter.notifyDataSetChanged();

                                    //hide keyboard
                                    InputMethodManager imm = (InputMethodManager)getSystemService(currentContext.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
                                }
                                dialog.dismiss();
                            }
                        });
                    }catch(Exception e){
                        Log.e("error", e.getMessage());
                        Utils.sendEmail(null, e);
                    }
                }
            });
            dialog.show();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    private void savePromotionMessage() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("promotionMessage", promotionMessageTextView.getText().toString());
        editor.commit();
    }

    @Override
    public void onPause() {
        try{
            super.onPause();
            if (this.isFinishing()){
                savePromotionMessage();
            }
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }
}
