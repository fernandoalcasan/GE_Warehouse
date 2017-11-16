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
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import static java.security.AccessController.getContext;

public class conexion extends AppCompatActivity {

    private BeaconManager beaconManager; //The beacon manager who is going to monitor and range the beacons
    private BeaconRegion region;    //The template of region that will follow specific beacons according to the features of it
    private int root_zone;  //The initial ranging and monitoring zone to start from
    private boolean on_first_region, on_pause, inOneZone;  //Boolean values to know if the program is paused or is goint to start ranging and monitoring
    private static final int NUM_BEACONS = 12;  //Number of beacons to use in the warehouse, in next delivery will be pulled from DB maybe
    TextView[] TVBeacons = new TextView[NUM_BEACONS];   //Array of TextViews from the layout to print the rssi values from ranging
    ImageView[] img_beacons = new ImageView[NUM_BEACONS];   //Array of ImageViews from the layout to change colors when a region was entered
    BeaconRegion[] BeaconRegions = new BeaconRegion[NUM_BEACONS];   //Array of BeaconRegions to monitor while the app is working (Zones)
    TextView zone;  //Textview to print the user's zone in the layout
    Button reg_scan;    //Button to stop and start monitoring and ranging
    HashMap<Integer, Bicon> BeaconsDeployed = new HashMap<Integer, Bicon>();
    Queue<Integer> queueZones;
    Request beaconsrequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_conexion);

        initializeVariables();  //Obtains and gives features to the informative objects in the layout

        beaconManager = new BeaconManager(getApplicationContext()); //Estimote beacons manager to manage beacons

        EstimoteSDK.initialize(getApplicationContext(), "warehouse-ge-gmail-com-s-b-dpm", "3a35682f21bd1cad60f8ecd9e2a4fc70");  //Initialize application context from estimote

        beaconsrequest = new Request(getApplicationContext());  //Initialize request to make the requests to DB in this object
        beaconsrequest.RequestBeaconsConfiguration("beacons");  //Request the beacons configuration

        //EstimoteSDK.enableDebugLogging(true); //Discomment this line to see the debugging from Estimote libraries at the console
    }

    protected void initializeVariables() //Function to pull and set the features from the objects in the layout
    {
        for(int i = 0; i < NUM_BEACONS; i++)    //According to the number of beacons to use
        {
            //Textviews of Beacons with their respective RSSI
            String BeaconID = "beacon" + (i+1);
            int EstID = getResources().getIdentifier(BeaconID, "id", getPackageName()); //Get the object source with the same name from the previous string
            TVBeacons[i] = (TextView) findViewById(EstID);  //Set the resource in the array of TextViews

            //ImageViews of Beacons
            String ImgBeaconID = "beaconIMG" + (i+1);
            int ImgID = getResources().getIdentifier(ImgBeaconID, "id", getPackageName());  //Get the object source with the same name from the previous string
            img_beacons[i] = (ImageView) findViewById(ImgID);   //Set the resource in the array of ImageViews
        }

        zone = (TextView) findViewById(R.id.rango); //Zone indicator (TextView)
        on_first_region = false;    //Boolean to know if the app will start monitoring
        on_pause = true;    //Boolean to know if the app is on pause mode
        reg_scan = (Button) findViewById(R.id.start_btn);   //Button to start and pause monitoring
        //region = new BeaconRegion("ranged region",UUID.fromString("4e6ed5ab-b3ed-4e10-8247-c5f5524d4b21"), null, null); //Setting the features for the ranging region
        queueZones = new LinkedList<Integer>(); //Initialize the queue for monitoring the zones efficiently
        inOneZone = false;

        reg_scan.setOnClickListener(new View.OnClickListener()  //Set the feature of the button when pressed
        {
            @Override
            public void onClick(View view)  //If it was pressed
            {
                if(on_pause)    //If the ranging and monitoring hasn't started or is paused
                {
                    BeaconsDeployed = beaconsrequest.GetDeployedBeacons(); //Return the beacons configuration to start the monitoring
                    setMonitoringFeatures();
                    //setRangingFeatures();
                    start_Scanning();   //Start the ranging and monitoring
                    reg_scan.setText("Pausar Monitoreo");   //Set the new text of the button
                    on_pause = false;   //Set the pause mode to false
                }
                else    //If the ranging and monitoring is working
                {
                    zone.setText("SIN ZONA");   //Set the text from the zone indicator
                    on_first_region = false;    //Set the Boolean to start monitoring and ranging to false
                    stop_Scanning();    //Stop the scanning and moitoring
                    reg_scan.setText("Iniciar Monitoreo");  //Set the new text of the button
                    on_pause = true;    //Set the pause mode to true
                }
            }
        });
    }

    protected void removeElementFromQueue(int x)    //Function to remove the zone from the queue that was exited
    {
        for(Integer in : queueZones)
        {
            if(in == x)
            {
                //Message.message(getApplicationContext(), "Se elimino la zona " + BeaconsDeployed.get(in).getZone() + " de la cola");
                queueZones.remove(in);
                break;
            }
        }
    }

    protected boolean adjacentToQueue(int x)    //Function to check if the new entered zone is adjacent to the regions in the queue where the user is
    {
        for(Integer zon: queueZones)
        {
            //Message.message(getApplicationContext(), "Buscando en los " + BeaconsDeployed.get(zon).getFloorsSize() + " pisos y las " + BeaconsDeployed.get(zon).getBiASize() + " zonas adyacentes de la zona " + BeaconsDeployed.get(zon).getZone());
            if(BeaconsDeployed.get(zon).isAdjacent(x))
                return true;
        }
        return false;
    }

    protected void setMonitoringFeatures()
    {
        beaconManager.setBackgroundScanPeriod(200, 0);  //Set the period of Monitoring that the cellphone will use to activate triggers of regions

        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener()    //Set the features of the listener for the beacon monitoring
        {
            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> beacons)    //When a region is entered
            {
                //int minor = beaconRegion.getMinor();    //Get the minor from that region
                int reg_zone = BeaconsDeployed.get(beaconRegion.getMinor()).getZone();    //Get the zone from that region

                Message.message(getApplicationContext(), "Se ingreso a la zona " + reg_zone + " con RSSI de " + beacons.get(0).getRssi());

                if(!on_first_region)    //If the region is the first to be monitored
                {
                    queueZones.clear(); //Clear data from previous monitoring
                    queueZones.add(beaconRegion.getMinor()); //Add the zone to the queue
                    root_zone = reg_zone;  //Set the root zone value equal to the minor of the region entered
                    on_first_region = true; //Set Boolean to true so now it is known that the first region was entered
                    zone.setText("ZONA: " + reg_zone); //Set the text of the zone indicator to the zone that was entered
                }

                //BeaconsDeployed.get(beaconRegion.getMinor()).isAdjacent(reg_zone)
                if(adjacentToQueue(reg_zone)) //If the new region entered is in adjacency of the previous entered regions in the queue
                {
                    if(inOneZone)   //In the case that the user is not in any area but the exit trigger didn't erase the zone from the queue
                    {
                        queueZones.remove();    //The zone that was exited long ago is removed as we entered now a new one
                        inOneZone = false;  //As the new zone was entered we must avoid to remove it the next time we enter to a zone (Example: Far Far zones)
                    }
                    queueZones.add(beaconRegion.getMinor());   //Add the zone to the queue
                    root_zone = reg_zone;  //Set the root zone value equal to the minor of the region entered
                    zone.setText("ZONA: " + reg_zone); //Set the text of the zone indicator to the zone that was entered
                    img_beacons[reg_zone - 1].setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //Set Red the image of the Beacon Region that was entered
                }
        }

        @Override
        public void onExitedRegion(BeaconRegion beaconRegion)
        {
            Message.message(getApplicationContext(), "Se salio de la zona " + BeaconsDeployed.get(beaconRegion.getMinor()).getZone());

            if(queueZones.size() > 1)
            {
                removeElementFromQueue(beaconRegion.getMinor()); //Remove the specific zone that was exited
                root_zone = BeaconsDeployed.get(queueZones.element()).getZone(); //set the global zone to the last one entered
                zone.setText("ZONA: " + root_zone);
                inOneZone = false;
            }
            else
                inOneZone = true;   //As we exited the last zone we entered we must keep it in the queue in order to enter a new zone by adjacency, so we use this boolean to know that the only zone eft in the queue was already exited

            img_beacons[BeaconsDeployed.get(beaconRegion.getMinor()).getZone() - 1].setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP); //Set Yellow the Beacon Region that was exited
        }
        });
    }

    protected void setRangingFeatures()
    {
        beaconManager.setForegroundScanPeriod(200,0);   //Set the time of ranging periods in the cellphone to range beacons

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener()  //Set the Ranging listener and its features to range the beacons
        {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) //On Beacon(s) discovered near to the cellphone
            {
                if (!list.isEmpty())    //If the list is empty  and no beacon was recognized
                {
                    for(int i = 0; i < list.size(); i++)    //Loop to check the list of beacons that were discovered
                    {
                        int num_beacon = list.get(i).getMinor();    //Get the minor from the beacon discovered
                        if(num_beacon <= NUM_BEACONS)   //If the beacon minor is on range of the beacons used
                            TVBeacons[num_beacon - 1].setText("Beacon " + num_beacon + " :" + Integer.toString(list.get(i).getRssi())); //Set the text of the layout's TextView to print the RSSI of the beacon and its name
                    }
                }
            }
        });
    }

    protected void start_Scanning() //Function to start ranging and monitoring of the functions
    {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback()  //Start the connection by the beacon manager
        {
            @Override
            public void onServiceReady()    //Check if the Estimote's app ID is correct and there's no problem in ranging and monitoring beacons
            {
                for (Bicon beacon : BeaconsDeployed.values())   //Start the monitoring of all regions
                {
                    beaconManager.startMonitoring(beacon.getRegion());
                }
                //beaconManager.startRanging(region); //Start the ranging of all beacons
            }
        });
    }

    protected void stop_Scanning()
    {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback()
        {
            @Override
            public void onServiceReady() //Check if the Estimote's app ID is correct and there's no problem in ranging and monitoring beacons
            {
                for (Bicon beacon : BeaconsDeployed.values())   //Stop the monitoring of all regions
                {
                    beaconManager.stopMonitoring(beacon.getRegionID() + beacon.getZone());
                }
                //beaconManager.stopRanging(region);  //Stop the ranging of all beacons
            }
        });
    }

    @Override
    protected void onResume() //Default function of Android Studio to know if the app is running
    {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);    //Check if requirements of the cellphone are accomplished to make the app work
    }

    @Override
    protected void onPause() //Default function of Android Studio to know if the app is on pause
    {
        super.onPause();
    }

    protected double getDistance(int rssi)  //Functin to return the distance of the beacon when it is ranged
    {
        return Math.pow(10, (( -69 - rssi)/20));
    }
}