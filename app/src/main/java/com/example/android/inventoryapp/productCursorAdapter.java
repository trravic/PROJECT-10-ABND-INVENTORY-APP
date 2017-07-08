package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.ProductContract.ProductEntry;

import static com.example.android.inventoryapp.ProductContract.ProductEntry.COLUMN_QUANTITY;

/**
 * Created by ravi on 6/27/2017.
 */

public class productCursorAdapter extends CursorAdapter {

    public productCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /*flags*/);
    }

    //makes a new blank list view
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //inflating a list item view using layout listItem
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, Context context, Cursor cursor) {
        //find individual views we want to modify or edit
        TextView nameTV = (TextView) view.findViewById(R.id.name);
        TextView quantityTv = (TextView) view.findViewById(R.id.quantity);
        TextView priceTv = (TextView) view.findViewById(R.id.price);
        Button sellButton = (Button) view.findViewById(R.id.sale);

        //find the column attributes
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityIndex = cursor.getColumnIndex(COLUMN_QUANTITY);
        int itemIdColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        int priceIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);

        //read the product attributes from cursor
        String productName = cursor.getString(nameColumnIndex);
        final String itemQuantity = cursor.getString(quantityIndex);
        final long itemId = cursor.getLong(itemIdColumnIndex);
        final String itemPrice = cursor.getString(priceIndex);

        //lin the textviews with the attributes for current products
        nameTV.setText(productName);
        quantityTv.setText(itemQuantity);
        priceTv.setText(itemPrice);
        final int currentQuantity = Integer.parseInt(itemQuantity);

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (currentQuantity > 0) {
                    int quantityValue = currentQuantity;

                    values.put(ProductEntry.COLUMN_QUANTITY, --quantityValue);

                    Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, itemId);
                    resolver.update(
                            uri,
                            values,
                            null,
                            null);
                }
            }
        });
    }
}
