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
import java.util.LinkedList;
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
        RequestFloors("sectionBeaconFloor");
        RequestAdjacencies("adjacencies");
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


    public void RequestAdjacencies(final String status) //status = adjacencies
    {
        StringRequest sr = new StringRequest(com.android.volley.Request.Method.POST, URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                //Message.message(context, "response in Adjacencies");
                try
                {
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i++)
                    {
                        JSONObject adjacent = (JSONObject) array.get(i);

                        int beacon_id = adjacent.getInt("beacon_id");
                        int adjbeacon_id = adjacent.getInt("adjacent_beacon_id");
                        beacons.get(beacon_id).insertBiA(adjbeacon_id);
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
                Message.message(context, "Error to connect (adjacencies)");
            }
        }){
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


    public void RequestFloors(final String status) //status = "sectionBeaconFloor"
    {
        StringRequest sr = new StringRequest(com.android.volley.Request.Method.POST, URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                //Message.message(context, "response in Floors");
                try
                {
                    JSONArray array = new JSONArray(response);

                    for(int i = 0; i < array.length(); i++)
                    {
                        JSONObject region = (JSONObject) array.get(i);

                        if(!region.getString("beacon_minor").equals("null"))
                        {
                            int beacon_id = region.getInt("beacon_id");
                            int section_id = region.getInt("section_id");

                            if(region.getString("floor").equals("null"))
                                beacons.get(beacon_id).setFloor(1, section_id);
                            else
                                beacons.get(beacon_id).setFloor(region.getInt("floor"), section_id);
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
                //Message.message(context, "Hola: " + response);
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

                        if(beacon.getString("has_beacon").equals("t") && !beacon.getString("minor").equals("null")) //If a beacon exists in the JSONObject
                        {
                            // Obtain the info inside of every
                            // attribute inside of the object
                            String uuid = beacon.getString("uuid");
                            int major, minor, id;
                            major = minor = id = 0;

                            //Catch parsing error from the JSON Object
                            try
                            {
                                id = beacon.getInt("id");
                                major = beacon.getInt("major");
                                minor = beacon.getInt("minor");
                            }
                            catch(NumberFormatException e)
                            {
                                //Message.message(context, "El tamano del array es : " + array.length());
                                Message.message(context, "ERROR: " + e);
                            }

                            // Insert the Beacons object with the corresponding configurations in the beacons array
                            beacons.put(id, new Bicon(uuid, major, minor, id));
                            //Message.message(context, "beacon creado con minor: "+ minor + " y id: " + id);
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