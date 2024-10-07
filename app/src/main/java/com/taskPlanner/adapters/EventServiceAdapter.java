package com.taskPlanner.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.radu.appointment.R;
import com.taskPlanner.Utils;
import com.taskPlanner.database.model.Service;

import java.util.List;

public class EventServiceAdapter extends BaseAdapter {

    Context context;
    List<Service> data;
    private static LayoutInflater inflater = null;

    public EventServiceAdapter(Context context,  List<Service> data) {
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.event_service_item, null);
        TextView nameField = (TextView) vi.findViewById(R.id.serviceNameField);
        TextView durationField = (TextView) vi.findViewById(R.id.serviceDurationField);
        ImageButton removeButton = (ImageButton) vi.findViewById(R.id.removeServiceButton);

        nameField.setText(data.get(position).getName());
        durationField.setText(data.get(position).getDuration() + " min");

        final EventServiceAdapter adapter = this;
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //calculate remaning time
                TextView timeFromView = ((Activity) context).findViewById(R.id.timeFromField);
                int hourOfDayFromInt = Integer.valueOf(timeFromView.getText().toString().split(":")[0]);
                int minuteFromInt = Integer.valueOf(timeFromView.getText().toString().split(":")[1]);

                TextView timeToView = ((Activity) context).findViewById(R.id.timeToField);
                int hourOfDayToInt = Integer.valueOf(timeToView.getText().toString().split(":")[0]);
                int minuteToInt = Integer.valueOf(timeToView.getText().toString().split(":")[1]);

                int totalFromMinutes = hourOfDayFromInt * 60 + minuteFromInt;
                int totalToMinutes = hourOfDayToInt * 60 + minuteToInt;
                int remainingMinutes = totalToMinutes - data.get(position).getDuration();

                if(remainingMinutes == totalFromMinutes){
                    remainingMinutes += 30;
                }
                int newHour = remainingMinutes / 60 ;
                int newMinutes = remainingMinutes % 60;
                if(newHour >= 24){
                    newHour = 23;
                    newMinutes = 59;
                    hourOfDayFromInt = 23;
                    minuteFromInt = 49;
                }

                if(hourOfDayFromInt * 60 + minuteFromInt > newHour * 60 + newMinutes){
                    timeToView.setText(Utils.getCorrectTime(hourOfDayFromInt, minuteFromInt));
                }else{
                    timeToView.setText(Utils.getCorrectTime(newHour, newMinutes));
                }

                //calculate remaining price
                TextView priceField = ((Activity) context).findViewById(R.id.priceField);
                int currentPrice = Integer.parseInt(priceField.getText().toString());
                currentPrice -= data.get(position).getPrice();
                priceField.setText("" + currentPrice);

                //remove service from list
                ListView listServicesView = parent.findViewById(R.id.servicesListView);
                data.remove(position);
                adapter.notifyDataSetChanged();
                Utils.setListViewHeightBasedOnChildren(listServicesView);
            }
        });
        return vi;
    }

}