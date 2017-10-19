package mx.itesm.csf.ge_scanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;


/**
 * Created by danflovier on 25/09/2017.
 */

public class SQLiteAdapter {

    private SQLiteHelper myhelper;
    private static final String TAG = "Testing: ";


    SQLiteAdapter(Context context) {
        myhelper = new SQLiteHelper(context);
    }

    long insertData(String ean, String lift_truck, String status, String section) {

        SQLiteDatabase db = myhelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(SQLiteHelper.EAN, ean);
        values.put(SQLiteHelper.LIFT_TRUCK, lift_truck);
        values.put(SQLiteHelper.STATUS, status);
        values.put(SQLiteHelper.SECTION, section);

        long id = db.insert(SQLiteHelper.TABLE_NAME, null , values);

        //db.close();

        return id;
    }



    public String getData(){
        SQLiteDatabase db = myhelper.getWritableDatabase();

        String[] columns = {SQLiteHelper.UID, SQLiteHelper.EAN, SQLiteHelper.LIFT_TRUCK,
                SQLiteHelper.STATUS, SQLiteHelper.SECTION};

        Cursor cursor = db.query(SQLiteHelper.TABLE_NAME,columns,null,null,null,null,null);
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()){

            int cid = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UID));

            String ean = cursor.getString(cursor.getColumnIndex(SQLiteHelper.EAN));
            String  lif_truck = cursor.getString(cursor.getColumnIndex(SQLiteHelper.LIFT_TRUCK));
            String  status = cursor.getString(cursor.getColumnIndex(SQLiteHelper.STATUS));
            String  section = cursor.getString(cursor.getColumnIndex(SQLiteHelper.SECTION));

            buffer.append(cid+ "\t\t\t" + ean + "\t\t\t\t" + lif_truck + "\t\t\t" + status + "\t\t\t" + section + "\n");
        }

        db.close();
        return buffer.toString();
    }

    ArrayList<String> getResults(String column) {
        ArrayList<String> ean = new ArrayList<>();
        SQLiteDatabase db = myhelper.getWritableDatabase();

        try {
            String query = "SELECT * FROM " + SQLiteHelper.TABLE_NAME;
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                ean.add(cursor.getString(cursor.getColumnIndex(column)));
            }
            cursor.close();
            db.close();

        }catch(Exception ex){
            Log.e(TAG,"ERROR "+ ex.toString());
        }
        return ean;
    }

    int getSize(){
        SQLiteDatabase db = myhelper.getReadableDatabase();
        String query = "SELECT * FROM " + SQLiteHelper.TABLE_NAME;
        int size;

        Cursor cursor = db.rawQuery(query, null);

        size = cursor.getCount();

        cursor.close();
        db.close();

        return size;
    }

    void deleteData(){
        SQLiteDatabase db = myhelper.getWritableDatabase();
        // Delete data from table
        db.delete(SQLiteHelper.TABLE_NAME,null,null);
        // Reset ID value from table
        db.delete("sqlite_sequence","name='" + SQLiteHelper.TABLE_NAME + "'",null);
        db.close();
    }
}
