package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ravi on 6/26/2017.
 */

public final class ProductContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ProductContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider
     * here content provider is the package name
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventoryapp/inventoryapp/ is a valid path for
     * looking at inventoryapp data. content://com.example.android.inventoryapp/inventoryapp/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String INVENTORY = "products";

    //each entry in a table refers to single inventory app

    public static final class ProductEntry implements BaseColumns {
        /**
         * The content URI to access the product data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, INVENTORY);

        //THE MIME TYPE FOR SINGLE PET
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + INVENTORY;

        //THE MIME TYPE FOR THE WHOLE PETS TABLE
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + INVENTORY;

        //NAME OF THE DB TABLE
        public static final String TABLE_NAME = "products";

        //unique id (int type)
        public static final String _ID = BaseColumns._ID;

        //product name
        public static final String COLUMN_PRODUCT_NAME = "name";

        //to enter the seller name
        public static final String COLUMN_SELLER_NAME = "seller_name";


        //column for the store (INT TYPE)
        public static final String COLUMN_STORE = "store";

        //separate column for the quantity
        public static final String COLUMN_QUANTITY = "quantity";

        //column for the price
        public static final String COLUMN_PRICE = "price";

        //column for image uri
        public static final String COLUMN_IMAGE = "image";

        //POSSIBLE VALUES FOR THE STORE
        public static final int STORE_UNKNOWN = 0;
        public static final int STORE_FLIPKART = 1;
        public static final int STORE_SNAPDEAL = 2;
        public static final int STORE_AMAZON = 3;
        public static final int STORE_EBAY = 4;

        //RETURNS WHETHER THE GIVEN STORE IS FLIPKART,SD,AMAZON,EBAY
        public static boolean isValidStore(int store) {
            if (store == STORE_UNKNOWN || store == STORE_AMAZON || store == STORE_FLIPKART || store == STORE_EBAY || store == STORE_SNAPDEAL) {
                return true;
            }
            return false;
        }
    }
}
