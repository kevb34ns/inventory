package com.kevinkyang.inventory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kevin on 2/23/2017.
 */

public class GroceryItemAdapter extends ArrayAdapter<Item> {
	private TextView quantity;
	private CheckBox checkBox;

	public GroceryItemAdapter(Context context, ArrayList<Item> items) {
		super(context, 0, items);
	}

	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.grocery_item, parent, false);
		}

		Item item = getItem(position);

		checkBox = (CheckBox) convertView.findViewById(R.id.grocery_item_checkbox);

		TextView name = (TextView) convertView.findViewById(R.id.grocery_item_name);
		name.setText(item.getName());

		quantity = (TextView) convertView.findViewById(R.id.grocery_item_quantity);
		quantity.setText(Integer.toString(item.getQuantity()));

		View.OnClickListener quantityListener = new View.OnClickListener() {
			public void onClick(View view) {
				int amount = 0;
				switch (view.getId()) {
					case R.id.decrease_quantity: amount = -1; break;
					case R.id.increase_quantity: amount = 1; break;
					default: break;
				}

				Item item = getItem(position);
				if (item != null) {
					item.setQuantity(item.getQuantity() + amount);
//					dbManager.updateItem(item, DBSchema.TABLE_ITEMS.COL_QUANTITY); //TODO
					GroceryItemAdapter.this.notifyDataSetChanged();
				}
			}
		};

		Button decQuantityButton = (Button) convertView.findViewById(R.id.grocery_item_decrease_quantity);
		Button incQuantityButton = (Button) convertView.findViewById(R.id.grocery_item_increase_quantity);
		decQuantityButton.setOnClickListener(quantityListener);
		incQuantityButton.setOnClickListener(quantityListener);

		return convertView;
	}
}
