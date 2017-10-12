package mx.itesm.csf.ge_scanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
* Resources:
* http://androidopentutorials.com/android-how-to-store-list-of-values-in-sharedpreferences/
* */

/**
 * Created by danflovier on 09/10/2017.
 */

public class ProductsPreferences {

    // Sharedpreferences file name
    private static final String PREF_NAME = "ProductsList";

    // Key name of the data to store
    private static final String PRODUCTS = "Products";

    // SharedPreferences
    private  SharedPreferences settings;

    // Editor for Shared preferences
    private Editor editor;

    // Context
    private Context context;

    // List of products to store
    private List<Products> list;

    // Constructor
    ProductsPreferences(Context c){
        super();
        context = c;
        settings = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        list = getProducts();
    }

    // Save data of products to SharedPreference
    private void saveProducts(List<Products> list){
        Gson gson = new Gson();
        String jsonProducts = gson.toJson(list);

        editor.putString(PRODUCTS, jsonProducts);
        editor.commit();
    }

    // Add product to SharedPreference
    void addProduct(Products product){
        if (list == null) {
            list = new ArrayList<>();
        }

        list.add(product);
        saveProducts(list);
    }

    // Remove product from SharedPreference
    void removeProduct(Products product) {
        if (list != null) {
            list.remove(product);
            // Save the new data of the list
            saveProducts(list);
        }
    }

    void removeList() {
        editor.clear();
        editor.commit();
    }

    // Obtain all data from products
    ArrayList<Products> getProducts(){
        if (settings.contains(PRODUCTS)){
            String jsonProducts = settings.getString(PRODUCTS, null);
            Gson gson = new Gson();
            Products[] items = gson.fromJson(jsonProducts,Products[].class);

            list = Arrays.asList(items);
            list = new ArrayList<>(list);
        }
        else{
            return null;
        }
        return (ArrayList<Products>) list;

    }
}
