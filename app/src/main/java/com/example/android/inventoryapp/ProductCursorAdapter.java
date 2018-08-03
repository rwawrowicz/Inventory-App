package com.example.android.inventoryapp;

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
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract;

/**
 * Created by wawr1 on 29.07.2018.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.name);
        final TextView quantityTextView = view.findViewById(R.id.quantity);
        TextView priceTextView = view.findViewById(R.id.price);

        String nameString = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
        int priceInt = cursor.getInt(cursor.getColumnIndexOrThrow("price"));
        final int quantityInt = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
        final String rowId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));



        nameTextView.setText(nameString);
        String quantity_summary = String.format(context.getResources().getString(R.string.in_stock), quantityInt);
        quantityTextView.setText(quantity_summary);
        String currency_summary = context.getResources().getString(R.string.price_currency, priceInt);
        priceTextView.setText(currency_summary);

        Button sellButton = view.findViewById(R.id.sell_button);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantityInner = quantityInt;


                Uri currentItemUri = Uri.withAppendedPath(ProductContract.ProductEntry.CONTENT_URI, rowId);
                if (quantityInner > 0) {
                    quantityInner--;
                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_QUANTITY, quantityInner);
                    int updateRowId = context.getContentResolver().update(currentItemUri, values, null, null);
                    String quantity_summary = String.format(context.getResources().getString(R.string.in_stock), quantityInt);
                    quantityTextView.setText(quantity_summary);
                    if ( updateRowId == 0) {
                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(context, view.getResources().getString(R.string.unable_to_sell_product),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the update was successful and we can display a toast.
                        Toast.makeText(context, view.getResources().getString(R.string.product_sold),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, view.getResources().getString(R.string.quantity_equal_zero), Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
