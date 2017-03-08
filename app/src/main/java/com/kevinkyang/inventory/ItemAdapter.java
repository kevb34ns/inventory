package com.kevinkyang.inventory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * adatper to take Item objects and populate list items using the data
 */

public class ItemAdapter extends ArrayAdapter<Item> {
	private DBManager dbManager;
	private InventoryFragment parent;
	private int defaultColor;

	public ItemAdapter(Context context, ArrayList<Item> items, InventoryFragment parent) {
		super(context, 0, items);
		dbManager = DBManager.getInstance();
		this.parent = parent;

		defaultColor = context.getResources().getColor(R.color.defaultTextColor, context.getTheme());
	}

	@Override
	public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
		}

		Item item = getItem(position);

		TextView name = (TextView) convertView.findViewById(R.id.item_name);
		name.setText(item.getName());

		TextView expiresNum = (TextView) convertView.findViewById(R.id.expires_num);
		TextView expiresUnit = (TextView) convertView.findViewById(R.id.expires_unit);
		int dateDifference = TimeManager.getDateDifferenceInDays(
				TimeManager.getDateTimeLocal(),
				item.getExpiresDate());
		String convertedTime = TimeManager.convertDays(dateDifference);
		String[] splitString = convertedTime.split(" ");
		expiresNum.setText(splitString[0]);
		expiresUnit.setText(splitString[1]);
		String msg = "";
		if (dateDifference < 0) {
			//TODO should factor out color change into a different method, would require turning local vars into private class members
			int color = getContext()
					.getResources()
					.getColor(
							android.R.color.holo_red_dark, null);
			name.setTextColor(color);
			expiresNum.setTextColor(color);
			expiresUnit.setTextColor(color);
			msg += "Expired " + convertedTime +
					" ago";
		} else if (dateDifference == 0) {
			name.setTextColor(defaultColor);
			expiresNum.setTextColor(defaultColor);
			expiresUnit.setTextColor(defaultColor);
			msg += "Expires today";
		} else {
			name.setTextColor(defaultColor);
			expiresNum.setTextColor(defaultColor);
			expiresUnit.setTextColor(defaultColor);
			msg += "Expires in " + convertedTime;
		}

		TextView quantity = (TextView) convertView.findViewById(R.id.quantity);
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
					dbManager.updateItemColumn(item, DBSchema.TABLE_ITEMS.COL_QUANTITY);
					ItemAdapter.this.parent.refresh();
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
