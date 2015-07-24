package jewksu.androidgc2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.jdom2.Document;
import org.jdom2.Element;

import core.ControllerCommunication;


public class TestActivity extends ActionBarActivity implements ControllerCommunication.ResponseListener {
    private static final String TAG = "TestActivity";

    ControllerCommunication controllerComm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_sim);

        // create default values if first execution of application
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
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

        updateSupervisionState();
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
                updateSupervisionState();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // request new supervision state from controller and update screen with new data
    protected void updateSupervisionState() {
        controllerComm.simpleRequest("REQ_SUPERVISION_STATE");
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
                    String dateState = supervisionState.getChild("date_state").getTextNormalize();
                    // update TextView
                    TextView tv = (TextView) findViewById(R.id.test_text);
                    tv.setText(dateState);
                    break;
            }
        }
    }
}
