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
         //       updateSupervisionState();
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
        // build full request
        Element rootReq = new Element("monID");
        Element eltReqType = new Element("request_type");
        eltReqType.setText("CONTAINER_REPORT");
        rootReq.addContent(eltReqType);
        Document request = new Document(rootReq);
        controllerComm.complexRequest(request);
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




                    // update ProgressBar
                    ProgressBar tauxContainer = (ProgressBar) findViewById(R.id.vertical_progressbar);
                    int tauxContainerSelected = containers.get(Integer.parseInt(containerID)).FillRatio;
                    tauxContainer.setProgress(tauxContainerSelected);

                    // update TextView
                    TextView tv = (TextView) findViewById(R.id.test_text);
                    tv.setText(tauxContainerSelected+"%");
                    break;
            }
        }
    }

    public void buttonOnClick(View v){
        Button button = (Button)v;
        switch(button.getText().toString()) {
            case "GET":
                updateSupervisionState(containerID);
                break;
            case "SET":
                sendContainerStateToController();
                break;
            case "Retour":
                Intent intent = new Intent(TestActivity.this, portail_simulation.class);
                startActivity(intent);
                break;
        }

    }
}
