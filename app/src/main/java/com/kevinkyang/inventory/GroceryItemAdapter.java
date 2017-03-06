package com.kevinkyang.inventory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kevin on 2/23/2017.
 */

public class GroceryItemAdapter extends ArrayAdapter<Item> {
	private DBManager dbManager;
	private GroceryFragment parent;

	public GroceryItemAdapter(Context context, ArrayList<Item> items, GroceryFragment parent) {
		super(context, 0, items);
		dbManager = DBManager.getInstance();
		this.parent = parent;
	}

	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.grocery_item, parent, false);
		}

		Item item = getItem(position);

		CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.grocery_item_checkbox);
		checkBox.setFocusable(false);
		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if (isChecked) {
					// remove this item and add it to the inventory
					GroceryItemAdapter.this.parent.removeItem(getItem(position));
				}
			}
		});

		TextView name = (TextView) convertView.findViewById(R.id.grocery_item_name);
		name.setText(item.getName());

		TextView quantity = (TextView) convertView.findViewById(R.id.grocery_item_quantity);
		quantity.setText(Integer.toString(item.getQuantity()));

		View.OnClickListener quantityListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int amount = 0;
				switch (view.getId()) {
					case R.id.grocery_item_decrease_quantity:
						amount = -1;
						break;
					case R.id.grocery_item_increase_quantity:
						amount = 1;
						break;
					default:
						break;
				}

				Item item = getItem(position);
				if (item != null) {
					item.setQuantity(item.getQuantity() + amount);
					dbManager.updateItem(item, DBSchema.TABLE_ITEMS.COL_QUANTITY);
					GroceryItemAdapter.this.parent.refresh();
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
