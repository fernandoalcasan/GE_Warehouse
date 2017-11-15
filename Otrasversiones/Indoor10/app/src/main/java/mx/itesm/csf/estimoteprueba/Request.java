package mx.itesm.csf.estimoteprueba;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Request {
    // URL of the server
    private String URL = "https://webservice-warehouse.run.aws-usw02-pr.ice.predix.io/index.php";

    // TAG to make a Log
    private static String TAG = Request.class.getSimpleName();

    // Context of the class
    Context context;

    //SQLite adapter
    //SQLiteAdapter db;
    HashMap<Integer, Bicon> beacons = new HashMap<Integer, Bicon>();;

    // Constructor of the class
    Request(Context context){
        this.context = context;
        //db = new SQLiteAdapter(this.context);

    }

    public HashMap<Integer, Bicon> GetDeployedBeacons()
    {
        HashMap<Integer, Bicon> min_beacons = new HashMap<Integer, Bicon>();
        for (Bicon beacon : beacons.values())
        {
            min_beacons.put(beacon.getMinor(), beacon);
        }
        return min_beacons;
    }

    /*public Bicon[] GetDeployedBeacons()
    {
        Bicon[] beaconsArray = new Bicon[beacons.size()];
        for(int i = 0; i < beacons.size(); i++)
        {
            Bicon temp = beacons.remove(i);
            beaconsArray[temp.zone - 1] = temp;
        }
        return beaconsArray;
    }*/

    public void setFloorsNAdj()
    {
        for (Bicon beacon : beacons.values())
        {
            RequestFloors("sectionBeacon_id", String.valueOf(beacon.getZone()));
            RequestAdjacencies("adjacencies", String.valueOf(beacon.getZone()));
        }
    }

    // Connection to the external database (Predix)
    public void sendToPredix(final String ean, final String mont, final String status, final String section) {
        final String[] respuesta = new String[1];

        StringRequest sr = new StringRequest(com.android.volley.Request.Method.POST, URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                respuesta[0] = response;
                if(respuesta[0].equals("001")) {
                    Message.message(context,"Registered (" + respuesta[0] +")\n EAN: " + ean );
                }
                else {
                    Message.message(context,"Registered error (" + respuesta[0] + ")" );
                    //db.insertData(ean, mont, status, section);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                respuesta[0] = "100";
                Message.message(context, "Error to connect (" + respuesta[0] + ")");
                //db.insertData(ean, mont, status, section);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("ean", ean);
                params.put("mnt", mont);
                params.put("s", status);
                params.put("sec", section);
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
        VolleySingleton.getInstance().addToRequestQueue(sr);
    }


    public void RequestAdjacencies(final String status, final String BID) //status = adjacencies, BID = beacon_id
    {
        StringRequest sr = new StringRequest(com.android.volley.Request.Method.POST, URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject object = new JSONObject(response);

                    if(object.getString("status") == "001")
                    {
                        beacons.get(Integer.parseInt(BID)).initializeBiA(object.length());
                        Iterator<String> keys = object.keys();
                        int i = 0;

                        while( keys.hasNext() )
                        {
                            String key = keys.next();

                            if (key != "status")
                            {
                                JSONObject adjBeacon = object.getJSONObject(key);

                                try
                                {
                                    int adjacent_id = adjBeacon.getInt("adjacent");
                                    beacons.get(Integer.parseInt(BID)).insertBiA(i,adjacent_id); //Insert adjacent beacon to the Bicon object
                                }
                                catch(NumberFormatException e)
                                {
                                    Message.message(context, "ERROR: " + e);
                                }

                                i++;
                            }
                        }
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    Message.message(context, "Error: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Message.message(context, "Error to connect (floors)");
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("s", status);
                params.put("beacon_id", BID);
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
        VolleySingleton.getInstance().addToRequestQueue(sr);
    }


    public void RequestFloors(final String status, final String BID) //status = "sectionBeacon_id", beacon_id = BID
    {
        StringRequest sr = new StringRequest(com.android.volley.Request.Method.POST, URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject object = new JSONObject(response);
                    if(object.getString("status") == "001")
                    {
                        Iterator<String> keys = object.keys();

                        while(keys.hasNext())
                        {
                            String key = keys.next();

                            if (key != "status")
                            {
                                JSONObject section = object.getJSONObject(key);

                                int beacon_id, section_id, floor;
                                beacon_id = section_id = floor = 0;

                                try
                                {
                                    beacon_id = section.getInt("beacon_id");
                                    section_id = section.getInt("id");
                                    if(section.getString("floor") != "null")
                                        floor = section.getInt("floor");

                                    beacons.get(beacon_id).floors.put(floor, section_id);
                                }
                                catch(NumberFormatException e)
                                {
                                    Message.message(context, "ERROR: " + e);
                                }
                            }
                        }
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    Message.message(context, "Error: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Message.message(context, "Error to connect (floors)");
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("s", status);
                params.put("beacon_id", BID);
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
        VolleySingleton.getInstance().addToRequestQueue(sr);
    }

    // Make JSON array request with ([)
    public void RequestBeaconsConfiguration(final String status) {
        StringRequest sr = new StringRequest(com.android.volley.Request.Method.POST,URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                try {
                    // Transform the response into JSONArray
                    JSONArray array = new JSONArray(response);

                    beacons = new HashMap<Integer, Bicon>();

                    // Go through every index of the array
                    // for reading the data and save it in the JSONObject
                    for (int i = 0; i < array.length(); i++)
                    {

                        // Save info of the array in the JSONObject
                        JSONObject beacon = (JSONObject) array.get(i);

                        if(beacon.getString("has_beacon") == "t") //If a beacon exists in the JSONObject
                        {
                            // Obtain the info inside of every
                            // attribute inside of the object
                            String uuid = beacon.getString("uuid");
                            int major, minor, hall, v1, v2, id;
                            major = minor = hall = v1 = v2 = id = 0;
                            float posX, posY;
                            posX = posY = 0;

                            //Catch parsing error from the JSON Object
                            try {
                                id = Integer.parseInt(beacon.getString("id"));
                                major = Integer.parseInt(beacon.getString("major"));
                                minor = Integer.parseInt(beacon.getString("minor"));
                                v1 = Integer.parseInt(beacon.getString("vertex_1"));
                                v2 = Integer.parseInt(beacon.getString("vertex_2"));
                                if(beacon.getString("hall") != "null")
                                    hall = Integer.parseInt(beacon.getString("hall"));
                                if(beacon.getString("x") != "null")
                                    posX = Float.parseFloat(beacon.getString("x"));
                                if(beacon.getString("y") != "null")
                                    posY = Float.parseFloat(beacon.getString("y"));
                            }
                            catch(NumberFormatException e)
                            {
                                Message.message(context, "ERROR: " + e);
                            }

                            // Insert the Beacons object with the corresponding configurations in the beacons array
                            beacons.put(id, new Bicon(uuid, major, minor, hall, posX, posY, id, v1, v2));
                        }
                    }
                    setFloorsNAdj(); //Set the floors and the adjacencies in the hashmap of the Bicon class objects
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    Message.message(context, "Error: " + e.getMessage());
                }
            }
        }
        , new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(context, "Error to connect" + error.toString(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
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
        VolleySingleton.getInstance().addToRequestQueue(sr);
    }
}


///////REQUESTS TO DO/////////
//SELECT max(floor) FROM public.sections WHERE beacon_id = x;
//SELECT * FROM public.sections WHERE type = x;
//SELECT * FROM public.sections WHERE beacon_id = x;