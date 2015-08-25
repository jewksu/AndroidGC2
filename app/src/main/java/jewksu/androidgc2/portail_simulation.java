package jewksu.androidgc2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jdom2.Document;
import org.jdom2.Element;

import core.ControllerCommunication;


public class portail_simulation extends ActionBarActivity implements ControllerCommunication.ResponseListener {

    private static final String TAG = "Portail_Simulation";
    final String CONTAINER_ID = "ContainerID";
    ControllerCommunication controllerComm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portail_simulation);

        // get controller host and port from settings
        /* no need to provide default values, we have ensured in onCreate that preferences
         * have been created with default values if needed */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String server_host = sharedPref.getString("server_host", "");
        int server_port = Integer.valueOf(sharedPref.getString("server_port", "0"));
        controllerComm = new ControllerCommunication(server_host, server_port, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_portail_simulation, menu);
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

    public void buttonPortailOnClick(View v){
        Button button = (Button)v;
        switch(button.getText().toString()) {
            case "Conteneur unitaire":
                Intent intentUnitaire = new Intent(portail_simulation.this, TestActivity.class);
                EditText containerIDtxt = (EditText)findViewById(R.id.containerIDtxt);
                intentUnitaire.putExtra(CONTAINER_ID, containerIDtxt.getText().toString());
                startActivity(intentUnitaire);
                break;
            case "Tous les conteneurs":
                Intent intentListe = new Intent(portail_simulation.this, conteneurs_liste.class);
                startActivity(intentListe);
                break;
        }
    }

    @Override
    public void onControllerResponse(Document response) {
        // check response type
       /* if (response != null) {
            Element rootResp = response.getRootElement();
            String responseType = rootResp.getChild("response_type").getTextNormalize().toUpperCase();
            Log.i(TAG, "Server response: " + responseType);
            switch (responseType) {
                case "RESP_SUPERVISION_STATE":
                    // get supervision data
                    Element supervisionState = rootResp.getChild("supervision_state");
                    int containerVal = Integer.parseInt(supervisionState.getChild("date_state").getValue());
                    // update TextView
                    TextView tv = (TextView) findViewById(R.id.test_text);
                    tv.setText(containerVal+"%");
                    // update ProgressBar
                    ProgressBar tauxContainer = (ProgressBar) findViewById(R.id.vertical_progressbar);
                    tauxContainer.setProgress(containerVal);
                    break;
            }
        }*/
    }
}
