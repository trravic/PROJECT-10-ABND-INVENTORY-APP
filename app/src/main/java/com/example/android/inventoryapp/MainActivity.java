package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.ProductContract.ProductEntry;



public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //identifier for the product data loader
    private static final int PRODUCT_LOADER_ID = 0;

    //ADAPTER FOR LISTVIEW
    productCursorAdapter mProductCursorAdapter;

    private Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup the fab to open editor activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, productEditorActivity.class);
                startActivity(intent);
            }
        });

        //LOCATE THE DATA ON THE  LIST VIEW
        ListView productListView = (ListView) findViewById(R.id.list);

        //set an empty view such that it shows only when the list has 0 items
        View emptyView = findViewById(R.id.empty_View);
        productListView.setEmptyView(emptyView);

        //setup an adapter to create a list item for each row of pet data
        //there is no petdata
        mProductCursorAdapter = new productCursorAdapter(this, null);
        productListView.setAdapter(mProductCursorAdapter);

        //setup the item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Icreate a new intent to go to editor activity
                Intent intent = new Intent(MainActivity.this, productEditorActivity.class);

                //forming the content uri such that specific row will be called
                Uri currentUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                //set the uri on the data  field
                intent.setData(currentUri);

                //launch the editoractivity
                startActivity(intent);
            }
        });

        //kick off loader
        getLoaderManager().initLoader(PRODUCT_LOADER_ID, null, this);
    }

    private void insertProduct() {
        //create a content values object where column names are the keys
        //and add the products as values
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "choclate");
        values.put(ProductEntry.COLUMN_STORE, ProductEntry.STORE_FLIPKART);
        values.put(ProductEntry.COLUMN_SELLER_NAME, "thiyagu");
        values.put(ProductEntry.COLUMN_PRICE,"50");
        values.put(ProductEntry.COLUMN_QUANTITY,"5");
        values.put(ProductEntry.COLUMN_IMAGE,R.drawable.milk);
        //insert a new row into the provider using contentresolver
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    private void deleteData() {
        int rowsDel = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("mainActivity", rowsDel + "rows deleted from db");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //will add menu to top bar by inflation
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //user clicked on menu at the top bar a dropdown list will be displayed
        switch (item.getItemId()) {
            case R.id.insert_dup_product:
                insertProduct();
                return true;

            case R.id.del_all_data:
                deleteData();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //projection--will specifies column
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_STORE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE
        };
        //the loader will execute the contentprovider query
        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProductCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductCursorAdapter.swapCursor(null);
    }
}
