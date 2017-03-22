package com.kevinkyang.inventory;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Kevin on 2/17/2017.
 */

public class InventoryFragment extends Fragment implements CustomFragment {
	private MainActivity parent;

	private ItemData itemData = null;

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

		itemData = ItemData.getInstance();

		itemRVAdapter = new ItemRVAdapter(new ArrayList<Item>(), this);
		itemRecyclerView.setAdapter(itemRVAdapter);
		layoutManager = new LinearLayoutManager(parent);
		itemRecyclerView.setLayoutManager(layoutManager);
		DividerItemDecoration divider = new DividerItemDecoration(parent, DividerItemDecoration.VERTICAL);
		itemRecyclerView.addItemDecoration(divider);
		itemRecyclerView.setHasFixedSize(false);
		itemTouchHelper = new ItemTouchHelper(
						new ListItemTouchHelperCallback(
								getContext(),
								itemRVAdapter));
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
				swapToGroceryList(it, position);
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

	public String getCurrentInventory() {
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
		itemRVAdapter.setItemsList(
				itemData.getItemsByInventory(inventory));
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

	public void setInventory(String inventory) {
		this.inventory = inventory;
	}

	public boolean isInitFinished() {
		return initFinished;
	}

	public void removeItem(Item item, int position) {
		if (itemData.removeItem(item)) {
			itemRVAdapter.removeItem(position);
		}
	}

	/**
	 *
	 * @param item
	 * @param position the position of the item in itemRVAdapter's
	 *                 internal data structure
	 */
	public void swapToGroceryList(Item item, int position) {
		//TODO might be able to do this by calling DbManager.updateItemColumn
		itemData.removeItem(item);
		item.setInGroceryList(true);
		itemData.addItem(item);
		itemRVAdapter.removeItem(position);
	}
}
