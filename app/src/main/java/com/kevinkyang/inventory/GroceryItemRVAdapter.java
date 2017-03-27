package com.kevinkyang.inventory;

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
		extends RecyclerView.Adapter<GroceryItemRVAdapter.ViewHolder>
		implements ListItemTouchHelperCallback.ListItemTouchHelperListener {
	private ArrayList<Item> items;
	private GroceryFragment parent;
	private DBManager dbManager;

	public GroceryItemRVAdapter(ArrayList<Item> items,
								GroceryFragment parent) {
		this.items = items;
		this.parent = parent;
		dbManager = DBManager.getInstance();

		setHasStableIds(true);
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
				Item item = items.get(position);
				GroceryItemRVAdapter.this
						.parent.getParent()
						.showEditDialog(item, position);
			}
		};

		return new ViewHolder(view, checkBox, name, quantity, quantityUnit, decQuantityButton, incQuantityButton, listener);
	}

	@Override
	public void onBindViewHolder(final GroceryItemRVAdapter.ViewHolder holder, int position) {
		Item item = items.get(position);

		holder.checkBox.setChecked(false);
		holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					return;
				}

				onSwapList(holder.getAdapterPosition());
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

	public void changeItem(int position) {
		if (position < 0 || position >= items.size()) {
			return;
		}

		notifyItemChanged(position);
	}

	public void removeItem(int position) {
		if (position < 0 || position >= items.size()) {
			return;
		}

		items.remove(position);
		notifyItemRemoved(position);
	}

	@Override
	public void onDelete(int position) {
		Item item = items.get(position);
		parent.removeItem(item, position);
		String inventory = (item.getInventory().isEmpty()) ?
				"inventory" : item.getInventory();
		String msg = "Removed " + item.getName() + " from grocery list.";
//		parent.getParent()
//				.showSnackbar(item, parent.getView(), position,
//				msg, parent::undoDelete); TODO swap to this when native Java 8 support released
		parent.getParent()
				.showSnackbar(item, parent.getView(), position,
						msg, new MainActivity.BiConsumer<Item, Integer>() {
							@Override
							public void accept(Item item, Integer integer) {
								parent.undoDelete(item, integer);
							}
						});
	}

	@Override
	public void onSwapList(int position) {
		Item item = items.get(position);
		parent.swapList(item, position);
		String inventory = (item.getInventory().isEmpty()) ?
				"inventory" : item.getInventory();
		String msg = item.getName() + " added to " + inventory + ".";
//		parent.getParent()
//				.showSnackbar(item, parent.getView(), position,
//						msg, parent::undoSwapList); // TODO switch to this when Android Studio adds native Java 8 support
		parent.getParent()
				.showSnackbar(item, parent.getView(), position,
						msg, new MainActivity.BiConsumer<Item, Integer>() {
							@Override
							public void accept(Item item, Integer integer) {
								parent.undoSwapList(item, integer);
							}
						});
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
