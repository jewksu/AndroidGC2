package jewksu.androidgc2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import org.jdom2.Document;
import org.jdom2.Element;

import core.ControllerCommunication;


public class Container_unitaire extends ActionBarActivity implements ControllerCommunication.ResponseListener {
    private static final String TAG = "TestActivity";
    ControllerCommunication controllerComm;
    String containerID = "-1";
    SeekBar remplissageSlider;
    ProgressBar tauxContainer;
    TextView tauxText;
    int tauxContainerSelected = 0;
    int containerVolumeMax = 0;


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

        updateContainerInfo();
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
                updateContainerInfo();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // request container info from controller and update screen with new data
    protected void updateContainerInfo() {
        Element rootReq = new Element("request");
        Document request = new Document(rootReq);
        Element eltReqType = new Element("request_type");
        eltReqType.setText("REQ_CONTAINER_INFO");
        rootReq.addContent(eltReqType);

        // add container data
        Element eltContInfo = new Element("container_info");
        addFieldInt(eltContInfo, "id", Integer.parseInt(containerID) );
        rootReq.addContent(eltContInfo);

        controllerComm.complexRequest(request);
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
        addFieldInt(eltContRep, "id", Integer.parseInt(containerID) );
        addFieldInt(eltContRep, "volume", tauxContainer.getProgress()*containerVolumeMax/100 );
        addFieldInt(eltContRep, "weight", tauxContainer.getProgress());
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
                case "RESP_CONTAINER_INFO":
                    // get supervision data
                    Element containerInfo = rootResp.getChild("container_info");

                    containerVolumeMax = Integer.valueOf(containerInfo.getChild("volume_max").getTextNormalize());
                    tauxContainerSelected = Integer.valueOf(containerInfo.getChild("volume").getTextNormalize())*100/containerVolumeMax;
                    // update ProgressBar
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
                updateContainerInfo();
                break;
            case "SAUVER":
                sendContainerStateToController();
                break;
            case "ACCUEIL":
                Intent intent = new Intent(Container_unitaire.this, portail_containers.class);
                startActivity(intent);
                break;
            case "LISTE":
                Intent intentList = new Intent(Container_unitaire.this, conteneurs_liste.class);
                startActivity(intentList);
                break;
        }

    }
}
