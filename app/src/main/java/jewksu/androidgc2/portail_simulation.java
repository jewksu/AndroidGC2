package jewksu.androidgc2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class portail_simulation extends ActionBarActivity  {

    private static final String TAG = "Portail_Simulation";
    final String CONTAINER_ID = "ContainerID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portail_simulation);
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
            case "CONTENEURS":
                Intent intentConteneurs = new Intent(portail_simulation.this, portail_containers.class);
                startActivity(intentConteneurs);
                break;
            case "CAMIONS":
                Intent intentCamions = new Intent(portail_simulation.this, portail_camions.class);
                startActivity(intentCamions);
                break;
            case "SUPERVISION":
                Intent intentSupervision = new Intent(portail_simulation.this, MapsActivity.class);
                startActivity(intentSupervision);
                break;
        }
    }
}
