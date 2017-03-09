package com.kevinkyang.inventory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * adatper to take Item objects and populate list items using the data
 */

public class ItemAdapter extends ArrayAdapter<Item> {
	private DBManager dbManager;
	private InventoryFragment parent;
	private int defaultColor;

	private TextView name;
	private LinearLayout expiresContainer;
	private TextView expiresNum;
	private TextView expiresUnit;
	private TextView quantity;
	private ImageButton decQuantityButton;
	private ImageButton incQuantityButton;

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

		name = (TextView) convertView.findViewById(R.id.item_name);
		name.setText(item.getName());

		expiresContainer = (LinearLayout)
				convertView.findViewById(R.id.expires_container);
		expiresNum = (TextView)
				convertView.findViewById(R.id.expires_num);
		expiresUnit = (TextView)
				convertView.findViewById(R.id.expires_unit);

		String expiresDate = item.getExpiresDate();
		if (!expiresDate.isEmpty()) {
			int dateDifference = TimeManager.getDateDifferenceInDays(
					TimeManager.getDateTimeLocal(),
					expiresDate);
			String convertedTime = TimeManager.convertDays(dateDifference);
			String[] splitString = convertedTime.split(" ");
			expiresNum.setText(splitString[0]);
			expiresUnit.setText(splitString[1]);
			if (dateDifference < 0) {
				int color = getContext()
						.getResources()
						.getColor(
								android.R.color.holo_red_dark, null);
				setExpirationColor(color);
			} else {
				setExpirationColor(defaultColor);
			}
			setExpirationVisibility(View.VISIBLE);
		} else {
			setExpirationVisibility(View.INVISIBLE);
		}

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
					dbManager.updateItemColumn(item, DBSchema.TABLE_ITEMS.COL_QUANTITY);
					ItemAdapter.this.parent.refresh();
				}
			}
		};

		decQuantityButton = (ImageButton) convertView.findViewById(R.id.decrease_quantity);
		decQuantityButton.setFocusable(false);
		decQuantityButton.setOnClickListener(quantityListener);

		incQuantityButton = (ImageButton) convertView.findViewById(R.id.increase_quantity);
		incQuantityButton.setFocusable(false);
		incQuantityButton.setOnClickListener(quantityListener);

		return convertView;
	}

	private void setExpirationVisibility(int visibility) {
		expiresContainer.setVisibility(visibility);
		expiresNum.setVisibility(visibility);
		expiresUnit.setVisibility(visibility);
	}

	private void setExpirationColor(int color) {
		name.setTextColor(color);
		expiresNum.setTextColor(color);
		expiresUnit.setTextColor(color);
	}
}
