package mx.itesm.csf.indoor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class trilatera extends AppCompatActivity {

    private ProximityManager proximityManager;
    private TextView beacon1;
    private TextView beacon2;
    private TextView beacon3;
    private TextView beacon4;
    private TextView zona;

    // Declarados 4 beacons para el demo
    private int[] beaconRssi = {0, 0, 0, 0};
    private int[] counter = {0, 0, 0, 0};

    private ProgressBar barrita;
    private int zone = 0;
    private int beaconInput = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_trilatera);

        beacon1 = (TextView) findViewById(R.id.beacon1);
        beacon2 = (TextView) findViewById(R.id.beacon2);
        beacon3 = (TextView) findViewById(R.id.beacon3);
        beacon4 = (TextView) findViewById(R.id.beacon4);
        zona = (TextView) findViewById(R.id.zona);
        barrita = (ProgressBar) findViewById(R.id.barra);
        KontaktSDK.initialize(this);

        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.setIBeaconListener(createIBeaconListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
        proximityManager.configuration()
                .scanMode(ScanMode.BALANCED)
                .scanPeriod(ScanPeriod.RANGING)
                .deviceUpdateCallbackInterval(100)
                .monitoringEnabled(true)
                .monitoringSyncInterval(10);
        startScanning();
    }

    @Override
    protected void onStop() {
        proximityManager.stopScanning();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        proximityManager.disconnect();
        proximityManager = null;
        super.onDestroy();
    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }

    private int calculateZone(int[] rssi) {

        // Si estás entre dos beacons, la suma de los rssi de esos dos beacons
        // debe ser mayor que la suma de cualquier otros dos beacons
        int zona1 = rssi[0] + rssi[1];
        int zona2 = rssi[1] + rssi[2];
        int zona3 = rssi[2] + rssi[3];

        // Checamos si la señal de la zona 1 es la mayor
        if (zona1 > zona2 && zona1 > zona3)
        {
            zona.setText("Zona 1");
            barrita.setProgress(33);
            return 1;
        }

        // Checamos si la señal de la zona 2 es la mayor
        if (zona2 > zona1 && zona2 > zona3)
        {
            zona.setText("Zona 2");
            barrita.setProgress(66);
            return 2;
        }

        // Checamos si la señal de la zona 3 es la mayor
        if (zona3 > zona2 && zona3 > zona1)
        {
            zona.setText("Zona 3");
            barrita.setProgress(100);
            return 3;
        }

        barrita.setProgress(0);
        return 0;
    }

    private void sendTo(final int z, final int m) //Petición POST para subir la ubicación en la BD
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST,"https://webservice-warehouse.run.aws-usw02-pr.ice.predix.io", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("s", "sl");
                params.put("route_id", "1");
                params.put("lift_truck_id", String.valueOf(m));
                params.put("section_id", String.valueOf(z));
                params.put("pos_x", "1");
                params.put("pos_y", "0");

                return params;
            }
        };
        queue.add(sr);
    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            /*@Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
                Log.i("Sample", "IBeacon discovered: " + ibeacon.toString());
                if (ibeacon.getUniqueId().equals("Fzsb")) {
                    //beacon1.append("Se encontró un iBeacon "  + ibeacon.getUniqueId() + "\n");
                    //beacon1.append("Su RSSI es: " + ibeacon.getRssi() + "\n");
                }
                if (ibeacon.getUniqueId().equals("6iDX")) {
                    //beacon2.append("Se encontró un iBeacon "  + ibeacon.getUniqueId() + "\n");
                    //beacon2.append("Su RSSI es: " + ibeacon.getRssi() + "\n");
                }
            }*/

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
                //Beacons updated
                for (IBeaconDevice ibeacon : iBeacons) {

                    // Detectar beacon Fzsb
                    if (ibeacon.getUniqueId().equals("Fzsb")) {
                        counter[0]++;
                        beaconRssi[0] += ibeacon.getRssi();
                    }

                    // Detectar beacon 6iDX
                    if (ibeacon.getUniqueId().equals("6iDX")) {
                        counter[1]++;
                        beaconRssi[1] += ibeacon.getRssi();
                    }

                    // Detectar beacon 9Dr2
                    if (ibeacon.getUniqueId().equals("9D2r")) {
                        counter[2]++;
                        beaconRssi[2] += ibeacon.getRssi();
                    }

                    // Detectar beacon qm8T
                    if (ibeacon.getUniqueId().equals("qm8T")) {
                        counter[3]++;
                        beaconRssi[3] += ibeacon.getRssi();
                    }

                    // Sacamos un promedio después de que se recibe info de algún beacon 6 veces
                    if (counter[0] >= 6 || counter[1] >= 6 || counter[2] >= 6 || counter[3] >= 6) {
                        int[] rssi = {beaconRssi[0]/counter[0],
                                beaconRssi[1]/counter[1],
                                beaconRssi[2]/counter[2],
                                beaconRssi[3]/counter[3]};

                        // Actualizamos los valores que se despliegan en la pantalla
                        beacon1.setText("Fzsb: " + rssi[0] + "\n");
                        beacon2.setText("6iDX: " + rssi[1] + "\n");
                        beacon3.setText("9D2r: " + rssi[2] + "\n");
                        beacon4.setText("qm8T: " + rssi[3] + "\n");

                        // Calculamos la zona
                        zone = calculateZone(rssi);

                        //Aquí va el POST con volley
                        sendTo(zone, 1);

                        // Reseteamos valores
                        for (int i = 0; i < 4; i++) {
                            counter[i] = 0;
                            beaconRssi[i] = 0;
                        }
                    }

                    // Sacamos un promedio después de que se recibe info de beacon2 6 veces
                    /*if (counter2 >= 6) {
                        beacon2.setText("NEGRO: " + beacon2rssi/counter2 + "\n");
                        calculateZone();
                        counter2 = 0;
                        beacon2rssi = 0;
                    }*/
                }
            }
        };
    }
}
