package com.kevinkyang.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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

		TextView name = (TextView) convertView.findViewById(R.id.item_name);
		Item item = getItem(position);
		name.setText(item.getName());

		return convertView;
	}
}
