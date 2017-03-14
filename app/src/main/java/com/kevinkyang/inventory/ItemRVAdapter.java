package com.kevinkyang.inventory;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kevin on 3/13/2017.
 */

public class ItemRVAdapter extends RecyclerView.Adapter<ItemRVAdapter.ViewHolder> {
	private ArrayList<Item> items;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public TextView name;
		public String createdDate;
		public String expiresDate;
		public int quantity;
		public String unit;
		public String type;
		public String inventory;
		public boolean inGroceryList;

		public int daysUntilExpiration;

		public ViewHolder(View itemView, TextView name, String createdDate, String expiresDate, int quantity, String unit, String type, String inventory, boolean inGroceryList, int daysUntilExpiration) {
			super(itemView);
			this.name = name;
			this.createdDate = createdDate;
			this.expiresDate = expiresDate;
			this.quantity = quantity;
			this.unit = unit;
			this.type = type;
			this.inventory = inventory;
			this.inGroceryList = inGroceryList;
			this.daysUntilExpiration = daysUntilExpiration;
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return null;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {

	}

	@Override
	public int getItemCount() {
		return items.size();
	}

}
