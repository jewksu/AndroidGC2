package core;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import jewksu.androidgc2.R;

/**
 * Created by kevin on 26/08/2015.
 */
public class ContainerAdapter extends ArrayAdapter<ContainerModel> {


    // declaring our ArrayList of items
    private ArrayList<ContainerModel> objects;


    public ContainerAdapter(Context context, int textViewResourceId, ArrayList<ContainerModel> containers) {
        super(context, textViewResourceId, containers);
        this.objects = containers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        // Check if an existing view is being reused, otherwise inflate the view
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item_container,null);
        }

        ContainerModel i = objects.get(position);

        if (i!=null){
            // Lookup view for data population
            TextView conteneurLabel = (TextView) v.findViewById(R.id.ConteneurLabel);
            TextView tvHome = (TextView) v.findViewById(R.id.tvHome);

            if (conteneurLabel !=null){
                conteneurLabel.setGravity(Gravity.CENTER_VERTICAL);
                conteneurLabel.setText("Conteneur " + i.Id);
            }
        }

        if (position % 2 == 1) {
            v.setBackgroundColor(Color.WHITE);
        } else {
            v.setBackgroundColor(Color.argb(255,248,255,23));
        }

return v;
    }
}
