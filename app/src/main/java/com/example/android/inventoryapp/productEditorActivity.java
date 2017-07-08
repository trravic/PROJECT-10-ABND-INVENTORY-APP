package com.example.android.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.ProductContract.ProductEntry;

import static com.example.android.inventoryapp.R.id.seek_bar;
import static com.example.android.inventoryapp.R.id.seekingPrice;

/**
 * Created by ravi on 6/26/2017.
 */

public class productEditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //edittext field to enter the product name
    EditText mProductEditText;

    //edittext field to enter the quantity
    EditText mQuantity;

    //edittext field to enter seller name
    EditText mSellerName;

    // to pick the image from gallery
    ImageView mImageView;

    //btn for inserting img
    Button imageButton;

    Button increase;

    Button decrease;

    //for seekbar
    private SeekBar seek_Bar;

    //textview for current progress
    private TextView seeking_Price;

    //content uri for the product
    private Uri mCurrentProductUri;

    //identfier for existing data loader
    private static final int EXISTING_PRODUCT_LOADER = 0;

    //identifier for pik imagereqst
    private static final int PICK_IMAGE_REQUEST = 0;

    //STORE OF THE PRODUCT
    private int Store = ProductEntry.STORE_UNKNOWN;

    //for spinner
    Spinner mStoreSpinner;

    Uri uri;

    private String ProductName;

    private String sellerName;

    private int price;

    private int quantity;

    private String image;

    private boolean mProductHasChanged = false;

    private static final int GIVE_PERMISSION = 1;

    //ontouchListener is used here.if the user touches on a View, implying that they are modifying
    //the view, and we change the mProductHasChanged boolean to true.
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        //examine the intent that was used to launch the activity
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app top bar to say "Add a product"
            setTitle(getString(R.string.add_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit product"
            setTitle(getString(R.string.edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
        //find all relevant views that we will need to read user input from
        mProductEditText = (EditText) findViewById(R.id.product);

        mQuantity = (EditText) findViewById(R.id.quantity_for_product);

        mSellerName = (EditText) findViewById(R.id.seller);

        seek_Bar = (SeekBar) findViewById(R.id.seek_bar);

        seeking_Price = (TextView) findViewById(R.id.seekingPrice);

        mImageView = (ImageView) findViewById(R.id.user_img);

        imageButton = (Button) findViewById(R.id.pick);

        increase = (Button) findViewById(R.id.increase);

        decrease = (Button) findViewById(R.id.decrease);

        mStoreSpinner = (Spinner) findViewById(R.id.spinner);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductEditText.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mSellerName.setOnTouchListener(mTouchListener);
        seek_Bar.setOnTouchListener(mTouchListener);
        seeking_Price.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
        mStoreSpinner.setOnTouchListener(mTouchListener);

        seekBAR();
        setupSpinner();

        //linking the button to access the img from gallery via setonclicklistener
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accessOpenImageSelector();
                mProductHasChanged = true;
            }
        });
    }

    public void accessOpenImageSelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    GIVE_PERMISSION);
            return;
        }
        openImageSelector();
    }

    //OPENIMAGESELECTOR METHOD LETS US TO OPEN IMG BY CHECKING THE BUILD VERSION OF compatible types or devices
    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //reqst permission method access the user to grant a permission by user
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GIVE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                    // permission was granted
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                uri = resultData.getData();
                mImageView.setImageURI(uri);
                mImageView.invalidate();
            }
        }
    }

    private void setupSpinner() {
        //create an adapter for spinner.so that it can use the default layout
        ArrayAdapter storeArrayadapter = ArrayAdapter.createFromResource(this, R.array.array_store, android.R.layout.simple_spinner_item);

        //style for dropdiwn
        storeArrayadapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        //apply adapter to spinner
        mStoreSpinner.setAdapter(storeArrayadapter);

        //set the integer to constant values
        mStoreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.flikart))) {
                        Store = ProductEntry.STORE_FLIPKART;
                    } else if (selection.equals(getString(R.string.an))) {
                        Store = ProductEntry.STORE_AMAZON;
                    } else if (selection.equals(getString(R.string.sd))) {
                        Store = ProductEntry.STORE_SNAPDEAL;
                    } else if (selection.equals(getString(R.string.eb))) {
                        Store = ProductEntry.STORE_EBAY;
                    } else {
                        Store = ProductEntry.STORE_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Store = ProductEntry.STORE_UNKNOWN;
            }
        });
    }

    private void seekBAR() {
        seeking_Price = (TextView) findViewById(seekingPrice);
        seek_Bar = (SeekBar) findViewById(seek_bar);
        seek_Bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress_value;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progress_value = progress;
                        seeking_Price.setText("Price:" + progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        seeking_Price.setText("Price:" + progress_value);
                        Toast.makeText(productEditorActivity.this, "your price is updated", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu_editor.xml
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.del);
            menuItem.setVisible(false);

        }
        return true;
    }

    //get user input from editor and save a product into database
    private void saveProduct() {
        //read from input fields
        //use trim to eliminate blank spaces
        String productNameString = mProductEditText.getText().toString().trim();
        String sellerNameString = mSellerName.getText().toString().trim();
        String price = seeking_Price.getText().toString().trim();
        String quantity = mQuantity.getText().toString();
        String image = mImageView.toString();


        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(productNameString) ||
                TextUtils.isEmpty(sellerNameString) ||
                TextUtils.isEmpty(quantity) ||
                TextUtils.isEmpty(image) ||
                TextUtils.isEmpty(price) ||
                Store == ProductEntry.STORE_UNKNOWN) {
            Toast.makeText(this, getString(R.string.invalid), Toast.LENGTH_SHORT).show();
            return;
        }
        //create a contentvalues where column names are keys
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(ProductEntry.COLUMN_SELLER_NAME, sellerNameString);
        values.put(ProductEntry.COLUMN_STORE, Store);
        values.put(ProductEntry.COLUMN_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_PRICE, price);
        values.put(ProductEntry.COLUMN_IMAGE, uri.toString());


        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            //show a toast msg regarding insertion
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_success), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save:
                saveProduct();
                finish();
                return true;

            //Respond to a click on the "Delete" menu option
            case R.id.del:
                showDelConfirmDialog();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(productEditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(productEditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //this method is called when the back button is pressed
    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_STORE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_SELLER_NAME,
                ProductEntry.COLUMN_IMAGE,
                ProductEntry.COLUMN_PRICE

        };

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {
            int ProductnameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int storeColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_STORE);
            int sellerNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SELLER_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int quantitycolumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE);


            // Extract out the value from the Cursor for the given column index
            ProductName = cursor.getString(ProductnameColumnIndex);
            sellerName = cursor.getString(sellerNameColumnIndex);
            int store = cursor.getInt(storeColumnIndex);
            price = cursor.getInt(priceColumnIndex);
            quantity = cursor.getInt(quantitycolumnIndex);
            image = cursor.getString(imageColumnIndex);


            // Update the views on the screen with the values from the database
            mProductEditText.setText(ProductName);
            mSellerName.setText(sellerName);
            mQuantity.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY)));
            seeking_Price.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRICE)));
            mImageView.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE))));


            //we can specify the store by constants
            switch (store) {
                case ProductEntry.STORE_AMAZON:
                    mStoreSpinner.setSelection(1);
                    break;
                case ProductEntry.STORE_FLIPKART:
                    mStoreSpinner.setSelection(2);
                    break;
                case ProductEntry.STORE_SNAPDEAL:
                    mStoreSpinner.setSelection(3);
                    break;
                case ProductEntry.STORE_EBAY:
                    mStoreSpinner.setSelection(4);
                    break;
                case ProductEntry.STORE_UNKNOWN:
                    mStoreSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //if the loader is invalidated clear out all data from the input fields
        mProductEditText.setText("");
        mStoreSpinner.setSelection(0);
        mQuantity.setText("");
        mSellerName.setText("");
        seeking_Price.setText("");

    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        //create an alert dialog box with +ve and -ve buttns
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                R.string.unsaved_msg);
        builder.setPositiveButton(R.string.neg_exit_msg, discardButtonClickListener);
        builder.setNegativeButton(R.string.pos_save_msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        //create and show the alertdialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //prompts the user to confirm whether the product was deleted
    private void showDelConfirmDialog() {
        //create an alertdialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.del_confirm);
        builder.setPositiveButton(R.string.del_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        //create and show the alertdialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    //deletion of full data
    private void deleteProduct() {
        //only perform the del if this is an existing product
        if (mCurrentProductUri != null) {
            //Call the ContentResolver to delete the product at the given content URI.
            int rowsDel = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDel == 0) {
                //if no rows were deleted then there was an error
                Toast.makeText(this, getString(R.string.editor_del_fail), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_del_ok), Toast.LENGTH_SHORT).show();
            }
        }
        //close the activity
        finish();
    }

    public void increment(View view) {
        if (quantity == 100) {
            return;
        }
        quantity = quantity + 1;
        displayQuantity(quantity);
    }

    /**
     * This method is called when the minus button is clicked.
     */
    public void decrement(View view) {
        if (quantity == 0) {
            return;
        }
        quantity = quantity - 1;
        displayQuantity(quantity);
    }

    private void displayQuantity(int newQuantity) {
        EditText quantityTextView = (EditText) findViewById(
                R.id.quantity_for_product);
        quantityTextView.setText("" + newQuantity);
    }

    //onclick is used to send a mail via intent
    public void order(View view) {

        String[] TO = {sellerName};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);


        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order " + mProductEditText.getText().toString());
        emailIntent.putExtra(Intent.EXTRA_TEXT, "ship as soon as " + mProductEditText.getText().toString() +
                " in stock " + quantity);

        mProductEditText.setText(ProductName);
        mSellerName.setText(sellerName);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}


