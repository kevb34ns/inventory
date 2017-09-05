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
	private MainActivity parent;

	private ItemManager itemManager = null;

	private RecyclerView itemRecyclerView;
	private ItemRVAdapter itemRVAdapter;
	private RecyclerView.LayoutManager layoutManager;
	private ItemTouchHelper itemTouchHelper;

	private String inventory = null;
	private boolean initFinished = false;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_inventory, container, false);
		itemRecyclerView = (RecyclerView) view.findViewById(R.id.inventory_rv);
		registerForContextMenu(itemRecyclerView);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO this fragment can only be attached to com.kevinkyang.inventory.MainActivity, is this safe?
		parent = (MainActivity) getActivity();

		itemManager = ItemManager.getInstance();

		itemRVAdapter = new ItemRVAdapter(new ArrayList<Item>(), itemRecyclerView, this);
		itemRecyclerView.setAdapter(itemRVAdapter);
		layoutManager = new LinearLayoutManager(parent);
		itemRecyclerView.setLayoutManager(layoutManager);
		DividerItemDecoration divider = new DividerItemDecoration(parent, DividerItemDecoration.VERTICAL);
		itemRecyclerView.addItemDecoration(divider);
		itemRecyclerView.setHasFixedSize(false);
		itemTouchHelper = new ItemTouchHelper(
						new ListItemTouchHelperCallback(
								getContext(), itemRVAdapter, false));
		itemTouchHelper.attachToRecyclerView(itemRecyclerView);

		initFinished = true;

		if (savedInstanceState != null) {
			inventory =
					savedInstanceState.getString("currentInventory");
			refresh();
			parent.changeToCurrentList();
		}

		showInventory(inventory);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("currentInventory", inventory);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int position = itemRVAdapter.getContextMenuPosition();
		Item it = itemRVAdapter.getItem(position);
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
	 * to the items belonging to a certain inventory.
	 * @param inventory the name of the inventory to
	 *                  display, or null to display
	 *                  all items.
	 */
	public void showInventory(String inventory) {
		this.inventory = inventory;
		refresh();
		parent.changeActionBarTitle(inventory);
	}

	public void showSearchResults(String query, ArrayList<Item> items) {
		itemRVAdapter.setItemsList(items);
		parent.changeActionBarTitle("Results for: " + query);
	}

	public String getCurrentInventory() {
		//TODO this can be null even though in the db an empty string represents no inv, which causes confusion
		return inventory;
	}

	@Override
	public MainActivity getParent() {
		return parent;
	}

	/**
	 * Call this when changes occur in other parts of the
	 * app that affect the inventory list.
	 */
	@Override
	public void refresh() {
		if (inventory != null && inventory.equals("Expiring")) {
			itemRVAdapter.setItemsList(
					itemManager.getItemsByInventory(inventory,
							ExpirationManager.getExpirationInterval(
									getContext())));
		} else {
			itemRVAdapter.setItemsList(
					itemManager.getItemsByInventory(inventory));
		}
	}

	@Override
	public void itemAdded(Item item) {
		itemRVAdapter.addItem(item, itemRVAdapter.getItemCount());
		layoutManager.scrollToPosition(itemRVAdapter.getItemCount() - 1);
	}

	@Override
	public void itemSaved(int position) {
		itemRVAdapter.changeItem(position);
	}

	public void itemExpanded(int position) {
		if (position == itemRVAdapter.getItemCount() - 1) {
			layoutManager.scrollToPosition(itemRVAdapter.getItemCount() - 1);
		}
	}

	public void setInventory(String inventory) {
		this.inventory = inventory;
	}

	public boolean isInitFinished() {
		return initFinished;
	}

	public void removeItem(Item item, int position) {
		if (itemManager.removeItem(item)) {
			itemRVAdapter.removeItem(position);
		}
	}

	/**
	 *
	 * @param item
	 * @param position the position of the item in itemRVAdapter's
	 *                 internal data structure
	 */
	@Override
	public void swapList(Item item, int position) {
		item.setInGroceryList(true);
		itemManager.updateItem(item);
		itemRVAdapter.removeItem(position);
	}

	@Override
	public void undoDelete(Item item, Integer position) {
		itemManager.addItem(item);
		itemRVAdapter.addItem(item, position);
	}

	@Override
	public void undoSwapList(Item item, Integer position) {
		item.setInGroceryList(false);
		itemManager.updateItem(item);
		itemRVAdapter.addItem(item, position);
	}
}
