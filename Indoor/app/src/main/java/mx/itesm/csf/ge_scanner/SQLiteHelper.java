package mx.itesm.csf.ge_scanner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
Resource:
http://abhiandroid.com/database/sqlite
https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
https://medium.com/@ssaurel/learn-to-save-data-with-sqlite-on-android-b11a8f7718d3
*/

/**
 * Created by danflovier on 25/09/2017.
 */

public class SQLiteHelper extends SQLiteOpenHelper {
    // Basic data of the database
    protected static final String DATABASE_NAME = "warehouse_db";// Name of the database
    protected static final String TABLE_NAME = "products_db";   // Name of the table
    private static final int DATABASE_Version = 1;    // Database Version

    // Create the columns of the table
    protected static final String UID = "_id";// Column one
    protected static final String EAN = "Ean";// Column two
    protected static final String LIFT_TRUCK = "LoginLiftTruck";// Column three
    protected static final String STATUS = "Status";// Column four
    protected static final String SECTION = "Section";// Column five

    // Create table
    private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
            " ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+EAN+" VARCHAR(13) ,"+ LIFT_TRUCK+" VARCHAR(13), "+STATUS+" VARCHAR(1), "+SECTION+" VARCHAR(1));";

    // Drop table
    private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;

    private Context context;

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_Version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            Message.message(context,""+e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Message.message(context,"OnUpgrade");
            db.execSQL(DROP_TABLE);
            onCreate(db);
        }catch (Exception e) {
            Message.message(context,""+e);
        }
    }


}
