package mx.itesm.csf.ge_scanner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import android.hardware.camera2.CameraDevice;

/* Resources for building the RecyclerView:
    https://developer.android.com/samples/RecyclerView/src/com.example.android.recyclerview/CustomAdapter.html
    https://www.androidhive.info/2016/01/android-working-with-recycler-view/
    https://medium.com/@orafaaraujo/lists-with-recyclerview-8eb9d9e84149
    https://github.com/orafaaraujo/RxRecyclerExample
*/

/**
 * Created by danflovier on 10/09/2017.
 */


public class Tab2_Products extends Fragment {

    // Data from barcode
    static TextView message;

    // Recyclerview
    private RecyclerView rv;
    private List<Products> list = new ArrayList<>();
    private ProductsAdapter adapter;

    ProductsPreferences list_sP;

    // Empty public constructor
    public Tab2_Products() {}

    // Instance for Tab2
    public static Tab2_Products newInstance() {
        return new Tab2_Products();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list_sP = new ProductsPreferences(getActivity());

        if (list_sP.getProducts() != null){
            list = list_sP.getProducts();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.tab2_products, container, false);

        // RecyclerView implementation
        // Reference
        rv = (RecyclerView) rootView.findViewById(R.id.rv_recycler_view);

        // Ensure that the size of the RecyclerView won't be changing
        rv.setHasFixedSize(true);

        // Adapter for RecyclerView
        adapter = new ProductsAdapter(getActivity(),list);
        rv.setAdapter(adapter);

        // Layout Manager
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        // Simple Animation
        rv.setItemAnimator(new DefaultItemAnimator());

        // Display a message when there's no products on the list
        message = (TextView) rootView.findViewById(R.id.barcode_result);

        if (list.size() <= 0) {
            setText();
        }

        return rootView;
    }


    // Data to add in the list of products
    public void addProduct(Products product) {
        // Insert the product in the list
        adapter.insertItem(product);

        // We restart the textView info
        message.setText("");
    }

    // Set info on the textView
    public static void setText (){
        message.setText("Empty list.");
        message.setTextSize(20);
        message.setGravity(Gravity.CENTER);
    }

}
