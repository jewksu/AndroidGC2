package jewksu.androidgc2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.jdom2.Document;
import org.jdom2.Element;

import java.util.List;

import core.ControllerCommunication;

public class MapsActivity extends ActionBarActivity implements ControllerCommunication.ResponseListener {
    private static final String TAG = "MapsActivity";

    private ControllerCommunication controllerComm;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Document lastCircuits;
    private MenuItem[] dechetMenuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        // create default values if first execution of application
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);

        // keep reference on menu items
        dechetMenuItems = new MenuItem[3];
        dechetMenuItems[0] = menu.findItem(R.id.action_dechet1);
        dechetMenuItems[1] = menu.findItem(R.id.action_dechet2);
        dechetMenuItems[2] = menu.findItem(R.id.action_dechet3);
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        controllerComm.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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

            case R.id.action_trigcircuitscalc:
                trigCircuitsCalc();
                return true;

            case R.id.action_supervision:
                updateSupervisionState();
                return true;

            case R.id.action_circuits:
                updateCircuits();
                return true;

            case R.id.action_dechet1:
            case R.id.action_dechet2:
            case R.id.action_dechet3:
                // toggle menu item
                item.setChecked(!item.isChecked());
                // refresh screen
                onControllerResponse(lastCircuits);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // request calculator to compute circuits
    protected void trigCircuitsCalc() {
        controllerComm.simpleRequest("TRIG_CIRCUIT_COMPUTATION");
    }

    // request new supervision state from controller and update screen with new data
    protected void updateSupervisionState() {
        controllerComm.simpleRequest("REQ_SUPERVISION_STATE");
    }

    // request new supervision state from controller and update screen with new data
    protected void updateCircuits() {
        controllerComm.simpleRequest("REQ_CIRCUITS");
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
                    mMap.clear(); // remove all markers, polylines, polygons, overlays, etc from the map.

                    // get supervision data
                    Element supervisionState = rootResp.getChild("supervision_state");
                    for (Element containerSet: supervisionState.getChild("container_sets").getChildren("container_set")) {
                        // create a marker for each container_set, colored in red (to be collected) or green (otherwise)
                        mMap.addMarker(new MarkerOptions()
                                .position(getLocation(containerSet.getChild("location")))
                                .draggable(false)
                                .icon(BitmapDescriptorFactory.defaultMarker(containerSet.getChild("to_be_collected").getTextNormalize().equals("false") ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED))
                        );
                    }
                    break;

                case "RESP_CIRCUITS":
                    lastCircuits = response; // keep latest circuit response to refresh screen when toggling dechet

                    mMap.clear(); // remove all markers, polylines, polygons, overlays, etc from the map.

                    Element circuits = rootResp.getChild("circuits");
                    /* not available anymore
                    // put in green container sets that are not collected
                    for (Element containerSet: circuits.getChild("not_collected").getChild("container_sets").getChildren("container_set")) {
                        mMap.addMarker(new MarkerOptions()
                                        .position(getLocation(containerSet.getChild("location")))
                                        .draggable(false)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        );
                    }*/

                    // for each circuit, use a different color for markers + polyline
                    // to show the circuit depot -> container sets in order -> depot
                    List<Element> eltCircuits = circuits.getChildren("circuit");
                    for (int dechet_id = 3; dechet_id > 0; dechet_id--) { // on traite a l'envers, plus esthetique d'afficher la collecte de verre en premier (donc en fond)
                        if (dechetMenuItems[dechet_id-1].isChecked()) {
                            int nb_circuits_dechet = 0;
                            for (Element circuit : eltCircuits) {
                                if (Integer.valueOf(circuit.getChild("dechet_id").getTextNormalize()) == dechet_id)
                                    nb_circuits_dechet++;
                            }

                            float colorHue;
                            float colorHueEnd;
                            switch (dechet_id) {
                                case 1: // tout venant: nuances de bleu
                                    colorHue = 190;
                                    colorHueEnd = 260;
                                    break;
                                case 2: // recyclage: nuances de vert
                                    colorHue = 80;
                                    colorHueEnd = 150;
                                    break;
                                case 3: // verre: jaune
                                    colorHue = 60;
                                    colorHueEnd = 60;
                                    break;
                                default: // rouge
                                    colorHue = 0;
                                    colorHueEnd = 0;
                                    break;
                            }
                            float colorHue_step = nb_circuits_dechet > 1 ? (colorHueEnd - colorHue) / (nb_circuits_dechet - 1) : 0;

                            for (Element circuit : eltCircuits) {
                                if (Integer.valueOf(circuit.getChild("dechet_id").getTextNormalize()) == dechet_id) {

                                    LatLng depotLocation = getLocation(circuit.getChild("depot_location"));

                                    // use polyline to display circuit order
                                    PolylineOptions polylineOpt = new PolylineOptions()
                                            .color(Color.HSVToColor(255, new float[]{colorHue, 0.75f, 1}));

                                    // start from depot
                                    polylineOpt.add(depotLocation);

                                    for (Element containerSet : circuit.getChild("container_sets").getChildren("container_set")) {
                                        // add marker and update polyline for each container set
                                        LatLng location = getLocation(containerSet.getChild("location"));
                                        mMap.addMarker(new MarkerOptions()
                                                        .position(location)
                                                        .draggable(false)
                                                        .icon(BitmapDescriptorFactory.defaultMarker(colorHue))
                                        );
                                        polylineOpt.add(location);
                                    }

                                    // end at depot
                                    polylineOpt.add(depotLocation);

                                    mMap.addPolyline(polylineOpt);

                                    // determine color for next circuit
                                    colorHue += colorHue_step;
                                    if (colorHue >= 360)
                                        colorHue -= 360; // color roll-over
                                }
                            }
                        }
                    }
                    break;

                case "OK":
                case "ERROR":
                default:
                    Toast.makeText(getApplicationContext(), "Réponse contrôleur: "+responseType, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    // convert a location element to LatLng
    private static LatLng getLocation(Element location) {
        return new LatLng(Double.valueOf(location.getChild("latitude").getTextNormalize()),Double.valueOf(location.getChild("longitude").getTextNormalize()));
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // set position for emulator
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(43.602704, 1.441745))
                .zoom(12)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
