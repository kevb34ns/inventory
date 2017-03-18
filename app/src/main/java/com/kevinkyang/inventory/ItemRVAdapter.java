package com.kevinkyang.inventory;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Target;
import java.util.ArrayList;

/**
 * Created by Kevin on 3/13/2017.
 */

public class ItemRVAdapter
		extends RecyclerView.Adapter<ItemRVAdapter.ViewHolder> {
	private ArrayList<Item> items;
	private InventoryFragment parent;
	private DBManager dbManager;
	private int defaultColor;

	private int contextMenuPosition;

	public ItemRVAdapter(ArrayList<Item> items,
						 InventoryFragment parent) {
		this.items = items;
		this.parent = parent;
		dbManager = DBManager.getInstance();
		defaultColor = -1;

		setHasStableIds(true);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item, parent, false);

		TextView name = (TextView) view.findViewById(R.id.item_name);
		LinearLayout expiresContainer = (LinearLayout) view.findViewById(R.id.expires_container);
		TextView expiresNum = (TextView) view.findViewById(R.id.expires_num);
		TextView expiresUnit = (TextView) view.findViewById(R.id.expires_unit);
		TextView quantity = (TextView) view.findViewById(R.id.quantity);
		TextView quantityUnit = (TextView) view.findViewById(R.id.quantity_unit);
		ImageButton decQuantityButton = (ImageButton) view.findViewById(R.id.decrease_quantity);
		decQuantityButton.setFocusable(false);
		ImageButton incQuantityButton = (ImageButton) view.findViewById(R.id.increase_quantity);
		incQuantityButton.setFocusable(false);

		ViewHolder.ViewHolderClickListener listener =
				new ViewHolder.ViewHolderClickListener() {
					@Override
					public void onClick(int position) {
						Item item = items.get(position);
						ItemRVAdapter.this
								.parent.getParent()
								.showEditDialog(item);
					}

					@Override
					public void onLongClick(int position, View itemView) {
						setContextMenuPosition(position);
					}
				};

		ViewHolder holder =
				new ViewHolder(view, name, expiresContainer, expiresNum,
						expiresUnit, quantity, quantityUnit,
						decQuantityButton, incQuantityButton, listener,
						ItemRVAdapter.this.parent.getParent());
		return holder;
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		Item item = items.get(position);

		holder.name.setText(item.getName());

		String expiresDate = item.getExpiresDate();
		if (!expiresDate.isEmpty()) {
			int dateDifference = TimeManager.getDateDifferenceInDays(
					TimeManager.getDateTimeLocal(),
					expiresDate);
			String convertedTime = TimeManager.convertDays(dateDifference);
			String[] splitString = convertedTime.split(" ");
			holder.expiresNum.setText(splitString[0]);
			holder.expiresUnit.setText(splitString[1]);
			if (dateDifference < 0) {
				int color = holder.itemView
						.getContext()
						.getResources()
						.getColor(
								android.R.color.holo_red_dark, null);
				setExpirationColor(holder, color);
			} else {
				if (defaultColor == -1) {
					Context context = holder.itemView.getContext();
					defaultColor = context.getResources()
							.getColor(R.color.defaultTextColor,
									context.getTheme());
				}
				setExpirationColor(holder, defaultColor);
			}
			setExpirationVisibility(holder, View.VISIBLE);
		} else {
			setExpirationVisibility(holder, View.INVISIBLE);
		}

		holder.quantity.setText(Integer.toString(item.getQuantity()));

		String unitString = item.getUnit().trim();
		if (unitString.isEmpty()) {
			holder.quantityUnit.setVisibility(View.GONE);
		} else {
			holder.quantityUnit.setText(item.getUnit());
			holder.quantityUnit.setVisibility(View.VISIBLE);
		}

		View.OnClickListener quantityListener = new View.OnClickListener() {
			public void onClick(View view) {
				int amount = 0;
				switch (view.getId()) {
					case R.id.decrease_quantity: amount = -1; break;
					case R.id.increase_quantity: amount = 1; break;
					default: break;
				}

				Item item = items.get(holder.getAdapterPosition());
				if (item != null) {
					item.setQuantity(item.getQuantity() + amount);
					dbManager.updateItemColumn(item, DBSchema.TABLE_ITEMS.COL_QUANTITY);
					ItemRVAdapter.this.parent.refresh();
				}
			}
		};

		holder.decQuantityButton.setOnClickListener(quantityListener);
		holder.incQuantityButton.setOnClickListener(quantityListener);
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public void setItemsList(final ArrayList<Item> newItems) {
		int oldSize = items.size();
		items.clear();
		if(oldSize > 0) {
			notifyItemRangeRemoved(0, oldSize);
		}
		items.addAll(newItems);
		if (items.size() > 0) {
			notifyItemRangeInserted(0, items.size());
		}
	}

	public void addItem(Item item, int position) {
		if (position < 0 || position > getItemCount()) {
			return;
		}

		items.add(position, item);
		notifyItemInserted(position);
	}

	public void removeItem(int position) {
		if (position < 0 || position >= getItemCount()) {
			return;
		}

		items.remove(position);
		notifyItemRemoved(position);
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).getRowID();
	}

	public Item getItem(int position) {
		return items.get(position);
	}

	public int getContextMenuPosition() {
		return contextMenuPosition;
	}

	public void setContextMenuPosition(int contextMenuPosition) {
		this.contextMenuPosition = contextMenuPosition;
	}

	private void setExpirationVisibility(ViewHolder holder, int visibility) {
		holder.expiresContainer.setVisibility(visibility);
		holder.expiresNum.setVisibility(visibility);
		holder.expiresUnit.setVisibility(visibility);
	}

	private void setExpirationColor(ViewHolder holder, int color) {
		holder.name.setTextColor(color);
		holder.expiresNum.setTextColor(color);
		holder.expiresUnit.setTextColor(color);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener,
			View.OnLongClickListener,
			View.OnCreateContextMenuListener {
		public View itemView;

		public TextView name;
		public LinearLayout expiresContainer;
		public TextView expiresNum;
		public TextView expiresUnit;
		public TextView quantity;
		public TextView quantityUnit;
		public ImageButton decQuantityButton;
		public ImageButton incQuantityButton;

		public ViewHolderClickListener listener;
		private MainActivity parent;

		public ViewHolder(View itemView, TextView name,
						  LinearLayout expiresContainer,
						  TextView expiresNum, TextView expiresUnit,
						  TextView quantity, TextView quantityUnit,
						  ImageButton decQuantityButton,
						  ImageButton incQuantityButton,
						  ViewHolderClickListener listener,
						  MainActivity parent) {
			super(itemView);
			this.itemView = itemView;
			this.name = name;
			this.expiresContainer = expiresContainer;
			this.expiresNum = expiresNum;
			this.expiresUnit = expiresUnit;
			this.quantity = quantity;
			this.quantityUnit = quantityUnit;
			this.decQuantityButton = decQuantityButton;
			this.incQuantityButton = incQuantityButton;
			this.listener = listener;
			this.parent = parent;

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
			itemView.setOnCreateContextMenuListener(this);
		}

		@Override
		public void onClick(View v) {
			listener.onClick(getAdapterPosition());
		}

		@Override
		public boolean onLongClick(View v) {
			Log.d(MainActivity.TAG, "ViewHolder onLongClick entered"); //TODO
			listener.onLongClick(getAdapterPosition(),
					itemView);
			return false;
		}

		/**
		 * Workaround to implement context menu in
		 * RecyclerView.
		 * @param menu
		 * @param v
		 * @param menuInfo
		 */
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
			if (parent == null) {
				return;
			}
			MenuInflater inflater = parent.getMenuInflater();
			inflater.inflate(
					R.menu.list_item_context_menu, menu);
		}

		public static interface ViewHolderClickListener {
			public void onClick(int position);

			public void onLongClick(int position, View itemView);
		}
	}
}
