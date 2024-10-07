package com.taskPlanner.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.radu.appointment.R;
import com.taskPlanner.database.model.IgnoreClient;

import java.util.List;

public class IgnoreListAdapter extends BaseAdapter {

    Context context;
    List<IgnoreClient> data;
    private static LayoutInflater inflater = null;

    public IgnoreListAdapter(Context context,  List<IgnoreClient> data) {
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
            vi = inflater.inflate(R.layout.ignore_client_list_item, null);
        TextView nameField = vi.findViewById(R.id.nameFieldIgnoreList);
        TextView phoneField = vi.findViewById(R.id.phoneFieldIgnoreList);
        ImageButton removeButton = vi.findViewById(R.id.removeClient);

        nameField.setText(data.get(position).getName());
        phoneField.setText(data.get(position).getPhone());

        final IgnoreListAdapter adapter = this;
        final Context removeContext = vi.getContext();
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //read clients from memory, replace number with empty string and save the new list.
                Toast.makeText(context, removeContext.getString(R.string.remove_client_ignore_list, data.get(position).getName()), Toast.LENGTH_SHORT).show();
                data.remove(position);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                SharedPreferences.Editor editor = preferences.edit();
                Gson gson = new Gson();
                String ignoreListString = gson.toJson(data);
                editor.putString("ignoreClientList", ignoreListString);
                editor.commit();
                adapter.notifyDataSetChanged();
            }
        });

        return vi;
    }
}