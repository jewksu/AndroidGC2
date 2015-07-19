package jewksu.androidgc2;

import android.os.AsyncTask;
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

        controllerComm = new ControllerCommunication("10.0.2.2", 10000); // virtual android device see host at 10.0.2.2
        updateSupervisionState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
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
/*
        String dateState = controllerComm.getSupervisionState();
        TextView tv = (TextView)findViewById(R.id.test_text);
        tv.setText(dateState);
*/
    }
}
