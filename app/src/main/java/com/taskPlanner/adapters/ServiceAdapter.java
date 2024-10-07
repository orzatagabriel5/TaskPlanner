package com.taskPlanner.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.radu.appointment.R;
import com.taskPlanner.database.DatabaseConnection;
import com.taskPlanner.database.model.Service;

import java.util.List;

public class ServiceAdapter extends BaseAdapter {

    Context context;
    List<Service> data;
    private static LayoutInflater inflater = null;

    public ServiceAdapter(Context context,  List<Service> data) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.serviceitemlayout, null);
        TextView nameField = vi.findViewById(R.id.serviceNameField);
        TextView durationField = vi.findViewById(R.id.serviceDurationField);
        TextView priceField = vi.findViewById(R.id.servicePriceField);
        ImageButton removeButton = vi.findViewById(R.id.removeServiceButton);
        ImageView colorField = vi.findViewById(R.id.serviceItemColor);

        //fix color problems
//        Utils.updateColors(data.get(position));
//        DatabaseConnection.getInstance().serviceDao().updateService(data.get(position));

        nameField.setText(data.get(position).getName());
        durationField.setText(data.get(position).getDuration() + " min");
        priceField.setText("RON " + data.get(position).getPrice());
        colorField.setBackgroundColor(ContextCompat.getColor(vi.getContext(), data.get(position).getColorId()));



        final ServiceAdapter adapter = this;
        final Context removeContext = vi.getContext();
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(removeContext);
                        builder.setTitle(removeContext.getString(R.string.remove_service_title));

                        // Add the buttons
                        builder.setPositiveButton(removeContext.getString(R.string.general_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DatabaseConnection.getInstance().serviceDao().deleteService(data.get(position));
                                Toast.makeText(context, removeContext.getString(R.string.remove_service_notification, data.get(position).getName()), Toast.LENGTH_SHORT).show();
                                data.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        });
                        builder.setNegativeButton(removeContext.getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });

                        // Create the AlertDialog
                        final AlertDialog dialog = builder.create();
                        dialog.show();

            }
        });

        return vi;
    }
}