package jewksu.androidgc2;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

import core.ContainerAdapter;
import core.ContainerModel;
import core.ControllerCommunication;


public class conteneurs_liste extends ListActivity implements ControllerCommunication.ResponseListener {

    private static final String TAG = "Conteneurs_Liste";
    ControllerCommunication controllerComm;
    ContainerAdapter adapter;
    ArrayList<ContainerModel> containers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conteneurs_liste);

        // get controller host and port from settings
        /* no need to provide default values, we have ensured in onCreate that preferences
         * have been created with default values if needed */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String server_host = sharedPref.getString("server_host", "");
        int server_port = Integer.valueOf(sharedPref.getString("server_port", "0"));

        controllerComm = new ControllerCommunication(server_host, server_port, this);
        controllerComm.simpleRequest("REQ_SUPERVISION_STATE");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conteneurs_liste, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void buttonListeOnClick(View v){
        Button button = (Button)v;
        switch(button.getText().toString()) {
            case "Retour":
                Intent intentListe = new Intent(conteneurs_liste.this, portail_simulation.class);
                startActivity(intentListe);
                break;
        }
    }

    @Override
    public void onControllerResponse(Document response) {
        // check response type
        if (response != null) {
            Element rootResp = response.getRootElement();
            String responseType = rootResp.getChild("response_type").getTextNormalize().toUpperCase();
            Log.i(TAG, "Server response: " + responseType);
            switch (responseType) {
                case "RESP_SUPERVISION_STATE":
                    // get supervision data
                    Element supervisionState = rootResp.getChild("supervision_state");
                    int containerVal = Integer.parseInt(supervisionState.getChild("date_state").getValue());

                    Element Ilots = supervisionState.getChild("container_sets");
                    List<Content> IlotsContent = Ilots.getContent();
                    containers = new ArrayList<ContainerModel>();
                    for(int i = 0; i < IlotsContent.size(); i++)
                    {
                        List<Content> containersXMLObjects = ((Element)IlotsContent.get(i)).getChild("containers").getContent();
                        for(int j =0; j < containersXMLObjects.size(); j++)
                        {
                            ContainerModel containerToAdd = new ContainerModel();
                            containerToAdd.SetId(Integer.parseInt(((Element)containersXMLObjects.get(j)).getChild("id").getValue()));
                            containerToAdd.SetPoids(Integer.parseInt(((Element)containersXMLObjects.get(j)).getChild("weight").getValue()));
                            containerToAdd.SetVolume(Integer.parseInt(((Element)containersXMLObjects.get(j)).getChild("volume").getValue()));
                            containerToAdd.SetVolumeMax(Integer.parseInt(((Element)containersXMLObjects.get(j)).getChild("volumemax").getValue()));
                            containerToAdd.SetFillRatio(Integer.parseInt(((Element)containersXMLObjects.get(j)).getChild("fillratio").getValue()));
                            containerToAdd.SetToBeCollected(Boolean.parseBoolean(((Element)containersXMLObjects.get(j)).getChild("to_be_collected").getValue()));

                            //containers.add((Element)containersXMLObjects.get(j));
                            containers.add(containerToAdd);
                        }
                    }
                    adapter = new ContainerAdapter(this,containers);
                    ListView listview = (ListView)findViewById(android.R.id.list);
                    listview.setAdapter(adapter);
                    //setListAdapter(adapter);
                    break;
            }
        }
    }
}
