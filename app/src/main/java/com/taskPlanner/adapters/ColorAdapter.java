package com.taskPlanner.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.radu.appointment.R;
import com.taskPlanner.database.memory.Color;

import java.util.ArrayList;
import java.util.List;

public class ColorAdapter extends ArrayAdapter {
    private List<Color> colorList;
    private Context mContext;
    private int itemLayout;

    public ColorAdapter(Context context, int resource, List<Color> storeDataLst) {
        super(context, resource, storeDataLst);
        colorList = storeDataLst;
        mContext = context;
        itemLayout = resource;
    }

    @Override
    public int getCount() {
        return colorList.size();
    }

    @Override
    public Color getItem(int position) {
        return colorList.get(position);
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(itemLayout, parent, false);
        }

        ImageView colorLabel =  view.findViewById(R.id.colorLabel);
        TextView colorField = view.findViewById(R.id.colorField);
        TextView hiddenColorField = view.findViewById(R.id.hiddenColorId);

        colorLabel.setBackgroundResource(getItem(position).getColorId());
        colorField.setText(getItem(position).getColorName());
        hiddenColorField.setText(getItem(position).getColorId());

        return view;
    }

}
