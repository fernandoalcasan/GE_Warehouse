package mx.itesm.csf.estimoteprueba;

import android.app.Application;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton extends Application {
    // TAG of the class
    public static final String TAG = VolleySingleton.class.getSimpleName();

    // RequestQueue
    private RequestQueue mRequestQueue;

    // Instance of the SIngleton
    private static VolleySingleton mInstance;

    // Initialize new instance if it's null
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    // Singleton
    public static synchronized VolleySingleton getInstance() {
        // Return Volley new instance
        return mInstance;
    }

    // Get request queue
    public RequestQueue getRequestQueue() {
        // If mRequestQueue is null then we initialize new RequestQueue
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        // Return mRequestQueue
        return mRequestQueue;
    }

    // Add the request to the RequestQueue
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
