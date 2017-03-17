package com.kevinkyang.inventory;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kevin on 3/16/2017.
 */

public class GroceryItemRVAdapter
		extends RecyclerView.Adapter<GroceryItemRVAdapter.ViewHolder> {
	private ArrayList<Item> items;
	private GroceryFragment parent;
	private DBManager dbManager;

	public GroceryItemRVAdapter(ArrayList<Item> items,
								GroceryFragment parent) {
		this.items = items;
		this.parent = parent;
		dbManager = DBManager.getInstance();
	}

	@Override
	public GroceryItemRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.grocery_item, parent, false);

		CheckBox checkBox = (CheckBox) view.findViewById(R.id.grocery_item_checkbox);
		checkBox.setFocusable(false);

		TextView name = (TextView) view.findViewById(R.id.grocery_item_name);
		TextView quantity = (TextView) view.findViewById(R.id.quantity);
		TextView quantityUnit = (TextView) view.findViewById(R.id.quantity_unit);
		ImageButton decQuantityButton = (ImageButton) view.findViewById(R.id.decrease_quantity);
		decQuantityButton.setFocusable(false);
		ImageButton incQuantityButton = (ImageButton) view.findViewById(R.id.increase_quantity);
		incQuantityButton.setFocusable(false);

		ViewHolder.ViewHolderClickListener listener = new ViewHolder.ViewHolderClickListener() {
			@Override
			public void onClick(int position) {
				// TODO pop edit dialog
			}
		};

		return new ViewHolder(view, checkBox, name, quantity, quantityUnit, decQuantityButton, incQuantityButton, listener);
	}

	@Override
	public void onBindViewHolder(final GroceryItemRVAdapter.ViewHolder holder, int position) {
		Item item = items.get(position);

		holder.checkBox.setSelected(false);
		holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Item item = items.get(holder.getAdapterPosition());
				GroceryItemRVAdapter.this.parent
						.removeItem(item, holder.getAdapterPosition());
				showSnackbar(item, parent.getView(),
						holder.getAdapterPosition());
			}
		});

		holder.name.setText(item.getName());
		holder.quantity.setText(Integer.toString(item.getQuantity()));

		String unitString = item.getUnit().trim();
		if (unitString.isEmpty()) {
			holder.quantityUnit.setVisibility(View.GONE);
		} else {
			holder.quantityUnit.setText(item.getUnit());
			holder.quantityUnit.setVisibility(View.VISIBLE);
		}

		View.OnClickListener quantityListener =
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO quantity change
					}
				};

		holder.decQuantityButton.setOnClickListener(quantityListener);
		holder.incQuantityButton.setOnClickListener(quantityListener);
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).getRowID();
	}

	public void setItemsList(ArrayList<Item> newItems) {
		int oldSize = items.size();
		items.clear();
		if (oldSize > 0) {
			notifyItemRangeRemoved(0, oldSize);
		}
		items.addAll(newItems);
		if (items.size() > 0) {
			notifyItemRangeInserted(0, items.size());
		}
	}

	public void addItem(Item item, int position) {
		if (position < 0 || position > items.size()) {
			return;
		}

		items.add(position, item);
		notifyItemInserted(position);
	}

	public void removeItem(int position) {
		if (position < 0 || position >= items.size()) {
			return;
		}

		items.remove(position);
		notifyItemRemoved(position);
	}

	public void showSnackbar(final Item item,
							 View view,
							 final int position) {
		String inventory = item.getInventory();
		if (inventory.isEmpty()) {
			inventory = "Inventory";
		}
		final String msg = "Item added to " +
				item.getInventory() + ".";
		View.OnClickListener listener =
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ItemData itemData =
								ItemData.getInstance();
						itemData.removeItem(item);
						item.setInGroceryList(true);
						itemData.addItem(item);
						addItem(item, position);
					}
				};

		Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
				.setAction("Undo", listener)
				.show();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener {
		public CheckBox checkBox;
		public TextView name;
		public TextView quantity;
		public TextView quantityUnit;
		public ImageButton decQuantityButton;
		public ImageButton incQuantityButton;

		private ViewHolderClickListener listener;

		public ViewHolder(View itemView, CheckBox checkBox,
						  TextView name, TextView quantity,
						  TextView quantityUnit,
						  ImageButton decQuantityButton,
						  ImageButton incQuantityButton,
						  ViewHolderClickListener listener) {
			super(itemView);
			this.checkBox = checkBox;
			this.name = name;
			this.quantity = quantity;
			this.quantityUnit = quantityUnit;
			this.decQuantityButton = decQuantityButton;
			this.incQuantityButton = incQuantityButton;
			this.listener = listener;

			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			listener.onClick(getAdapterPosition());
		}

		public static interface ViewHolderClickListener {
			public void onClick(int position);
		}
	}
}
