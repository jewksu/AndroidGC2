package jewksu.androidgc2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import core.ContainerModel;
import core.ControllerCommunication;


public class TestActivity extends ActionBarActivity implements ControllerCommunication.ResponseListener {
    private static final String TAG = "TestActivity";
    ControllerCommunication controllerComm;
    String containerID = "-1";
    SeekBar remplissageSlider;
    ProgressBar tauxContainer;
    TextView tauxText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_sim);

        // create default values if first execution of application
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //Pour récupérer les données transmises par la vue précédent
        Intent intent = getIntent();
        TextView conteneurNumber = (TextView)findViewById(R.id.conteneurNumber);
        if (intent != null){
            containerID = intent.getStringExtra("ContainerID");
            conteneurNumber.setText(intent.getStringExtra("ContainerID"));
        }

        //ProgressBar qui représenter le remplissage dans le conteneur
        tauxContainer = (ProgressBar) findViewById(R.id.vertical_progressbar);
        tauxText = (TextView) findViewById(R.id.test_text);

        //On récupère le slider de remplissage pour y ajouter un évenement ProgressChanged
        remplissageSlider = (SeekBar)findViewById(R.id.SliderRemplissage);
        remplissageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tauxContainer.setProgress(remplissageSlider.getProgress());
                tauxText.setText(String.valueOf(remplissageSlider.getProgress())+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // get controller host and port from settings
        /* no need to provide default values, we have ensured in onCreate that preferences
         * have been created with default values if needed */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String server_host = sharedPref.getString("server_host", "");
        int server_port = Integer.valueOf(sharedPref.getString("server_port", "0"));

        controllerComm = new ControllerCommunication(server_host, server_port, this);

        updateSupervisionState(containerID);
    }

    @Override
    protected void onStop() {
        super.onStop();
        controllerComm.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings:
                // start settings activity
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_refresh:
                updateSupervisionState(containerID);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // request new supervision state from controller and update screen with new data
    protected void updateSupervisionState(String containerID) {
      //  controllerComm.simpleRequest("REQ_ALL_CONTAINERS");
        controllerComm.simpleRequest("REQ_SUPERVISION_STATE");
    }

    protected void sendContainerStateToController()
    {
        Element rootReq = new Element("request");
        Document request = new Document(rootReq);
        Element eltReqType = new Element("request_type");
        eltReqType.setText("CONTAINER_REPORT");
        rootReq.addContent(eltReqType);

        // add container data
        Element eltContRep = new Element("container_report");
        addFieldInt(eltContRep, "id", 3);
        addFieldInt(eltContRep, "weight", 0);
        addFieldInt(eltContRep, "volume", 0);
        addFieldInt(eltContRep, "volumemax", 200);
        rootReq.addContent(eltContRep);


        controllerComm.complexRequest(request);
    }

    // methode helper pour ajouter un champ avec valeur entière dans un Element
    private static void addFieldInt(Element eltRoot, String fieldname, int value) {
        Element elt = new Element(fieldname);
        elt.setText(String.valueOf(value));
        eltRoot.addContent(elt);
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
                   // int containerVal = Integer.parseInt(supervisionState.getChild("date_state").getValue());

                    Element Ilots = supervisionState.getChild("container_sets");
                    List<Content> IlotsContent = Ilots.getContent();
                    List<ContainerModel> containers = new ArrayList<ContainerModel>();
                    for(int i = 0; i < IlotsContent.size(); i++)
                    {
                       List<Content> containersXMLObjects = ((Element)IlotsContent.get(i)).getChild("containers").getContent();
                       for(int j =0; j < containersXMLObjects.size(); j++)
                       {
                           ContainerModel containerToAdd = new ContainerModel();
                           containerToAdd.SetId(Integer.parseInt(((Element) containersXMLObjects.get(j)).getChild("id").getValue()));
                           containerToAdd.SetPoids(Integer.parseInt(((Element) containersXMLObjects.get(j)).getChild("weight").getValue()));
                           containerToAdd.SetVolume(Integer.parseInt(((Element) containersXMLObjects.get(j)).getChild("volume").getValue()));
                           containerToAdd.SetVolumeMax(Integer.parseInt(((Element) containersXMLObjects.get(j)).getChild("volumemax").getValue()));
                           containerToAdd.SetFillRatio(Integer.parseInt(((Element) containersXMLObjects.get(j)).getChild("fillratio").getValue()));
                           containerToAdd.SetToBeCollected(Boolean.parseBoolean(((Element) containersXMLObjects.get(j)).getChild("to_be_collected").getValue()));

                           //containers.add((Element)containersXMLObjects.get(j));
                           containers.add(containerToAdd);
                       }
                    }

                    // update ProgressBar
                    int tauxContainerSelected = containers.get(Integer.parseInt(containerID)).FillRatio;
                    tauxContainer.setProgress(tauxContainerSelected);

                    // update TextView
                    tauxText.setText(tauxContainerSelected+"%");

                    //update slider
                    remplissageSlider.setProgress(tauxContainerSelected);
                    break;
            }
        }
    }

    public void buttonOnClick(View v){
        Button button = (Button)v;
        switch(button.getText().toString()) {
            case "RAFRAICHIR":
                updateSupervisionState(containerID);
                break;
            case "SAUVER":
                sendContainerStateToController();
                break;
            case "ACCUEIL":
                Intent intent = new Intent(TestActivity.this, portail_simulation.class);
                startActivity(intent);
                break;
            case "LISTE":
                Intent intentList = new Intent(TestActivity.this, conteneurs_liste.class);
                startActivity(intentList);
                break;
        }

    }
}
