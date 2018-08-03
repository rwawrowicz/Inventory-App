package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by wawr1 on 23.07.2018.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "storage.db";
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String SQL_CREATE_PRODUCTS_TABLE = " CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + ProductEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_SUPPLIER_PHONE + " TEXT);";

        sqLiteDatabase.execSQL(SQL_CREATE_PRODUCTS_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // TODO: onUpgrade

    }
}
