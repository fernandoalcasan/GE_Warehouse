package mx.itesm.csf.ge_scanner;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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



/**
 * Created by danflovier on 10/09/2017.
 */

public class Tabs extends AppCompatActivity implements Tab3_Shipload.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private static final int ZXING_CAMERA_PERMISSION = 1;
    private static final int FINE_LOCATION_PERMISSION = 1;

    // ViewPager that will host the section contents.
    private ViewPager mViewPager;

    static Tabs activity;

    // SQLite Database
    SQLiteAdapter helper;

    // AlertDialog
    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    //String id_lt = LoginLiftTruck.getVariable();
    static String id_lt;
    NetworkReceiver networkReceiver;

    int count = 1;
    int i;

    // Session Manager Class
    LoginPreferences session;
    ProductsPreferences list_sP;

    //Proximity Manager
    private ProximityManager proximityManager;

    // IDs de los beacons a utilizar
    private String[] beaconIds = {"6uFL", "JBlF", "qm8T", "9D2r", "Fzsb", "6iDX", "r4rh"};

    //Bluetooth Manager
    BluetoothAdapter blu = BluetoothAdapter.getDefaultAdapter();

    // Declarados 7 beacons para el demo
    private int[] beaconRssi = {0, 0, 0, 0, 0, 0, 0};
    private int[] counter = {0, 0, 0, 0, 0, 0, 0};

    //zona donde se encuentra el montacargas
    public int zone = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs);
        //Enable bluetooth for the beacons management
        blu.enable();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Initialize activity
        activity = this;

        //Beacons activities
        KontaktSDK.initialize(this);
        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.setIBeaconListener(createIBeaconListener());



        // Shared Preferences class instance for Session manager
        session = new LoginPreferences(getApplicationContext());
        list_sP = new ProductsPreferences(getApplicationContext());
        // Components to use the SQLite database
        helper = new SQLiteAdapter(this);

        // AlertDialog
        builder = new AlertDialog.Builder(this);

        // Network receiver
        networkReceiver = new NetworkReceiver();

        networkBroadcast();
        new newProgressDialog().execute();

        HashMap<String, String> user = session.getLiftTruckDetail();
        id_lt = user.get(LoginPreferences.KEY_LIFTTRUCK);

        // Check if the permission to use the CAMERA is activated on the device
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        }

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, FINE_LOCATION_PERMISSION);
        }

        // Check connectivity to Internet
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            connectivityState(true);
        } else {
            connectivityState(false);
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// BEACONS ///////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


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

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }


    private void calculateZone(int[] rssi) {

        // Si estás entre dos beacons, la suma de los rssi de esos dos beacons
        // debe ser mayor que la suma de cualquier otros dos beacons
        int zona1 = rssi[0] + rssi[1];
        int zona2 = rssi[1] + rssi[2];
        int zona3 = rssi[1] + rssi[3];
        int zona4 = rssi[3] + rssi[4];
        int zona5 = rssi[4] + rssi[5];
        int zona6 = rssi[4] + rssi[6];

        int newzone = 0;

        // Checamos si la señal de la zona 1 es la mayor
        if (zona1 > zona2 && zona1 > zona3 && zona1 > zona4 && zona1 > zona5 && zona1 > zona6) {
            //zona.setText("Zona 1");
            //barrita.setProgress(16);
            newzone = 1;
        }

        // Checamos si la señal de la zona 2 es la mayor
        if (zona2 > zona1 && zona2 > zona3 && zona2 > zona4 && zona2 > zona5 && zona2 > zona6) {
            //zona.setText("Zona 2");
            //barrita.setProgress(33);
            newzone = 2;
        }

        // Checamos si la señal de la zona 3 es la mayor
        if (zona3 > zona2 && zona3 > zona1 && zona3 > zona4 && zona3 > zona5 && zona3 > zona6) {
            //zona.setText("Zona 3");
            //barrita.setProgress(50);
            newzone = 3;
        }

        // Checamos si la señal de la zona 4 es la mayor
        if (zona4 > zona2 && zona4 > zona1 && zona4 > zona3 && zona4 > zona5 && zona4 > zona6) {
            //zona.setText("Zona 4");
            //barrita.setProgress(67);
            newzone = 4;
        }

        // Checamos si la señal de la zona 5 es la mayor
        if (zona5 > zona2 && zona5 > zona1 && zona5 > zona3 && zona5 > zona4 && zona3 > zona6) {
            //zona.setText("Zona 5");
            //barrita.setProgress(84);
            newzone = 5;
        }

        // Checamos si la señal de la zona 6 es la mayor
        if (zona6 > zona2 && zona6 > zona1 && zona6 > zona4 && zona6 > zona5 && zona6 > zona3) {
            //zona.setText("Zona 6");
            //barrita.setProgress(100);
            newzone = 6;
        }

        if (newzone != zone) {
            // Aquí se tiene que hacer el push a la base de datos
            zone = newzone;
            sendTo(zone, 1);
        }


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

                    if(ibeacon.getUniqueId() != null)
                    {
                        // Detectar beacon 0
                        if (ibeacon.getUniqueId().equals(beaconIds[0])) {
                            counter[0]++;
                            beaconRssi[0] += ibeacon.getRssi();
                        }

                        // Detectar beacon 1
                        if (ibeacon.getUniqueId().equals(beaconIds[1])) {
                            counter[1]++;
                            beaconRssi[1] += ibeacon.getRssi();
                        }

                        // Detectar beacon 2
                        if (ibeacon.getUniqueId().equals(beaconIds[2])) {
                            counter[2]++;
                            beaconRssi[2] += ibeacon.getRssi();
                        }

                        // Detectar beacon 3
                        if (ibeacon.getUniqueId().equals(beaconIds[3])) {
                            counter[3]++;
                            beaconRssi[3] += ibeacon.getRssi();
                        }

                        // Detectar beacon 4
                        if (ibeacon.getUniqueId().equals(beaconIds[4])) {
                            counter[4]++;
                            beaconRssi[4] += ibeacon.getRssi();
                        }

                        // Detectar beacon 5
                        if (ibeacon.getUniqueId().equals(beaconIds[5])) {
                            counter[5]++;
                            beaconRssi[5] += ibeacon.getRssi();
                        }

                        // Detectar beacon 6
                        if (ibeacon.getUniqueId().equals(beaconIds[6])) {
                            counter[6]++;
                            beaconRssi[6] += ibeacon.getRssi();
                        }

                        // Sacamos un promedio después de que se recibe info de algún beacon 6 veces
                        if (counter[0] >= 6 || counter[1] >= 6 || counter[2] >= 6 || counter[3] >= 6 || counter[4] >= 6 || counter[5] >= 6 || counter[6] >= 6) {

                            for (int i = 0; i <= 6; i++) {
                                if (counter[i] == 0) counter[i]++;
                            }

                            int[] rssi = {beaconRssi[0]/counter[0],
                                    beaconRssi[1]/counter[1],
                                    beaconRssi[2]/counter[2],
                                    beaconRssi[3]/counter[3],
                                    beaconRssi[4]/counter[4],
                                    beaconRssi[5]/counter[5],
                                    beaconRssi[6]/counter[6]};

                            // Actualizamos los valores que se despliegan en la pantalla
                        /*beacon1.setText("6uFL: " + rssi[0] + "\n");
                        beacon2.setText("JBlF: " + rssi[1] + "\n");
                        beacon3.setText("qm8T: " + rssi[2] + "\n");
                        beacon4.setText("9D2r: " + rssi[3] + "\n");
                        beacon5.setText("Fzsb: " + rssi[4] + "\n");
                        beacon6.setText("6iDX: " + rssi[5] + "\n");
                        beacon7.setText("r4rh: " + rssi[6] + "\n");*/

                            // Calculamos la zona
                            calculateZone(rssi);

                            // Reseteamos valores
                            for (int i = 0; i < 7; i++) {
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
            }
        };
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// NETWORK ///////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    // Set the connectivity
    public void connectivityState(boolean ans){
        int size_table = helper.getSize();
        // Validate if there's connection to Internet
        if (ans == true){
            // Validate if there's any content on the table of database
            if (size_table > 0){
                // Send saved data to the server
                sendSQLiteData();
            }
        }

    }

    // Network broadcast for Nougat
    private void networkBroadcast() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    // Unregister possibly network changes
    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// MENU /////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////


    // Inflate the menu
    // Add items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.database) {
            showDatabaseInfo();
            return true;
        }

        if (id == R.id.logout) {
            list_sP.removeList();
            session.logoutLiftTruck();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //Returns a fragment corresponding to one of the tabs.
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return Tab1_Principal.newInstance();
                case 1:
                    return Tab2_Products.newInstance();
                case 2:
                    return Tab3_Shipload.newInstance();
            }

            return null;
        }

        // Obtain the number of tabs showed in the bar(3)
        @Override
        public int getCount() {

            return 3;
        }

        // Set the title of each tab
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "PRINCIPAL";
                case 1:
                    return "PRODUCTS";
                case 2:
                    return "SHIPLOAD";
            }
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// GETTING DATA ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    // Instance to use the method
    public static Tabs getInstance(){
        return  activity;
    }

    // Activity obtains the value of the EAN registered
    @Override
    public void getEANresult(String ean) {
        String product_name = "PRODUCTO " + Integer.toString(count);

        // Create a product with the properly parameters
        Products product = new Products(i,ean,product_name,id_lt, "1", "1");

        // Counter of id and number of the product
        count++;
        i++;

        // Send the data to the database
        sendToPredix (ean, id_lt, "1", "1");

        // Send the data of the interface(s) to the function of
        // the other fragment (displayReceivedData) to manipulate the info
        String tag = "android:switcher:" + R.id.container + ":" + 1;
        Tab2_Products f = (Tab2_Products) getSupportFragmentManager().findFragmentByTag(tag);
        //f.ReceiveData(ean);
        f.addProduct(product);

        //Tab2_Products.addProduct(product);
    }


    // Get the ID of the lifttruck to show the data in other places of the app
    public static String getVariable(){
        return id_lt;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// EXTERNAL DATABASE ///////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    // Connection to the database
    public void sendToPredix(final String ean, final String mont, final String status, final String section) {

        //Toast.makeText(Tabs.this, "prueba" + ans , Toast.LENGTH_LONG).show();

        final String[] respuesta = new String[1];

        RequestQueue queue = Volley.newRequestQueue(Tabs.this);
        StringRequest sr = new StringRequest(Request.Method.POST,"http://ubiquitous.csf.itesm.mx/~Warehouse/app.php", new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {

                respuesta[0] = response.toString();
                if(respuesta[0].equals("001")) {
                    Toast.makeText(Tabs.this, "Registered (" + respuesta[0] +")\n EAN: " + ean , Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(Tabs.this, "Registered error (" + respuesta[0] + ")", Toast.LENGTH_LONG).show();

                    // Internal database
                    //id = helper.insertData(ean, mont, status, section);
                    helper.insertData(ean, mont, status, section);
                    //Check_results(id);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                respuesta[0] = "100";
                Toast.makeText(Tabs.this, "Error to connect (" + respuesta[0] + ")", Toast.LENGTH_LONG).show();
                // Internal database
                //id = helper.insertData(ean, mont, status, section);
                helper.insertData(ean, mont, status, section);
                //Check_results(id);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("ean", ean);
                params.put("mnt", mont);
                params.put("sec", section);
                params.put("s", status);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String encodedString = Base64.encodeToString(String.format("%s:%s", "app_client", "prueba123").getBytes(), Base64.NO_WRAP);
                String infoAut = String.format("Basic %s", encodedString);
                headers.put("Authorization", infoAut);
                return headers;
            }

        };

        queue.add(sr);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// INTERNAL DATABASE //////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    public void sendSQLiteData(){
        final int size_table = helper.getSize();

        final ArrayList<String> ean, id_lift_truck, status, section;

        // Retrieve data from database in the array lists
        ean = helper.getResults("Ean");
        id_lift_truck = helper.getResults("LoginLiftTruck");
        status = helper.getResults("Status");
        section = helper.getResults("Section");

        int tam = section.size();
        // Delete data from the table of the database
        helper.deleteData();

        new Runnable(){
            @Override
            public void run() {
                try {
                    // Send data to the server
                    for (int i = 0; i < size_table; i++) {
                        sendToPredix(ean.get(i),id_lift_truck.get(i), status.get(i), section.get(i));
                        //Message.message(getApplicationContext(),"P:" + ean.get(i) +"--" + id_lift_truck.get(i) + "--" + status.get(i) + "--" + section.get(i) +"\n");
                        Thread.sleep(200);
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.run();

        // Remove data from array lists
        ean.clear();
        id_lift_truck.clear();
        status.clear();
        section.clear();
    }


    public void showDatabaseInfo() {
        String database = helper.getData();

        if (!Objects.equals(database, "")) {
            builder.setTitle("DATABASE CONTENT")
                    .setMessage("ID \t\t\tEAN(BARCODE)\t\t\tLT\tST \t\tSE\n\n" + database)
                    .setPositiveButton("ACCEPT",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
            alertDialog = builder.create();
            alertDialog.getWindow().setLayout(600, 400);
            alertDialog.show();
        }
        else{
            Message.message(getApplicationContext(), "No records found");
        }
    }


    @Override
    public void onDestroy() {
        blu.disable();
        proximityManager.disconnect();
        proximityManager = null;
        super.onDestroy();
        unregisterNetworkChanges();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// ASYNCTASK //////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    private class newProgressDialog extends AsyncTask<Void,Void,Void> {
        // Initialize a new instance of progress dialog
        private ProgressDialog pd = new ProgressDialog(Tabs.this);

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            // Set the title
            pd.setTitle("Processing");
            // Set a message
            pd.setMessage("Loading...");
            // Set a style of the progress
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Sets whether the dialog is cancelable or not
            pd.setCancelable(false);
            // Show the progress dialog
            pd.show();

        }

        @Override
        protected Void doInBackground(Void...args){
            // Start Operation in a background thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                        try {
                            // Set time of showing ProgressDialog
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // Close the ProgressDialog
                        pd.dismiss();
                }
            }).start(); // Start the operation

            return null;
        }
    }


}



