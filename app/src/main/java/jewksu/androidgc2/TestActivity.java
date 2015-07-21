package jewksu.androidgc2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class TestActivity extends ActionBarActivity {

    // inner class to manage socket in a dedicated thread
    public class BackgroundCommunication extends AsyncTask<ControllerCommunication, Void, String> {
        @Override
        protected String doInBackground(ControllerCommunication... params) {
            return params[0].getSupervisionState();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TextView tv = (TextView)findViewById(R.id.test_text);
            tv.setText(s);
        }

    }

    ControllerCommunication controllerComm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

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

        controllerComm = new ControllerCommunication(server_host, server_port);

        updateSupervisionState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // endCommunication, in background task as well
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
        new BackgroundCommunication().execute(controllerComm);
    }
}
