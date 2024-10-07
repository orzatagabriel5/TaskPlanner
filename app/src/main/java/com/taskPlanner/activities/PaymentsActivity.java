package com.taskPlanner.activities;

import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.radu.appointment.R;
import com.taskPlanner.Utils;
import com.taskPlanner.database.DatabaseConnection;
import com.taskPlanner.database.model.ClientTotal;

import java.util.Calendar;
import java.util.List;

public class PaymentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_payments);

            //set toolbar actions
            android.support.v7.widget.Toolbar toolbar = findViewById(R.id.paymentsToolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(this.getString(R.string.payments_activity_title));

            TableLayout tableView = (TableLayout) findViewById(R.id.paymentsTable);

            List<ClientTotal> clientList = DatabaseConnection.getInstance().eventDao().getTotalForEveryClient(Calendar.getInstance());

            TableRow rowHeader = new TableRow(this);
            TableRow.LayoutParams tableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f);
            tableRowParams.setMargins(50, 0, 0, 20);
            rowHeader.setLayoutParams(tableRowParams);

            TextView nameHeader = new TextView(this);
            nameHeader.setLayoutParams(tableRowParams);
            nameHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            nameHeader.setTypeface(null, Typeface.BOLD);
            nameHeader.setTextAppearance(this, R.style.PaymentHeaderTextView);
            nameHeader.setText(getString(R.string.payments_activity_name_header));

            TextView totalPaidHeader = new TextView(this);
            totalPaidHeader.setLayoutParams(tableRowParams);
            totalPaidHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            totalPaidHeader.setTypeface(null, Typeface.BOLD);
            totalPaidHeader.setTextAppearance(this, R.style.PaymentHeaderTextView);
            totalPaidHeader.setText(R.string.payments_activity_total_header);

            rowHeader.addView(nameHeader);
            rowHeader.addView(totalPaidHeader);
            tableView.addView(rowHeader);

            for (ClientTotal client : clientList) {
                TableRow row = new TableRow(this);
                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f);
                rowParams.setMargins(50, 0, 0, 20);
                row.setLayoutParams(rowParams);

                TextView nameCol = new TextView(this);
                nameCol.setLayoutParams(rowParams);
                nameCol.setText(client.getName());
                TextView totalPaidCol = new TextView(this);
                totalPaidCol.setLayoutParams(rowParams);
                totalPaidCol.setText(client.getTotalPaid() + " RON");

                row.addView(nameCol);
                row.addView(totalPaidCol);
                tableView.addView(row);
            }
        }catch(Exception e){
            Log.e("error", e.getMessage());
            Utils.sendEmail(this, e);
        }
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
