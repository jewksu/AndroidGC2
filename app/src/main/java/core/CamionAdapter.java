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
public class CamionAdapter extends ArrayAdapter<CamionModel> {
    static private final String dechets_name[] = {"tout_venant", "recyclable", "verre"};


    // declaring our ArrayList of items
    private ArrayList<CamionModel> objects;


    public CamionAdapter(Context context, int textViewResourceId, ArrayList<CamionModel> camions) {
        super(context, textViewResourceId, camions);
        this.objects = camions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        // Check if an existing view is being reused, otherwise inflate the view
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.camion_listitem,null);
        }

        CamionModel i = objects.get(position);

        if (i!=null){
            // Lookup view for data population
            TextView CamionLabel = (TextView) v.findViewById(R.id.CamionLabel);
            TextView CamionId = (TextView) v.findViewById(R.id.CamionId);
            TextView CamionDesc = (TextView) v.findViewById(R.id.CamionDesc);

            CamionLabel.setText("Camion");
            CamionId.setText("#"+String.valueOf(i.Id));
            CamionDesc.setText(dechets_name[i.TypeDechetID-1]);
        }

        if (position % 2 == 1) {
            v.setBackgroundColor(Color.LTGRAY);
        } else {
            v.setBackgroundColor(Color.argb(200,248,255,23));
        }

return v;
    }
}
