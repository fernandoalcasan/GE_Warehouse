package mx.itesm.csf.ge_scanner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/*
Resource: https://github.com/dm77/barcodescanner
*/

/**
 * Created by danflovier(A01023226) on 10/09/2017.
 */

public class Tab3_Shipload extends Fragment implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;

    private Activity activity;

    // Empty public constructor
    public Tab3_Shipload() {}

    // Instance to return the fragment
    public static Tab3_Shipload newInstance() {
        return new Tab3_Shipload();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            activity = (Activity) context;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mScannerView = new ZXingScannerView(this.getActivity().getApplication());

    }

    // Create the View of the Fragment with a ScannerView from ZXing.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
        mScannerView.setAspectTolerance(0.5f);// Start the camera with an aspect tolerance for HUAWEI devices
        //setUserVisibleHint(true);
        return mScannerView;
        */

        View rootView = inflater.inflate(R.layout.tab3_shipload, container, false);

        mScannerView = new ZXingScannerView(getActivity());
        mScannerView = (ZXingScannerView) rootView.findViewById(R.id.area);

        return rootView;

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (mScannerView != null) {
            if (isVisibleToUser) {
                onResume();
            } else {
                onPause();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCameraPreview();
        mScannerView.stopCamera();
    }

    // Making the register as a handler for scan results.
    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        // Start the camera with an aspect tolerance for HUAWEI devices
        mScannerView.setAspectTolerance(0.5f);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        //Toast.makeText(getActivity(),"CODE: "+ ean, Toast.LENGTH_SHORT).show();
        //mListener.createCopyEAN(ean);
        //((Tabs) getActivity()).getEANResult(ean);

        String ean = result.getText();

        // Handle the results
        try{
            // Send to activity the EAN the scanner registered
            ((OnFragmentInteractionListener) activity).getEANresult(ean);
        }
        catch (ClassCastException e){
            e.printStackTrace();
        }

        // Making a delay with a handler so the user can't scan twice.
        Handler handler;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(Tab3_Shipload.this);
            }
        }, 1500);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // Interaction Listener to send data to the Activity
    interface OnFragmentInteractionListener {
        void getEANresult(String ean);
    }
}