package jewksu.androidgc2;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

import core.CamionAdapter;
import core.CamionModel;
import core.ControllerCommunication;


public class camions_liste extends ListActivity implements ControllerCommunication.ResponseListener {

    private static final String TAG = "Camions_Liste";
    ControllerCommunication controllerComm;
    CamionAdapter adapter;
    ArrayList<CamionModel> camion_List;
    final String CONTAINER_ID = "ContainerID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camions_liste);

        // get controller host and port from settings
        /* no need to provide default values, we have ensured in onCreate that preferences
         * have been created with default values if needed */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String server_host = sharedPref.getString("server_host", "");
        int server_port = Integer.valueOf(sharedPref.getString("server_port", "0"));

        controllerComm = new ControllerCommunication(server_host, server_port, this);
        controllerComm.simpleRequest("REQ_ALL_CAMIONS");
    }

    @Override
    protected  void onListItemClick(ListView l, View v, int position, long id) {
        Context context = getApplicationContext();
        CharSequence text = "Chargement du conteneur numéro " + String.valueOf(camion_List.get(position).Id);
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        Intent intentUnitaire = new Intent(camions_liste.this, Container_unitaire.class);
        EditText containerIDtxt = (EditText)findViewById(R.id.camionIDtxt);
        intentUnitaire.putExtra(CONTAINER_ID, String.valueOf(camion_List.get(position).Id));
        startActivity(intentUnitaire);
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
            case "ACCUEIL":
                Intent intentAccueil = new Intent(camions_liste.this, portail_camions.class);
                startActivity(intentAccueil);
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
                case "REQ_ALL_CAMIONS":
                    // get supervision data
                    Element allCamions = rootResp.getChild("all_camions");

                    Element Camions = allCamions.getChild("camion_sets");
                    List<Content> CamionsContent = Camions.getContent();
                    camion_List = new ArrayList<CamionModel>();
                    for(int i = 0; i < CamionsContent.size(); i++)
                    {
                        //List<Content> camionsXMLObjects = ((Element)CamionsContent.get(i)).getChild("camion").getContent();
                            CamionModel camionToAdd = new CamionModel();
                        camionToAdd.SetId(Integer.parseInt(((Element)CamionsContent.get(i)).getChild("id").getValue()));
                        camionToAdd.SetPoidsMAx(Integer.parseInt(((Element) CamionsContent.get(i)).getChild("poidsmax").getValue()));
                        camionToAdd.SetVolumeMax(Integer.parseInt(((Element)CamionsContent.get(i)).getChild("volumemax").getValue()));
                        camionToAdd.SetTypeDechetID(Integer.parseInt(((Element) CamionsContent.get(i)).getChild("typedechetsid").getValue()));
                        camionToAdd.SetDispo(Boolean.parseBoolean(((Element) CamionsContent.get(i)).getChild("disponible").getValue()));

                            //containers.add((Element)containersXMLObjects.get(j));
                            camion_List.add(camionToAdd);

                    }

                    adapter = new CamionAdapter(this, R.layout.camion_listitem, camion_List);
                    setListAdapter(adapter);
                    break;
            }
        }
    }
}
