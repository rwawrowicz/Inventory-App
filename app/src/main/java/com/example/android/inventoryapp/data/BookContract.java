package com.example.android.inventoryapp.data;

import android.provider.BaseColumns;

/**
 * Created by wawr1 on 23.07.2018.
 */

public final class BookContract {

    private BookContract(){}

    public static final class BookEntry implements BaseColumns {

        public final static String TABLE_NAME = "books";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME = "product_name";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_QUANTITY = "quantity";
        public final static String COLUMN_SUPPLIER_NAME = "supplier_name";
        public final static String COLUMN_SUPPLIER_PHONE = "supplier_phone";

    }
}
