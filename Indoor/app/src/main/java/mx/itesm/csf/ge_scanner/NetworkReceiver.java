package mx.itesm.csf.ge_scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/* Resource
   https://stackoverflow.com/questions/31689513/broadcastreceiver-to-detect-network-is-connected
*/

/**
 * Created by danflovier on 26/09/2017.
 */
public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (isNetworkAvailable(context)) {
                new Tabs().connectivityState(true);
            } else {
                new Tabs().connectivityState(false);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    // Check if the device is connected to Internet (Wi-Fi or Mobile)
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        // Check if the connectivity is available
        if (info != null && info.isConnectedOrConnecting()) {

            //Check if the connection is from Wi-Fi
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                if (info.isAvailable() && info.isConnectedOrConnecting()){
                    Message.message(context, "Connected to Wi-Fi");
                }
            }

            //Check if the connection is from Mobile data plan
            else if ((info.getType() == ConnectivityManager.TYPE_MOBILE)) {
                if (info.isAvailable() && info.isConnectedOrConnecting()) {
                    Message.message(context, "Connected to mobile provider's data plan");
                }
            }
            return true;
        }
        else{
            Message.message(context, "Not connected to Internet");
            return false;
        }
    }

}
