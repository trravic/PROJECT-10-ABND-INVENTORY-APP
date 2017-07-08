package com.example.android.inventoryapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.ProductContract.ProductEntry;

/**
 * Created by ravi on 6/27/2017.
 */

public class productDbHelper extends SQLiteOpenHelper {

    //tag for log msgs
    public static final String LOG_TAG = productDbHelper.class.getSimpleName();

    //name of the db file
    private static final String DATABASE_NAME = "products.db";

    //db version
    private static final int DATABASE_VERSION = 3;

    public productDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " ( "
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_SELLER_NAME + " TEXT , "
                + ProductEntry.COLUMN_IMAGE + " TEXT NOT NULL , "
                + ProductEntry.COLUMN_PRICE + " INTEGER , "
                + ProductEntry.COLUMN_QUANTITY + " INTEGER NOT NULL , "
                + ProductEntry.COLUMN_STORE + " INTEGER NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
