package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.util.Patterns;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wawr1 on 27.07.2018.
 */

public class ProductProvider extends ContentProvider {
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();
    private static final int PRODUCTS = 100;

    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private ProductDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the PETS code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.products/products/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the products table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {

        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.name_empty));
        }
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRICE);
        if (price == null) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.price_empty));
        } else if (price != null && price <= 0) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.price_equal_zero));
        }

        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.quantity_empty));
        }

        String supplierName = values.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.supplier_name_empty));
        }

        String phone = values.getAsString(ProductEntry.COLUMN_SUPPLIER_PHONE);
        if (phone != null && !isPhoneValid(phone)) {
            throw new IllegalArgumentException(getContext().getResources().getString(R.string.invalid_phone));
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(ProductEntry.TABLE_NAME, null, values);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        // Show a toast message depending on whether or not the insertion was successful
        if (id == -1) {
            // If the row ID is -1, then there was an error with insertion.
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Otherwise, the insertion was successful and we can display a toast with the row ID.
        Log.v(LOG_TAG, "Saved row ID: " + id);

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.name_empty));
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.price_empty));
            } else if (price <= 0) {
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.price_equal_zero));
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.quantity_empty));
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.supplier_name_empty));
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_SUPPLIER_PHONE)) {
            String phone = values.getAsString(ProductEntry.COLUMN_SUPPLIER_PHONE);
            if (phone != null && !isPhoneValid(phone)) {
                throw new IllegalArgumentException(getContext().getResources().getString(R.string.invalid_phone));
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;    }

    private boolean isPhoneValid(String phone) {
        String phonePattern = "[+]?[0-9]{7,13}$";
        Pattern pattern;
        Matcher match;
        pattern = Pattern.compile(phonePattern);
        match = pattern.matcher(phone);

        return match.matches();
    }


}

