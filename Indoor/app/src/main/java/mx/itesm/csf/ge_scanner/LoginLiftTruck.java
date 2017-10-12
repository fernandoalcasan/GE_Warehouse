package mx.itesm.csf.ge_scanner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by danflovier on 10/09/2017.
 */

public class LoginLiftTruck extends AppCompatActivity{
    private String id_lifttruck="";
    // ProgressDialog
    //ProgressDialog progressDialog;

    // Session Manager Class
    private LoginPreferences session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.lift_truck);

        final Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        final Button button = (Button) findViewById(R.id.button1);
        // Session Manager
        session = new LoginPreferences(getApplicationContext());

        Toast.makeText(getApplicationContext(), "User login status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        // Adapting the string array and the spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.liftTruck_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // Until the user choose an option, the button inside the Activity will be enable
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String id_lt = parent.getItemAtPosition(position).toString();
                id_lifttruck = id_lt;

                // Behavior of the button depending of the option selected on the Spinner
                if(position != 0){
                    button.setClickable(true);
                    button.setEnabled(true);
                    button.setTextColor(Color.parseColor("#FFFFFF"));
                    button.setBackgroundColor(Color.parseColor("#2DCC70"));
                }
                else{
                    button.setClickable(false);
                    button.setEnabled(false);
                    button.setTextColor(Color.parseColor("#BB1D0E"));
                    button.setBackgroundColor(Color.parseColor("#E84C3D"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                progressDialog = new ProgressDialog(LoginLiftTruck.this);
                // Set the title
                progressDialog.setTitle("Processing");
                // Set a message
                progressDialog.setMessage("Loading...");
                // Set a style of the progress
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                // Show the progress dialog
                progressDialog.show();

                // Sets whether the dialog is cancelable or not
                progressDialog.setCancelable(false);

                // We create a thread to make a delay
                // in the progress dialog
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();

                        // Make an Intent to move to the another activity
                        Intent intent = new Intent(LoginLiftTruck.this, Tabs.class);
                        startActivity(intent);
                    }
                }).start();
                */
                session.createLoginSession(id_lifttruck);
                Intent intent = new Intent(LoginLiftTruck.this, Tabs.class);
                startActivity(intent);
            }
        });

    }
    /*
    // Get the ID of the lifttruck to show the data in other places of the app
    public static String getVariable(){
        return id_lifttruck;
    }*/
}


