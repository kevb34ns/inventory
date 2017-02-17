package com.kevinkyang.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kevin on 2/16/2017.
 */

public class SuggestionAdapter extends ArrayAdapter<SuggestionItem> {

	public SuggestionAdapter(Context context, ArrayList<SuggestionItem> items) {
		super(context, 0, items);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
		}

		SuggestionItem item = getItem(position);

		TextView name = (TextView) convertView.findViewById(R.id.item_name);
		name.setText(item.getName());

		TextView createdDate = (TextView) convertView.findViewById(R.id.created_date);
		createdDate.setText("Type: " + item.getType() + "; Exp: " + item.getDefaultExpiration() + "; Unit: " + item.getDefaultUnit());

		return convertView;
	}
}
