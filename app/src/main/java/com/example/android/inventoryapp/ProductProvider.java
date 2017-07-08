package com.example.android.inventoryapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventoryapp.ProductContract.ProductEntry;

/**
 * Created by ravi on 6/27/2017.
 */

public class ProductProvider extends ContentProvider {

    //tag for log msgs
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    //uri matcher for content uri for products table
    public static final int PRODUCTS = 100;

    //URI MATCHER FOR CONTENT URI FOR SINGLE PRODUCTS TABLE
    public static final int PRODUCTS_ID = 101;

    //uri matcher object to match content uri to the corrosponding code

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //addUri will be called here for all of the contenturi
        //which is in the form of "content://com.example.android.inventoryapp/products will map to int code
        //the uri is provided to whole product table
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.INVENTORY, PRODUCTS);

        //addUri will be called here for all of the contenturi
        //which is in the form of "content://com.example.android.inventoryapp/products/4 will map to int code
        //# is used here which is substituted here .here in tis case without number doesnt match  to uri
        //the uri is provided for single product row in table
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.INVENTORY + "/#", PRODUCTS_ID);
    }

    //database of helper object
    private productDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new productDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //to get the readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //it will holds the result of the query
        Cursor cursor;

        //figure out whether the uri will matching with specific code or not???
        int spot = mUriMatcher.match(uri);
        switch (spot) {
            case PRODUCTS:
                //for the product table ,query the whole table with projection,selection,sekectionArgs,sortorder
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCTS_ID:
                //which is in the form of "content://com.example.android.inventoryapp/products/4 will map to int code
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 4 in this case.

                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("cannot query with unknown uri" + uri);
        }
        //set notification on cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int spot = mUriMatcher.match(uri);
        switch (spot) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("insertion is unsupported with unknown uri" + uri);
        }
    }

    public Uri insertProduct(Uri uri, ContentValues values) {
        //check that name is not null
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("name the product");
        }

        //check the store is valid
        Integer store = values.getAsInteger(ProductEntry.COLUMN_STORE);
        if (store == null || !ProductEntry.isValidStore(store)) {
            throw new IllegalArgumentException("product requires valid store");
        }

        //check that name is not null
        String sellername = values.getAsString(ProductEntry.COLUMN_SELLER_NAME);
        if (sellername == null) {
            throw new IllegalArgumentException("name your seller");
        }


        //if the quantity is provided than it should be  greater than or equal to 0 kg
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("product requires a valid quantity");
        }

        //if the quantity is provided than it should be  greater than or equal to 0 kg
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("product requires a valid price");
        }

        //check that image is not null
        String image = values.getAsString(ProductEntry.COLUMN_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("name the product");
        }


        //get writable db
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //insert the new product with the given vaues
        long id = db.insert(ProductEntry.TABLE_NAME, null, values);
        //if id is -1,insertion fails
        if (id == -1) {
            Log.e(LOG_TAG, "failed to insert row" + uri);
            return null;
        }

        //notify all the listeners thst data has changed for product content uri
        getContext().getContentResolver().notifyChange(uri, null);
        //return the new uri with id
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        final int spot = mUriMatcher.match(uri);
        switch (spot) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCTS_ID:
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("update is not suported for" + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        //if the key is present check the name values is nt null
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("product requires name");
            }
        }

        //if the store key is present check the store is valid
        if (values.containsKey(ProductEntry.COLUMN_STORE)) {
            Integer store = values.getAsInteger(ProductEntry.COLUMN_STORE);
            if (store == null || !ProductEntry.isValidStore(store)) {
                throw new IllegalArgumentException("product requires valid store");
            }
        }

        //if the sellername key is prsent check that name is not null
        if (values.containsKey(ProductEntry.COLUMN_SELLER_NAME)) {
            String sellername = values.getAsString(ProductEntry.COLUMN_SELLER_NAME);
            if (sellername == null) {
                throw new IllegalArgumentException("name your seller");
            }
        }

        //if the quantity key is present check that quantity is greater than or equal to 0 kg
        if (values.containsKey(ProductEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("product requires a valid quantity");
            }
        }


        //if the image key is prsent check that name is not null
        if (values.containsKey(ProductEntry.COLUMN_IMAGE)) {
            String image = values.getAsString(ProductEntry.COLUMN_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("product req image");
            }
        }


        //if the price key is present check that price  is greater than or equal to 0
        if (values.containsKey(ProductEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("product requires a valid price");
            }
        }

        //if no values arent updated dont try to update
        if (values.size() == 0) {
            return 0;
        }

        //otherwise get writable database to update data
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //perform the update on db and get the no of rows affected
        int rowsUpdated = db.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //get writable db
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //track the no of rows deleted
        int rowsDel;

        final int spot = mUriMatcher.match(uri);
        switch (spot) {
            case PRODUCTS:
                rowsDel = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCTS_ID:
                // so we know which row to delete Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))};
                rowsDel = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("delete is not supported for" + uri);
        }
        //if 1 or more rows del then notify all listeners that the data at the given uri changed
        if (rowsDel != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDel;
    }


    @Override
    public String getType(Uri uri) {
        final int spot = mUriMatcher.match(uri);
        switch (spot) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCTS_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("unknown uri" + uri + "with match" + spot);
        }
    }
}


