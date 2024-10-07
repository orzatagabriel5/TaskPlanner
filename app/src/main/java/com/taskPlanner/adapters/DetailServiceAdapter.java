package com.taskPlanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.radu.appointment.R;
import com.taskPlanner.database.model.Service;

import java.util.List;

public class DetailServiceAdapter extends BaseAdapter {

    Context context;
    List<Service> data;
    private static LayoutInflater inflater = null;

    public DetailServiceAdapter(Context context, List<Service> data) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.detail_service_item, null);
        TextView serviceField = (TextView) vi.findViewById(R.id.detailServiceField);

        serviceField.setText(data.get(position).getName());

        return vi;
    }
}