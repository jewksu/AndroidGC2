package jewksu.androidgc2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class portail_containers extends ActionBarActivity  {

    private static final String TAG = "Portail_Simulation";
    final String CONTAINER_ID = "ContainerID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portail_container);
        final Button UnitaryContainer = (Button)findViewById(R.id.goContainer);
        final EditText editIdTxt = (EditText)findViewById(R.id.containerIDtxt);
        UnitaryContainer.setEnabled(false);

        editIdTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editIdTxt.getText().toString() != null && !editIdTxt.getText().toString().isEmpty()) {
                    UnitaryContainer.setEnabled(true);
                }
                else
                {
                    UnitaryContainer.setEnabled(false);
                }
            }
        });
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
                Intent intentUnitaire2 = new Intent(portail_containers.this, Container_unitaire.class);
                EditText containerIDtxt = (EditText)findViewById(R.id.containerIDtxt);
                intentUnitaire2.putExtra(CONTAINER_ID, containerIDtxt.getText().toString());
                startActivity(intentUnitaire2);
                break;
            case "Tous les conteneurs":
                Intent intentListe = new Intent(portail_containers.this, conteneurs_liste.class);
                startActivity(intentListe);
                break;
        }
    }
}
