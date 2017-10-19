package mx.itesm.csf.ge_scanner;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import static mx.itesm.csf.ge_scanner.Tabs.getInstance;

/* Resources:
https://stackoverflow.com/questions/37159217/how-to-get-data-from-a-particular-cardview-attached-recyclerview
*/

/**
 * Created by danflovier on 16/09/2017.
 */

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.MyViewHolder> {

    private List<Products> list;
    private String id_lt = Tabs.getVariable();
    private ProductsPreferences list_sP;
    private Context context;

    // Show RecyclerView with their elements and info
    // Show the objects declared in the XML file so they can be seen at the view.

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView product_name, ean;
        ImageButton delete, leave_product;
        CardView mCardView;

        MyViewHolder(View view) {
            super(view);
            mCardView = (CardView) view.findViewById(R.id.card_view);
            product_name = (TextView) view.findViewById(R.id.product);
            ean = (TextView) view.findViewById(R.id.ean);
            delete = (ImageButton) view.findViewById(R.id.card_delete);
            leave_product = (ImageButton) view.findViewById(R.id.card_leave);

        }
    }

    // Constructor
    ProductsAdapter(Context c, List<Products> list) {
        this.list = list;
        context = c;
        list_sP = new ProductsPreferences(context);
    }

    // Create new views by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.items, parent, false);

        return new MyViewHolder(itemView);
    }

    // Get element from the data entered by the user
    // at position and replace the content of the View with it
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Products product = list.get(position);
        holder.product_name.setText(product.getProduct());
        holder.ean.setText(product.getEAN());

        holder.leave_product.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Leave item at Warehouse and remove it from the list
                leaveItem(position, product);
                setText();
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Remove the item from list
                removeItem(position, product);
                setText();
            }
        });


    }

    // This function returns the size of the list created
    @Override
    public int getItemCount() {
        return list.size();
    }

    // Insert item on the list
    void insertItem(Products product) {
        list.add(product);
        list_sP.addProduct(product);
        notifyItemInserted(getItemCount());
        notifyDataSetChanged();
    }

    // Remove item from list in case the user scans twice a product by error
    private void removeItem(int position, Products product) {
        list.remove(position);
        list_sP.removeProduct(product);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, list.size());
        notifyDataSetChanged();
    }

    // Remove item from list and data base
    private void leaveItem(int position, Products product) {
        // Get value of the barcode from each product
        final String ean = product.getEAN();
        getInstance().sendToPredix(ean,id_lt,"0","2");
        list.remove(position);
        list_sP.removeProduct(product);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, list.size());
    }

    // Set message to textView in fragment depending if there's an item on the list
    private void setText(){
        if (getItemCount() == 0){
            Tab2_Products.setText();
        }
    }
}