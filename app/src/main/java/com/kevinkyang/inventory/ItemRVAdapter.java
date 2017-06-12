package com.kevinkyang.inventory;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionValues;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class ItemRVAdapter
		extends RecyclerView.Adapter<ItemRVAdapter.ViewHolder>
		implements ListItemTouchHelperCallback.ListItemTouchHelperListener {
	private static final int PAYLOAD_EXPAND = 0x1;
	private static final int PAYLOAD_COLLAPSE = 0x2;
	private static final int PAYLOAD_EXP_EDIT = 0x3;
	private static final int PAYLOAD_QUA_EDIT = 0x4;

	private ArrayList<Item> items;
	private InventoryFragment parent;
	private DBManager dbManager;

	private RecyclerView recyclerView;
	private int expandedItemPosition;

	private int contextMenuPosition;

	private TypedArray colorArray;

	public ItemRVAdapter(ArrayList<Item> items,
						 RecyclerView recyclerView,
						 InventoryFragment parent) {
		this.items = items;
		this.parent = parent;
		dbManager = DBManager.getInstance();

		expandedItemPosition = RecyclerView.NO_POSITION;
		this.recyclerView = recyclerView;

		colorArray = parent.getResources()
				.obtainTypedArray(R.array.array_inventory_colors);

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

		updateExpirationViews(holder, item);

		updateQuantityViews(holder, item);

		holder.mEditExpirationButton.setOnClickListener(view -> {

			String expiresDate = item.getExpiresDate();
			String dateToSet = (expiresDate.isEmpty()) ? null : expiresDate;
			ExpirationPickerPopup popup = new ExpirationPickerPopup(parent.getContext(), dateToSet);

			popup.setClearButtonClickListener(() -> {
				holder.mExpiresDate.setText("");
				item.setExpiresDate("");
				DBManager dbManager = DBManager.getInstance();
				dbManager.updateItemColumn(item, DBSchema.TABLE_ITEMS.COL_EXPIRES);

				ItemRVAdapter.this.notifyItemChanged(position,
						PAYLOAD_EXP_EDIT);
			});

			popup.setSaveButtonClickListener((year, month, day) -> {
				final SimpleDateFormat sdFormat =
						new SimpleDateFormat(
								TimeManager.DEFAULT_DATE_FORMAT);

				Calendar cal = Calendar.getInstance();
				cal.set(year, month, day);
				String date = sdFormat.format(cal.getTime());
				if (!date.equals(item.getExpiresDate())) {
					item.setExpiresDate(date);
					holder.mExpiresDate.setText(date);
					DBManager dbManager = DBManager.getInstance();
					dbManager.updateItemColumn(item, DBSchema.TABLE_ITEMS.COL_EXPIRES);
				}

				ItemRVAdapter.this.notifyItemChanged(position,
						PAYLOAD_EXP_EDIT);
			});

			popup.showAtLocation(holder.mEditExpirationButton,
					Gravity.START,
					(int) holder.mEditExpirationButton.getX(),
					(int) holder.mEditExpirationButton.getY());
		});

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

				ItemRVAdapter.this.notifyItemChanged(position,
						PAYLOAD_QUA_EDIT);
			}
		};

		holder.mDecQuantityButton.setOnClickListener(quantityListener);
		holder.mIncQuantityButton.setOnClickListener(quantityListener);

		String createdString = "Created on: " + item.getCreatedDate();
		holder.mCreatedDate.setText(createdString);

		String invString = item.getInventory();
		if (!invString.isEmpty()) {
			ArrayList<String> inventories =
					DBManager.getInstance().getInventories();

			holder.mDetailColorTag.setBackgroundColor(
					colorArray.getColor(inventories.indexOf(invString),
							parent.getResources().getColor(
									R.color.colorGrey, null)));
			holder.mInventoryLabel.setText(invString);
		} else {
			holder.mDetailColorTag.setBackgroundColor(
					parent.getResources().getColor(
							R.color.colorGrey, null));
			holder.mInventoryLabel.setText("None");
		}

		String typeString = item.getType();
		if (!typeString.isEmpty()) {

			holder.mTypeLabel.setText(typeString);

			holder.mTypeLabel.setVisibility(View.VISIBLE);
		} else {
			holder.mTypeLabel.setVisibility(View.GONE);
		}

		holder.mEditButton.setOnClickListener((view) -> {
			ItemRVAdapter.this
					.parent.getParent()
					.showEditDialog(item, position);
		});

		final boolean isExpanded = position == expandedItemPosition;
		setItemExpansion(holder, isExpanded);
	}

	private void setItemExpansion(ViewHolder holder, boolean isExpanded) {
		holder.mQuantity.setVisibility(isExpanded ?
				View.GONE : View.VISIBLE);

		holder.mDetailLayout.setVisibility(isExpanded ?
				View.VISIBLE : View.GONE);
		holder.mItemView.setActivated(isExpanded);
	}

	private void updateExpirationViews(ViewHolder holder, Item item) {
		String expiresDate = item.getExpiresDate();
		if (!expiresDate.isEmpty()) {

			int dateDifference = TimeManager.getDateDifferenceInDays(
					TimeManager.getDateTimeLocal(),
					expiresDate);
			if (dateDifference > 3) {
				holder.mExpiresWarning.setVisibility(View.INVISIBLE);

			} else {
				String convertedTime = TimeManager.convertDays(dateDifference);
				String warningString;
				if (dateDifference < 0) {
					warningString = "EXPIRED " + convertedTime + " AGO";
				} else if (dateDifference > 0) {
					warningString = "EXPIRES IN " + convertedTime;
				} else {
					warningString = "EXPIRES TODAY";
				}
				holder.mExpiresWarning.setText(warningString);
				holder.mExpiresWarning.setVisibility(View.VISIBLE);
			}

			holder.mExpiresDate.setText(expiresDate);

		} else {
			holder.mExpiresWarning.setVisibility(View.INVISIBLE);

			holder.mExpiresDate.setText("Not Set");
		}
	}

	private void updateQuantityViews(ViewHolder holder, Item item) {
		String quantityString = Integer.toString((item.getQuantity()));
		if (item.getQuantity() > 0) {
			quantityString += " " + item.getUnit().trim();
		}
		holder.mQuantity.setText(quantityString);
		holder.mDetailQuantity.setText(quantityString);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position,
								 List<Object> payloads) {

		if (payloads.contains(PAYLOAD_EXPAND)) {
			setItemExpansion(holder, true);
		} else if (payloads.contains(PAYLOAD_COLLAPSE)) {
			setItemExpansion(holder, false);
		} else if (payloads.contains(PAYLOAD_EXP_EDIT)) {
			updateExpirationViews(holder, items.get(position));
		} else if (payloads.contains(PAYLOAD_QUA_EDIT)) {
			updateQuantityViews(holder, items.get(position));
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
