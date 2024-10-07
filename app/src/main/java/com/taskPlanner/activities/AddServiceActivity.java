package com.taskPlanner.activities;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.radu.appointment.R;
import com.taskPlanner.Utils;
import com.taskPlanner.adapters.ColorAdapter;
import com.taskPlanner.adapters.ServiceAdapter;
import com.taskPlanner.database.DatabaseConnection;
import com.taskPlanner.database.dao.EventDao;
import com.taskPlanner.database.dao.ServiceDao;
import com.taskPlanner.database.memory.Color;
import com.taskPlanner.database.memory.ColorInMemory;
import com.taskPlanner.database.model.Event;
import com.taskPlanner.database.model.Service;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;


public class AddServiceActivity extends AppCompatActivity {

    ListView serviceListView;
    List<Service> serviceList;
    ServiceAdapter serviceAdapter;

    TextView serviceField;
    TextView durationField;
    TextView priceField;
    TextView colorFieldId;

    ServiceDao serviceDao;
    EventDao eventDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_service);

            //set toolbar actions
            android.support.v7.widget.Toolbar toolbar = findViewById(R.id.addServiceToolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(this.getString(R.string.add_service_activity_title));

            //get db dao
            serviceDao = DatabaseConnection.getInstance().serviceDao();
            eventDao = DatabaseConnection.getInstance().eventDao();

            //populate service list
            serviceList = serviceDao.findAllServices();
            serviceAdapter = new ServiceAdapter(this, serviceList);

            serviceListView = findViewById(R.id.servicesList);
            serviceListView.setAdapter(serviceAdapter);

            final Context currentContext = this;
            serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final Service service = (Service) parent.getItemAtPosition(position);
                    AlertDialog.Builder builder = new AlertDialog.Builder(currentContext);

                    //Add content
                    LayoutInflater inflater = getLayoutInflater();
                    final View inflaterView = inflater.inflate(R.layout.add_service_dialog, null);

                    serviceField = inflaterView.findViewById(R.id.serviceField);
                    priceField = inflaterView.findViewById(R.id.priceField);
                    colorFieldId = inflaterView.findViewById(R.id.hiddenColorField);
                    builder.setView(inflaterView);
                    durationField = inflaterView.findViewById(R.id.durationField);

                    //Prefill data for service edit
                    serviceField.setText(service.getName());
                    durationField.setText("" + service.getDuration());
                    priceField.setText("" + service.getPrice());
                    colorFieldId.setText("" + service.getColorId());

                    //Populate colors in autocomplete
                    final AutoCompleteTextView colorView = prepareColorAutocomplete(view.getContext(), inflaterView, service);

                    // Add the buttons
                    builder.setPositiveButton(currentContext.getString(R.string.general_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //overriden below
                        }
                    });
                    builder.setNegativeButton(currentContext.getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

                    // Create the AlertDialog
                    final AlertDialog dialog = builder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    if (isServiceValid(serviceField, durationField, priceField, inflaterView)) {
                                        service.setName(serviceField.getText().toString());
                                        service.setDuration(Integer.parseInt(durationField.getText().toString()));
                                        service.setPrice(Integer.parseInt(priceField.getText().toString()));
                                        service.setColorName(colorView.getText().toString());
                                        service.setColorId(Integer.parseInt(colorFieldId.getText().toString()));

                                        //update services color for existing events
                                        List<Event> events = eventDao.findAllEvents();
                                        for (Event event : events) {
                                            Type collectionService = new TypeToken<Collection<Service>>() {
                                            }.getType();
                                            List<Service> serviceList = new Gson().fromJson(event.getServices(), collectionService);
                                            for (Service dbService : serviceList) {
                                                if (service.getId() == dbService.getId()) {
                                                    dbService.setColorId(service.getColorId());
                                                }
                                            }
                                            String updatedServices = new Gson().toJson(serviceList);
                                            event.setServices(updatedServices);
                                            eventDao.updateEvent(event);
                                        }

                                        Toast.makeText(inflaterView.getContext(), currentContext.getString(R.string.edit_service_success_notification), Toast.LENGTH_SHORT).show();
                                        serviceDao.updateService(service);
                                        serviceList = serviceDao.findAllServices();
                                        serviceAdapter.notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }
                    });
                    dialog.show();
                }
            });
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    //create and show add service dialog
    public void addServiceDialog(final View view){
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            //Add content
            LayoutInflater inflater = getLayoutInflater();
            final View inflaterView = inflater.inflate(R.layout.add_service_dialog, null);
            builder.setView(inflaterView);

            //Populate colors in autocomplete
            final AutoCompleteTextView colorView = prepareColorAutocomplete(view.getContext(), inflaterView, null);

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
                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            serviceField = inflaterView.findViewById(R.id.serviceField);
                            durationField = inflaterView.findViewById(R.id.durationField);
                            priceField = inflaterView.findViewById(R.id.priceField);
                            colorFieldId = inflaterView.findViewById(R.id.hiddenColorField);

                            if (isServiceValid(serviceField, durationField, priceField, inflaterView)) {
                                Service service = new Service();
                                service.setName(serviceField.getText().toString());
                                service.setDuration(Integer.parseInt(durationField.getText().toString()));
                                service.setPrice(Integer.parseInt(priceField.getText().toString()));
                                service.setColorName(colorView.getText().toString());
                                service.setColorId(Integer.parseInt(colorFieldId.getText().toString()));

                                Toast.makeText(inflaterView.getContext(), currentContext.getString(R.string.add_service_success_notification), Toast.LENGTH_SHORT).show();
                                long newServiceId = serviceDao.insertService(service);
                                service.setId((int) newServiceId);
                                serviceList.add(service);
                                serviceAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });
            dialog.show();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }
    private boolean isServiceValid(TextView serviceField, TextView durationField, TextView priceField, View inflaterView){
        if(serviceField.getText().toString().isEmpty()){
            Toast.makeText(inflaterView.getContext(), this.getString(R.string.add_service_error_name_required), Toast.LENGTH_SHORT).show();
            return false;
        } else if(durationField.getText().toString().isEmpty()){
            Toast.makeText(inflaterView.getContext(), this.getString(R.string.add_service_error_duration_required), Toast.LENGTH_SHORT).show();
            return false;
        } else if(priceField.getText().toString().isEmpty()){
            Toast.makeText(inflaterView.getContext(), this.getString(R.string.add_service_error_price_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private AutoCompleteTextView prepareColorAutocomplete(Context context, View inflaterView, Service service){
        ColorAdapter colorAdapter = new ColorAdapter(context, R.layout.color_item, ColorInMemory.colorList);

        final ImageView colorLabel = inflaterView.findViewById(R.id.serviceColorLabel);
        final AutoCompleteTextView colorView = inflaterView.findViewById(R.id.colorAutoComplete);
        final TextView colorId = inflaterView.findViewById(R.id.hiddenColorField);

        //set service color or default color
        if(service == null) {
            colorLabel.setBackgroundResource(R.color.green);
            colorView.setText(getString(R.string.green));
            colorId.setText("" + R.color.green);
        }else{
            colorLabel.setBackgroundResource(service.getColorId());
            colorView.setText(service.getColorName());
            colorId.setText("" + service.getColorId());
        }

        colorView.setAdapter(colorAdapter);
        colorView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    colorView.showDropDown();
                    Utils.hideKeyboard(AddServiceActivity.this);
                }
            }
        });
        colorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorView.showDropDown();
                Utils.hideKeyboard(AddServiceActivity.this);
            }
        });
        colorView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Color color = (Color) parent.getItemAtPosition(position);
                colorLabel.setBackgroundResource(color.getColorId());
                colorView.setText(color.getColorName());
                colorId.setText("" + color.getColorId());
            }
        });
        return colorView;
    }

    @Override
    public void onResume(){
        try{
            super.onResume();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    @Override
    public void onPause(){
        try{
            super.onPause();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    @Override
    public void onStart(){
        try{
            super.onStart();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

    @Override
    public void onStop(){
        try{
            super.onStop();
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
    }

}
