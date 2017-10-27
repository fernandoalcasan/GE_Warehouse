package mx.itesm.csf.estimoteprueba;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.recognition.packets.ConfigurableDevice;
import com.estimote.coresdk.recognition.packets.DeviceType;
import com.estimote.coresdk.service.BeaconManager;

import java.util.List;
import java.util.UUID;

public class conexion extends AppCompatActivity {

    private BeaconManager beaconManager;
    private ConfigurableDevice device;
    private BeaconRegion region;
    TextView est1;
    TextView sig1;
    TextView est2;
    TextView sig2;
    TextView est3;
    TextView sig3;
    TextView est4;
    TextView sig4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_conexion);

        this.beaconManager = new BeaconManager(getApplicationContext());
        this.device = null;

        est1 =(TextView) findViewById(R.id.beacon1);
        sig1 =(TextView) findViewById(R.id.rssi1);
        est2 =(TextView) findViewById(R.id.beacon2);
        sig2 =(TextView) findViewById(R.id.rssi2);
        est3 =(TextView) findViewById(R.id.beacon3);
        sig3 =(TextView) findViewById(R.id.rssi3);
        est4 =(TextView) findViewById(R.id.beacon4);
        sig4 =(TextView) findViewById(R.id.rssi4);

        EstimoteSDK.initialize(getApplicationContext(), "warehouse-ge-gmail-com-s-b-dpm", "3a35682f21bd1cad60f8ecd9e2a4fc70");

        EstimoteSDK.enableDebugLogging(true);

        region = new BeaconRegion("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setForegroundScanPeriod(200,0);

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
                if (!list.isEmpty()) {

                    for(int i = 0; i < list.size(); i++)
                    {
                        if(list.get(i).getMinor() == 6681)
                        {
                            sig1.setText(Integer.toString(list.get(i).getRssi()));
                            //sig1.setText(Double.toString(getDistance(list.get(i).getRssi())));
                        }
                        if(list.get(i).getMinor() == 45422)
                        {
                            sig2.setText(Integer.toString(list.get(i).getRssi()));
                            //sig2.setText(Double.toString(getDistance(list.get(i).getRssi())));
                        }
                        if(list.get(i).getMinor() == 47456)
                        {
                            sig3.setText(Integer.toString(list.get(i).getRssi()));
                            //sig3.setText(Double.toString(getDistance(list.get(i).getRssi())));
                        }
                        if(list.get(i).getMinor() == 30895)
                        {
                            sig4.setText(Integer.toString(list.get(i).getRssi()));
                            //sig4.setText(Double.toString(getDistance(list.get(i).getRssi())));
                        }
                    }
                }
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
              @Override
              public void onServiceReady() {
                  beaconManager.startRanging(region);
              }
        });


        /*beaconManager.setConfigurableDevicesListener(new BeaconManager.ConfigurableDevicesListener() {
            @Override
            public void onConfigurableDevicesFound(List<ConfigurableDevice> configurableDevices) {

                for(int i = 0; i < configurableDevices.size(); i++)
                {
                    if(configurableDevices.get(i).toString() == "a")
                    {

                    }
                    est.setText(configurableDevices.get(i).toString());
                    System.out.println("Juan Camaney");
                    System.out.println(configurableDevices.get(i).toString());
                }

                // Choose here which device on the list you want to connect to (4):
                //device = configurableDevices.get(2);

            }
        });

        beaconManager.startConfigurableDevicesDiscovery();*/

    }


    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }

    public double getDistance(int rssi)
    {
        return Math.pow(10, (( -69 - rssi)/20));
    }

}
