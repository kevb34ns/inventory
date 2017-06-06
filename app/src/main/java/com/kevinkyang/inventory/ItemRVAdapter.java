package com.kevinkyang.inventory;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ItemRVAdapter
		extends RecyclerView.Adapter<ItemRVAdapter.ViewHolder>
		implements ListItemTouchHelperCallback.ListItemTouchHelperListener {
	private static final int PAYLOAD_EXPAND = 0x1;
	private static final int PAYLOAD_COLLAPSE = 0x2;

	private ArrayList<Item> items;
	private InventoryFragment parent;
	private DBManager dbManager;
	private int defaultColor;

	//TODO experimental
	private RecyclerView recyclerView;
	private int expandedItemPosition;

	private int contextMenuPosition;

	public ItemRVAdapter(ArrayList<Item> items,
						 RecyclerView recyclerView,
						 InventoryFragment parent) {
		this.items = items;
		this.parent = parent;
		dbManager = DBManager.getInstance();
		defaultColor = -1;

		expandedItemPosition = RecyclerView.NO_POSITION;
		this.recyclerView = recyclerView;

		setHasStableIds(true);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item, parent, false);
		
		ViewHolder holder = new ViewHolder(view,
						ItemRVAdapter.this.parent.getParent());

		holder.setViewHolderClickListener(new ViewHolder
				.ViewHolderClickListener() {

			@Override
			public void onClick(int position) {
				// TODO open edit dialog some other way
//						Item item = items.get(position);
//						ItemRVAdapter.this
//								.parent.getParent()
//								.showEditDialog(item, position);

				if (expandedItemPosition != RecyclerView.NO_POSITION) {
					notifyItemChanged(expandedItemPosition, PAYLOAD_COLLAPSE);
				}

				if (expandedItemPosition != position) {
					expandedItemPosition = position;
					notifyItemChanged(position, PAYLOAD_EXPAND);
				} else {
					expandedItemPosition = RecyclerView.NO_POSITION;
				}
				//TODO see dribbbleshot for a better expandcollapse transition
				TransitionManager.beginDelayedTransition(recyclerView);
			}

			@Override
			public void onLongClick(int position, View itemView) {
				setContextMenuPosition(position);
			}
		});


		return holder;
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		Item item = items.get(position);

		holder.mName.setText(item.getName());

		// If the item expires in 3 days or less (or is expired),
		// show expiration warning message
		String expiresDate = item.getExpiresDate();
		if (!expiresDate.isEmpty()) {

			int dateDifference = TimeManager.getDateDifferenceInDays(
					TimeManager.getDateTimeLocal(),
					expiresDate);
			if (dateDifference > 3) {
				holder.mExpiresWarning.setVisibility(View.INVISIBLE);

			} else {
				String convertedTime = TimeManager.convertDays(dateDifference);
				holder.mExpiresWarning.setText(convertedTime);
				holder.mExpiresWarning.setVisibility(View.VISIBLE);
			}

		} else {
			holder.mExpiresWarning.setVisibility(View.INVISIBLE);
		}

		String quantityString = Integer.toString((item.getQuantity()))
				+ " " + item.getUnit().trim();
		holder.mQuantity.setText(quantityString);

		// Set detail layout info
		// TODO set expires/quantity visibility based on whether or not it exists for the item
		if (!expiresDate.isEmpty()) {
			holder.mExpiresDate.setText(expiresDate);
		}

		holder.mQuantity.setText(quantityString);
		View.OnClickListener quantityListener = (view) -> {
			int amount = 0;
			switch (view.getId()) {
				case R.id.decrease_quantity:
					amount = -1;
					break;
				case R.id.increase_quantity:
					amount = 1;
					break;
				default:
					break;
			}

			Item clickedItem = items.get(holder.getAdapterPosition());
			if (clickedItem != null) {
				clickedItem.setQuantity(clickedItem.getQuantity() + amount);
				dbManager.updateItemColumn(clickedItem, DBSchema.TABLE_ITEMS.COL_QUANTITY);
				ItemRVAdapter.this.parent.refresh(); // TODO change to notifyItemChanged
			}
		};

		holder.mDecQuantityButton.setOnClickListener(quantityListener);
		holder.mIncQuantityButton.setOnClickListener(quantityListener);

		String createdString = "Created on: " + item.getCreatedDate();
		holder.mCreatedDate.setText(createdString);

		// TODO color tag, inventory label, type label

		final boolean isExpanded = position == expandedItemPosition;
		setItemExpansion(holder, isExpanded);
	}

	private void setItemExpansion(ViewHolder holder, boolean isExpanded) {
		// TODO change item name size
		holder.mQuantity.setVisibility(isExpanded ?
				View.GONE : View.VISIBLE);

		holder.mDetailLayout.setVisibility(isExpanded ?
				View.VISIBLE : View.GONE);
		holder.mItemView.setActivated(isExpanded);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position,
								 List<Object> payloads) {

		if (payloads.contains(PAYLOAD_EXPAND)) {
			setItemExpansion(holder, true);
		} else if (payloads.contains(PAYLOAD_COLLAPSE)) {
			setItemExpansion(holder, false);
		} else {
			onBindViewHolder(holder, position);
		}
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public void setItemsList(final ArrayList<Item> newItems) {
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

	@Override
	public void onDelete(int position) {
		Item item = items.get(position);
		parent.removeItem(item, position);
		String inventory = (item.getInventory().isEmpty()) ?
				"inventory" : item.getInventory();
		String msg = "Removed " + item.getName() + " from " +
				inventory + ".";
		parent.getParent()
				.showSnackbar(item, parent.getView(), position,
						msg, parent::undoDelete);
	}

	@Override
	public void onSwapList(int position) {
		Item item = items.get(position);
		parent.swapList(item, position);
		String inventory = (item.getInventory().isEmpty()) ?
				"inventory" : item.getInventory();
		String msg = "Moved " + item.getName() + " to the Grocery List.";
		parent.getParent()
				.showSnackbar(item, parent.getView(), position,
						msg, parent::undoSwapList);
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

	public static class ViewHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener,
			View.OnLongClickListener,
			View.OnCreateContextMenuListener {
		public View mItemView;

		// default view items
		public View mColorTag;
		public TextView mName;
		public TextView mExpiresWarning;
		public TextView mQuantity;

		// detail view items
		public LinearLayout mDetailLayout;
		// expiration views
		public LinearLayout mExpiresContainer;
		public TextView mExpiresDate;
		public Button mEditExpirationButton;
		// quantity views
		public LinearLayout mQuantityContainer;
		public TextView mDetailQuantity;
		public ImageButton mDecQuantityButton;
		public ImageButton mIncQuantityButton;
		// date created
		public TextView mCreatedDate;
		// bottom inventory/type info and edit button
		public View mDetailColorTag;
		public TextView mInventoryLabel;
		public TextView mTypeLabel;
		public Button mEditButton;

		public ViewHolderClickListener viewHolderClickListener;
		private MainActivity parent;

		public ViewHolder(View itemView,
						  MainActivity parent) {
			super(itemView);
			mItemView = itemView;
			this.parent = parent;

			getViews();
			setListeners();
		}

		private void getViews() {
			mColorTag = itemView.findViewById(R.id.item_color_tag);
			mName = (TextView) itemView.findViewById(R.id.item_name);
			mExpiresWarning = (TextView)
					itemView.findViewById(R.id.expires_warning);
			mQuantity = (TextView) itemView.findViewById(R.id.quantity);

			mDetailLayout = (LinearLayout)
					itemView.findViewById(R.id.detail_layout);

			mExpiresContainer = (LinearLayout)
					itemView.findViewById(R.id.detail_expiration_container);
			mExpiresDate = (TextView)
					itemView.findViewById(R.id.detail_expiration);
			mEditExpirationButton = (Button)
					itemView.findViewById(R.id.detail_edit_expiration);

			mQuantityContainer = (LinearLayout)
					itemView.findViewById(R.id.detail_quantity_container);
			mDetailQuantity = (TextView)
					itemView.findViewById(R.id.detail_quantity);
			mDecQuantityButton = (ImageButton)
					itemView.findViewById(R.id.decrease_quantity);
			mIncQuantityButton = (ImageButton)
					itemView.findViewById(R.id.increase_quantity);

			mCreatedDate = (TextView)
					itemView.findViewById(R.id.detail_created_date);

			mDetailColorTag = itemView.findViewById(R.id.detail_color_tag);
			mInventoryLabel = (TextView)
					itemView.findViewById(R.id.detail_inventory_label);
			mTypeLabel = (TextView)
					itemView.findViewById(R.id.detail_type_label);
			mEditButton = (Button)
					itemView.findViewById(R.id.detail_edit_button);
		}

		private void setListeners() {
			viewHolderClickListener = null;

			mItemView.setOnClickListener(this);
			mItemView.setOnLongClickListener(this);
			mItemView.setOnCreateContextMenuListener(this);

			mEditExpirationButton.setOnClickListener((view) -> {
				// TODO pop expiration edit widget (same one from the add/edit dialog)
			});

			mEditButton.setOnClickListener((view) -> {
				// TODO pop edit dialog
			});
		}

		public ViewHolderClickListener getViewHolderClickListener() {
			return viewHolderClickListener;
		}

		public void setViewHolderClickListener(ViewHolderClickListener viewHolderClickListener) {
			this.viewHolderClickListener = viewHolderClickListener;
		}

		@Override
		public void onClick(View v) {
			if (viewHolderClickListener != null) {
				viewHolderClickListener.onClick(getAdapterPosition());
			}
		}

		@Override
		public boolean onLongClick(View v) {
			if (viewHolderClickListener != null) {
				viewHolderClickListener.onLongClick(getAdapterPosition(),
						itemView);
			}

			return false;
		}

		/**
		 * Workaround to implement context menu in
		 * RecyclerView.
		 *
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
