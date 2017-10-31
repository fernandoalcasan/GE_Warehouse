package mx.itesm.csf.estimoteprueba;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import java.util.UUID;

import static java.security.AccessController.getContext;

public class conexion extends AppCompatActivity {

    private BeaconManager beaconManager;
    private BeaconRegion region;
    private int root_zone;
    private boolean on_first_region, on_pause;
    private static final int NUM_BEACONS = 12;
    TextView[] TVBeacons = new TextView[NUM_BEACONS];
    ImageView[] img_beacons = new ImageView[NUM_BEACONS];
    BeaconRegion[] BeaconRegions = new BeaconRegion[NUM_BEACONS];
    TextView zona;
    Button reg_scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_conexion);

        inicializaVariables();  //Obtiene los objetos informativos del layout_conexion.xml

        beaconManager = new BeaconManager(getApplicationContext());

        EstimoteSDK.initialize(getApplicationContext(), "warehouse-ge-gmail-com-s-b-dpm", "3a35682f21bd1cad60f8ecd9e2a4fc70");

        //EstimoteSDK.enableDebugLogging(true);
    }

    protected void inicializaVariables() //Etiquetar los Textiews del layout
    {
        for(int i = 0; i < NUM_BEACONS; i++)
        {
            //Textviews de Beacons con su RSSI
            String BeaconID = "beacon" + (i+1);
            int EstID = getResources().getIdentifier(BeaconID, "id", getPackageName());
            TVBeacons[i] = (TextView) findViewById(EstID);

            //Regiones 1 c/u Beacon
            BeaconRegions[i] = new BeaconRegion("ranged region " + (i+1), UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 12, i+1);

            //ImageViews de Beacons
            String ImgBeaconID = "beaconIMG" + (i+1);
            int ImgID = getResources().getIdentifier(ImgBeaconID, "id", getPackageName());
            img_beacons[i] = (ImageView) findViewById(ImgID);
        }
        //Indicador de zona (TextView)
        zona = (TextView) findViewById(R.id.rango);

        on_first_region = false;

        reg_scan = (Button) findViewById(R.id.start_btn);

        region = new BeaconRegion("ranged region",UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        on_pause = true;

        reg_scan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(on_pause)
                {
                    setMonitoringFeatures();
                    setRangingFeatures();
                    start_Scanning();
                    reg_scan.setText("Pausar Monitoreo");
                    on_pause = false;
                }
                else
                {
                    zona.setText("SIN ZONA");
                    stop_Scanning();
                    reg_scan.setText("Iniciar Monitoreo");
                    on_pause = true;
                }
            }
        });
    }

    protected void setMonitoringFeatures()
    {
        beaconManager.setBackgroundScanPeriod(200, 0);

        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener()
        {
            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> beacons)
            {
                int minor = beaconRegion.getMinor();

                if(!on_first_region)
                {
                    root_zone = minor;
                    on_first_region = true;
                    zona.setText("ZONA: " + minor);
                }

                if(minor == root_zone + 1 || minor == root_zone - 1)
                {
                    root_zone = minor;
                    zona.setText("ZONA: " + minor);
                    img_beacons[minor - 1].setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                }
            }

            @Override
            public void onExitedRegion(BeaconRegion beaconRegion)
            {
                img_beacons[beaconRegion.getMinor() - 1].setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
            }
        });
    }

    protected void setRangingFeatures()
    {
        beaconManager.setForegroundScanPeriod(200,0);

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener()
        {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list)
            {
                if (!list.isEmpty())
                {
                    for(int i = 0; i < list.size(); i++)
                    {
                        int num_beacon = list.get(i).getMinor();
                        if(num_beacon <= NUM_BEACONS)
                            TVBeacons[num_beacon - 1].setText("Beacon " + num_beacon + " :" + Integer.toString(list.get(i).getRssi()));
                    }
                }
            }
        });
    }

    protected void start_Scanning()
    {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback()
        {
            @Override
            public void onServiceReady()
            {
                for(int i = 0; i < NUM_BEACONS; i++)
                {
                    beaconManager.startMonitoring(BeaconRegions[i]);
                }
                beaconManager.startRanging(region);
            }
        });
    }

    protected void stop_Scanning()
    {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback()
        {
            @Override
            public void onServiceReady()
            {
                for(int i = 0; i < NUM_BEACONS; i++)
                {
                    beaconManager.stopMonitoring("ranged region " + (i+1));
                }
                beaconManager.stopRanging(region);
            }
        });
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
