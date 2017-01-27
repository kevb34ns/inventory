package com.kevinkyang.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * adatper to take Item objects and populate list items using the data
 */

public class ItemAdapter extends ArrayAdapter<Item> {
	private DBManager dbManager = DBManager.getInstance();
	private TextView expiresDate;
	private TextView quantity;

	public ItemAdapter(Context context, ArrayList<Item> items) {
		super(context, 0, items);
		dbManager = DBManager.getInstance();
		expiresDate = null;
		quantity = null;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
		}

		Item item = getItem(position);

		TextView name = (TextView) convertView.findViewById(R.id.item_name);
		name.setText(item.getName());

		TextView createdDate = (TextView) convertView.findViewById(R.id.created_date);
		createdDate.setText("Created: " + item.getCreatedDate());

		expiresDate = (TextView) convertView.findViewById(R.id.expires_date);
		expiresDate.setText("Expires: " + item.getExpiresDate());

		quantity = (TextView) convertView.findViewById(R.id.quantity);
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
					dbManager.updateItem(item, DBSchema.TABLE_ITEMS.COL_QUANTITY);
					ItemAdapter.this.notifyDataSetChanged();
				}
			}
		};

		Button decQuantityButton = (Button) convertView.findViewById(R.id.decrease_quantity);
		Button incQuantityButton = (Button) convertView.findViewById(R.id.increase_quantity);
		decQuantityButton.setOnClickListener(quantityListener);
		incQuantityButton.setOnClickListener(quantityListener);

		return convertView;
	}
}
