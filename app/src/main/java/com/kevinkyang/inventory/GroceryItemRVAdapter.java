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

public class GroceryItemRVAdapter
		extends RecyclerView.Adapter<GroceryItemRVAdapter.ViewHolder>
		implements ListItemTouchHelperCallback.ListItemTouchHelperListener {
	private ArrayList<Item> mItems;
	private GroceryFragment mParent;

	public GroceryItemRVAdapter(ArrayList<Item> items,
								GroceryFragment parent) {
		this.mItems = items;
		this.mParent = parent;

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
				Item item = mItems.get(position);
				GroceryItemRVAdapter.this
						.mParent.getParent()
						.showEditDialog(item, position);
			}
		};

		return new ViewHolder(view, checkBox, name, quantity, quantityUnit, decQuantityButton, incQuantityButton, listener);
	}

	@Override
	public void onBindViewHolder(final GroceryItemRVAdapter.ViewHolder holder, int position) {
		Item item = mItems.get(position);

		holder.mCheckBox.setChecked(false);
		holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					return;
				}

				onSwapList(holder.getAdapterPosition());
			}
		});

		holder.mName.setText(item.getName());
		holder.mQuantity.setText(Utilities.Math.formatFloat(item.getQuantity()));

		String unitString = item.getUnit().trim();
		if (unitString.isEmpty()) {
			holder.mQuantityUnit.setVisibility(View.GONE);
		} else {
			holder.mQuantityUnit.setText(item.getUnit());
			holder.mQuantityUnit.setVisibility(View.VISIBLE);
		}

		View.OnClickListener quantityListener =
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO mQuantity change
					}
				};

		holder.mDecQuantityButton.setOnClickListener(quantityListener);
		holder.mIncQuantityButton.setOnClickListener(quantityListener);
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	@Override
	public long getItemId(int position) {
		return mItems.get(position).getRowID();
	}

	public void setItemsList(ArrayList<Item> newItems) {
		int oldSize = mItems.size();
		mItems.clear();
		if (oldSize > 0) {
			notifyItemRangeRemoved(0, oldSize);
		}
		mItems.addAll(newItems);
		if (mItems.size() > 0) {
			notifyItemRangeInserted(0, mItems.size());
		}
	}

	public void addItem(Item item, int position) {
		if (position < 0 || position > mItems.size()) {
			return;
		}

		mItems.add(position, item);
		notifyItemInserted(position);
	}

	public void changeItem(int position) {
		if (position < 0 || position >= mItems.size()) {
			return;
		}

		notifyItemChanged(position);
	}

	public void removeItem(int position) {
		if (position < 0 || position >= mItems.size()) {
			return;
		}

		mItems.remove(position);
		notifyItemRemoved(position);
	}

	@Override
	public void onDelete(int position) {
		Item item = mItems.get(position);
		mParent.removeItem(item, position);
		String inventory = (item.getInventory().isEmpty()) ?
				"inventory" : item.getInventory();
		String msg = "Removed " + item.getName() + " from grocery list.";
		mParent.getParent()
				.showSnackbar(item, mParent.getView(), position,
				msg, mParent::undoDelete);
	}

	@Override
	public void onSwapList(int position) {
		Item item = mItems.get(position);
		mParent.swapList(item, position);
		String inventory = (item.getInventory().isEmpty()) ?
				"inventory" : item.getInventory();
		String msg = item.getName() + " added to " + inventory + ".";
		mParent.getParent()
				.showSnackbar(item, mParent.getView(), position,
						msg, mParent::undoSwapList);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener {
		public CheckBox mCheckBox;
		public TextView mName;
		public TextView mQuantity;
		public TextView mQuantityUnit;
		public ImageButton mDecQuantityButton;
		public ImageButton mIncQuantityButton;

		private ViewHolderClickListener mListener;

		public ViewHolder(View itemView, CheckBox checkBox,
						  TextView name, TextView quantity,
						  TextView quantityUnit,
						  ImageButton decQuantityButton,
						  ImageButton incQuantityButton,
						  ViewHolderClickListener listener) {
			super(itemView);
			mCheckBox = checkBox;
			mName = name;
			mQuantity = quantity;
			mQuantityUnit = quantityUnit;
			mDecQuantityButton = decQuantityButton;
			mIncQuantityButton = incQuantityButton;
			mListener = listener;

			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			mListener.onClick(getAdapterPosition());
		}

		public static interface ViewHolderClickListener {
			public void onClick(int position);
		}
	}
}
