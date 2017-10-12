package mx.itesm.csf.ge_scanner;

import android.content.Context;
import android.content.Intent;
import java.util.HashMap;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by danflovier on 06/10/2017.
 */

public class LoginPreferences {
    // Sharedpreferences file name
    private static final String PREF_NAME = "SessionLogin";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // Key number of the lift truck to use
    static final String KEY_LIFTTRUCK = "LoginLiftTruck";

    // SharedPreferences
    private SharedPreferences settings;

    // Editor for Shared preferences
    private Editor editor;

    // Context
    private Context context;

    // Constructor
    LoginPreferences(Context c){
        super();
        context = c;
        settings = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    // Login session
    void createLoginSession(String lif_truck){
        // Storing login as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing liftruck in preferences
        editor.putString(KEY_LIFTTRUCK, lif_truck);

        // commit changes
        editor.commit();
    }

    // Get the data of the lift truck stored
    HashMap<String, String> getLiftTruckDetail(){
        HashMap<String, String> liftTruck = new HashMap<>();

        // Value of the lift truck
        liftTruck.put(KEY_LIFTTRUCK, settings.getString(KEY_LIFTTRUCK, null));

        // return the value
        return liftTruck;
    }

    // Delete session data
    void logoutLiftTruck(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // Redirect user to Login of Lif Truck
        Intent intent = new Intent(context, LoginLiftTruck.class);

        // Close Activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Flag to start Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Start Login Activity
        context.startActivity(intent);
    }

    // Get login
    boolean isLoggedIn(){
        return settings.getBoolean(IS_LOGIN, false);
    }
}
