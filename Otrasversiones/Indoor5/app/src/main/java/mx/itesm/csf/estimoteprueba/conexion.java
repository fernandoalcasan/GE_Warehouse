package mx.itesm.csf.estimoteprueba;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.Region;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.recognition.packets.ConfigurableDevice;
import com.estimote.coresdk.recognition.packets.DeviceType;
import com.estimote.coresdk.service.BeaconManager;

import java.util.List;
import java.util.HashMap;
import java.util.UUID;

import static java.security.AccessController.getContext;

public class conexion extends AppCompatActivity {

    private BeaconManager beaconManager;
    private BeaconRegion region;
    private static final int NUM_BEACONS = 12;
    HashMap<Integer, TextView> TVBeacons = new HashMap<Integer, TextView>();
    ImageView[] img_beacons = new ImageView[NUM_BEACONS];
    BeaconRegion[] regiones = new BeaconRegion[NUM_BEACONS];
    TextView zona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_conexion);

        inicializaVariables();  //Obtiene los TextViews informativos del layout

        beaconManager = new BeaconManager(getApplicationContext());

        EstimoteSDK.initialize(getApplicationContext(), "warehouse-ge-gmail-com-s-b-dpm", "3a35682f21bd1cad60f8ecd9e2a4fc70");

        //EstimoteSDK.enableDebugLogging(true);

        region = new BeaconRegion("ranged region",UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setBackgroundScanPeriod(200, 0);

        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener()
        {
            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> beacons)
            {
                for(int i = 0; i < NUM_BEACONS; i++)
                {
                    if (beaconRegion.getIdentifier().equals(regiones[i].getIdentifier())) //Se entra a una de las regiones definidas anteriormente
                    {
                        zona.setText("ZONA = " + beacons.get(0).getMinor());
                        img_beacons[beacons.get(0).getMinor() - 1].setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                    }
                }
            }

            @Override
            public void onExitedRegion(BeaconRegion beaconRegion)
            {
                for(int i = 0; i < NUM_BEACONS; i++)
                {
                    if (beaconRegion.getIdentifier().equals(regiones[i].getIdentifier()))   //Se sale de una de las regiones definidas anteriormenre
                    {
                        img_beacons[regiones[i].getMinor() - 1].clearColorFilter();
                    }
                }
            }
        });

        beaconManager.setForegroundScanPeriod(200,0);

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
                if (!list.isEmpty())
                {
                    for(int i = 0; i < list.size(); i++)
                    {
                        int num_beacon = list.get(i).getMinor();
                        if(num_beacon <= NUM_BEACONS)
                            TVBeacons.get(num_beacon).setText("Beacon " + num_beacon + " :" + Integer.toString(list.get(i).getRssi()));
                    }
                }
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                for(int i = 0; i < NUM_BEACONS; i++)
                {
                    beaconManager.startMonitoring(regiones[i]);
                }
                beaconManager.startRanging(region);
            }
        });
    }

    protected void inicializaVariables() //Etiquetar los Textiews del layout
    {
        for(int i = 0; i < NUM_BEACONS; i++)
        {
            //Textviews de Beacons con su RSSI
            String BeaconID = "beacon" + (i+1);
            int EstID = getResources().getIdentifier(BeaconID, "id", getPackageName());
            TVBeacons.put(i+1, (TextView) findViewById(EstID));

            //Regiones 1 c/u Beacon
            regiones[i] = new BeaconRegion("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 12, i+1);

            //ImageViews de Beacons
            String ImgBeaconID = "beaconIMG" + (i+1);
            int ImgID = getResources().getIdentifier(ImgBeaconID, "id", getPackageName());
            img_beacons[i] = (ImageView) findViewById(ImgID);
        }
        //Indicador de zona (TextView)
        zona = (TextView) findViewById(R.id.rango);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        /*beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });*/
    }

    @Override
    protected void onPause() {
        //beaconManager.stopRanging(region);

        super.onPause();
    }

    public double getDistance(int rssi)
    {
        return Math.pow(10, (( -69 - rssi)/20));
    }

}
