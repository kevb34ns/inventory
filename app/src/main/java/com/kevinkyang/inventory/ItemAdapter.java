package com.kevinkyang.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * adatper to take Item objects and populate list items using the data
 */

public class ItemAdapter extends ArrayAdapter<Item> {

	public ItemAdapter(Context context, ArrayList<Item> items) {
		super(context, 0, items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
		}

		Item item = getItem(position);

		TextView name = (TextView) convertView.findViewById(R.id.item_name);
		name.setText(item.getName());

		TextView createdDate = (TextView) convertView.findViewById(R.id.created_date);
		createdDate.setText("Created: " + item.getCreatedDate());

		TextView expiresDate = (TextView) convertView.findViewById(R.id.expires_date);
		expiresDate.setText("Expires: " + item.getExpiresDate());

		TextView quantity = (TextView) convertView.findViewById(R.id.quantity);
		quantity.setText("Quantity: " + item.getQuantity());

		return convertView;
	}
}
