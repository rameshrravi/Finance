package com.psr.financial.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public class DatabaseHelper extends SQLiteOpenHelper {

    static final int COLLECT_BY_COLUMN_ADDED_VERSION = 4;
    static final int PHONE2_COLUMN_ADDED_VERSION = 6;
    static final int NO_OF_CUSTOMERS_COLUMN_ADDED_VERSION = 12;

    // Database Information
    public static final String DB_NAME = "PSR_FINANCE.DB";
    public static final String OLD_DB_NAME = "ROCKER_FINANCE.DB";
    // database version
    static final int DB_VERSION = 1;

    // Table Name
    public static final String CUSTOMERS_TABLE_NAME = "CUSTOMERS";
    public static final String TITLE_TABLE_NAME = "TITLE";
    public static final String EMI_TABLE_NAME = "EMI";

    // Table columns
    public static final String TITLE_ID = "titleId";
    public static final String TITLE_NAME = "titleName";
    public static final String INDEX_POS = "indexPos";
    public static final String NO_OF_CUSTOMERS = "noOfCustomers";

    // Table columns
    public static final String CUSTOMER_ID = "customerId";
    public static final String NAME = "name";
    public static final String PHONE = "phone";
    public static final String PLACE = "place";
    public static final String AMOUNT = "amount";
    public static final String RECEIVED_AMOUNT = "receivedAmount";
    public static final String BALANCE_AMOUNT = "balanceAmount";
    public static final String CREATED_ON = "createdOn";
    public static final String COLLECT_BY = "collectBy";
    public static final String PHONE2 = "phone2";
    public static final String LAST_PAYMENT_ID = "lastPaymentId";
    public static final String TODAY_COLLECTION_AMOUNT = "totalCollectionAmount";

    // Table columns
    public static final String EMI_ID = "emiId";
    public static final String EMI_DATE = "date";
    public static final String EMI_AMOUNT = "amount";

    public static final String REGULAR = "Regular";
    public static final String INTEREST = "Interest";

    // Creating table query
    private static final String TITLE_CREATE_TABLE = "create table " + TITLE_TABLE_NAME + "(" + TITLE_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TITLE_NAME + " TEXT NOT NULL, " + CREATED_ON + " TEXT, " + INDEX_POS + " INTEGER );";

    // Creating table query
    private static final String CUSTOMER_CREATE_TABLE = "create table " + CUSTOMERS_TABLE_NAME + "(" + CUSTOMER_ID
            + " INTEGER PRIMARY KEY, " + TITLE_ID + " TEXT NOT NULL, " + NAME + " TEXT NOT NULL, " + PHONE + " TEXT, " + PLACE + " TEXT NOT NULL, " + AMOUNT + " TEXT NOT NULL, "
            + RECEIVED_AMOUNT + " TEXT, " + BALANCE_AMOUNT + " TEXT, " + CREATED_ON + " TEXT, " + COLLECT_BY + " TEXT, " + INDEX_POS + " INTEGER);";


    // Creating table query
    private static final String EMI_CREATE_TABLE = "create table " + EMI_TABLE_NAME + "(" + TITLE_ID
            + " INTEGER, " + CUSTOMER_ID + " TEXT NOT NULL, " + EMI_ID
            + " INTEGER PRIMARY KEY, " + EMI_DATE + " TEXT NOT NULL, " + EMI_AMOUNT + " TEXT);";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        //super(context, "/mnt/sdcard/database_name.db", null, DB_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if(Build.VERSION.SDK_INT >= 28)
        {
            db.disableWriteAheadLogging();
        }
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CUSTOMER_CREATE_TABLE);
        db.execSQL(TITLE_CREATE_TABLE);
        db.execSQL(EMI_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS " + CUSTOMERS_TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS " + TITLE_TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS " + EMI_TABLE_NAME);
        if (oldVersion < COLLECT_BY_COLUMN_ADDED_VERSION && newVersion > oldVersion) {
            String query = "ALTER TABLE " + EMI_TABLE_NAME + " ADD COLUMN " + COLLECT_BY + " TEXT DEFAULT '" + REGULAR + "';";
            db.execSQL(query);
        }
        if (oldVersion < PHONE2_COLUMN_ADDED_VERSION && newVersion > oldVersion) {
            String query = "ALTER TABLE " + CUSTOMERS_TABLE_NAME + " ADD COLUMN " + PHONE2 + " TEXT;";
            db.execSQL(query);
        }
        if (oldVersion < NO_OF_CUSTOMERS_COLUMN_ADDED_VERSION && newVersion > oldVersion) {
            String query = "ALTER TABLE " + TITLE_TABLE_NAME + " ADD COLUMN " + NO_OF_CUSTOMERS + " INTEGER;";
            db.execSQL(query);
            String query1 = "ALTER TABLE " + TITLE_TABLE_NAME + " ADD COLUMN " + RECEIVED_AMOUNT + " TEXT;";
            db.execSQL(query1);
            String query2 = "ALTER TABLE " + TITLE_TABLE_NAME + " ADD COLUMN " + BALANCE_AMOUNT + " TEXT;";
            db.execSQL(query2);

            String query3 = "ALTER TABLE " + CUSTOMERS_TABLE_NAME + " ADD COLUMN " + LAST_PAYMENT_ID + " TEXT;";
            db.execSQL(query3);
            String query4 = "ALTER TABLE " + CUSTOMERS_TABLE_NAME + " ADD COLUMN " + TODAY_COLLECTION_AMOUNT + " TEXT;";
            db.execSQL(query4);
        }
        //onCreate(db);
    }
}
