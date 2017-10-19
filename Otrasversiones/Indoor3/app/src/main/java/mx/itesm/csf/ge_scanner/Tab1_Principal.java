package mx.itesm.csf.ge_scanner;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by danflovier on 10/09/2017.
 */

public class Tab1_Principal extends Fragment {

    TextView Truck;
    TextView zones;

    String id_lt = Tabs.getVariable();

    public Tab1_Principal() {}



    // Instance for Tab2 (in case we need to use it)
    public static Tab1_Principal newInstance() {
        return new Tab1_Principal();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Declare the view of the fragment
        View rootView = inflater.inflate(R.layout.tab1_principal, container, false);

        // Set the object of the Textview Truck with the one declared in the XML fi la
        Truck = (TextView) rootView.findViewById(R.id.id_ltruck);

        // Show the ID of the Lift Truck chosen by the user.
        Truck.setText("WELCOME.\n\nThe ID of the lift truck is: \n" + id_lt);
        Truck.setTextSize(20);
        Truck.setGravity(Gravity.CENTER);

        //TextView de la zona actual
        zones = (TextView) rootView.findViewById(R.id.idZonita);
        //zones.setText(Tabs.zone);

        return rootView;
    }

}
