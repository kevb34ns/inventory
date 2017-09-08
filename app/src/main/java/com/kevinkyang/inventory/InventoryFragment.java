package com.kevinkyang.inventory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;

public class InventoryFragment extends Fragment implements CustomFragment {
	private MainActivity mParent;

	private ItemManager mItemManager = null;

	private RecyclerView mItemRecyclerView;
	private ItemRVAdapter mItemRVAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private ItemTouchHelper mItemTouchHelper;

	private String mInventory = null;
	private boolean mInitFinished = false;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_inventory, container, false);
		mItemRecyclerView = (RecyclerView) view.findViewById(R.id.inventory_rv);
		registerForContextMenu(mItemRecyclerView);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO this fragment can only be attached to com.kevinkyang.mInventory.MainActivity, is this safe?
		mParent = (MainActivity) getActivity();

		mItemManager = ItemManager.getInstance();

		mItemRVAdapter = new ItemRVAdapter(new ArrayList<Item>(), mItemRecyclerView, this);
		mItemRecyclerView.setAdapter(mItemRVAdapter);
		mLayoutManager = new LinearLayoutManager(mParent);
		mItemRecyclerView.setLayoutManager(mLayoutManager);
		DividerItemDecoration divider = new DividerItemDecoration(mParent, DividerItemDecoration.VERTICAL);
		mItemRecyclerView.addItemDecoration(divider);
		mItemRecyclerView.setHasFixedSize(false);
		mItemTouchHelper = new ItemTouchHelper(
						new ListItemTouchHelperCallback(
								getContext(), mItemRVAdapter, false));
		mItemTouchHelper.attachToRecyclerView(mItemRecyclerView);

		mInitFinished = true;

		if (savedInstanceState != null) {
			mInventory =
					savedInstanceState.getString("currentInventory");
			refresh();
			mParent.changeToCurrentList();
		}

		showInventory(mInventory);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("currentInventory", mInventory);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int position = mItemRVAdapter.getContextMenuPosition();
		Item it = mItemRVAdapter.getItem(position);
		switch (item.getItemId()) {
			case R.id.list_item_delete:
				removeItem(it, position);
				return true;
			case R.id.list_item_add_to_grocery:
				swapList(it, position);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	/**
	 * Changes the list displayed by this fragment
	 * to the items belonging to a certain mInventory.
	 * @param inventory the mName of the mInventory to
	 *                  display, or null to display
	 *                  all items.
	 */
	public void showInventory(String inventory) {
		this.mInventory = inventory;
		refresh();
		mParent.changeActionBarTitle(inventory);
	}

	public void showSearchResults(String query, ArrayList<Item> items) {
		mItemRVAdapter.setItemsList(items);
		mParent.changeActionBarTitle("Results for: " + query);
	}

	public String getCurrentInventory() {
		//TODO this can be null even though in the db an empty string represents no inv, which causes confusion
		return mInventory;
	}

	@Override
	public MainActivity getParent() {
		return mParent;
	}

	/**
	 * Call this when changes occur in other parts of the
	 * app that affect the mInventory list.
	 */
	@Override
	public void refresh() {
		if (mInventory != null && mInventory.equals("Expiring")) {
			mItemRVAdapter.setItemsList(
					mItemManager.getItemsByInventory(mInventory,
							ExpirationManager.getExpirationInterval(
									getContext())));
		} else {
			mItemRVAdapter.setItemsList(
					mItemManager.getItemsByInventory(mInventory));
		}
	}

	@Override
	public void itemAdded(Item item) {
		mItemRVAdapter.addItem(item, mItemRVAdapter.getItemCount());
		mLayoutManager.scrollToPosition(mItemRVAdapter.getItemCount() - 1);
	}

	@Override
	public void itemSaved(int position) {
		mItemRVAdapter.changeItem(position);
	}

	public void itemExpanded(int position) {
		if (position == mItemRVAdapter.getItemCount() - 1) {
			mLayoutManager.scrollToPosition(mItemRVAdapter.getItemCount() - 1);
		}
	}

	public void setInventory(String inventory) {
		this.mInventory = inventory;
	}

	public boolean isInitFinished() {
		return mInitFinished;
	}

	public void removeItem(Item item, int position) {
		if (mItemManager.removeItem(item)) {
			mItemRVAdapter.removeItem(position);
		}
	}

	/**
	 *
	 * @param item
	 * @param position the position of the item in mItemRVAdapter's
	 *                 internal data structure
	 */
	@Override
	public void swapList(Item item, int position) {
		item.setInGroceryList(true);
		mItemManager.updateItem(item);
		mItemRVAdapter.removeItem(position);
	}

	@Override
	public void undoDelete(Item item, Integer position) {
		mItemManager.addItem(item);
		mItemRVAdapter.addItem(item, position);
	}

	@Override
	public void undoSwapList(Item item, Integer position) {
		item.setInGroceryList(false);
		mItemManager.updateItem(item);
		mItemRVAdapter.addItem(item, position);
	}
}
