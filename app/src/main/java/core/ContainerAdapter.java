package core;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jewksu.androidgc2.R;

/**
 * Created by kevin on 26/08/2015.
 */
public class ContainerAdapter extends ArrayAdapter<ContainerModel> {
    public ContainerAdapter(Context context, ArrayList<ContainerModel> containers) {
        super(context, 0, containers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ContainerModel container = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_container, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvHome = (TextView) convertView.findViewById(R.id.tvHome);
        // Populate the data into the template view using the data object
        tvName.setText(container.Id);
        tvHome.setText(container.Poids);
        // Return the completed view to render on screen
        return convertView;
    }
}
