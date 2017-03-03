package com.kevinkyang.inventory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by Kevin on 2/17/2017.
 */

public class InventoryFragment extends Fragment implements CustomFragment {
	private MainActivity parent;

	private ItemData itemData = null;
	private ListView inventoryListView;
	private ItemAdapter itemAdapter;

	private String inventory;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_inventory, container, false);
		inventoryListView = (ListView) view.findViewById(R.id.inventory_listview);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// this fragment can only be attached to MainActivity
		parent = (MainActivity) getActivity();

		itemData = ItemData.getInstance();
		itemAdapter = new ItemAdapter(parent,
				itemData.getInventoryItems(), this);
		inventoryListView.setAdapter(itemAdapter);
		inventory = null;
		registerForContextMenu(inventoryListView);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if (parent == null) {
			return;
		}

		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = parent.getMenuInflater();
		inflater.inflate(R.menu.list_item_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Item it = itemAdapter.getItem(menuInfo.position);
		switch (item.getItemId()) {
			case R.id.list_item_delete:
				if (itemData.removeItem(it)) {
					refresh();
					return true;
				}
				else return false;
			case R.id.list_item_add_to_grocery:
				swapToGroceryList(it);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	/**
	 * Call this when changes occur in other parts of the
	 * app that affect the inventory list.
	 */
	@Override
	public void refresh() {
		itemAdapter.notifyDataSetInvalidated();
		itemAdapter = new ItemAdapter(parent,
				itemData.getItemsByInventory(inventory), this);
		inventoryListView.setAdapter(itemAdapter);
	}

	public void setInventory(String inventory) {
		this.inventory = inventory;
	}

	private void swapToGroceryList(Item item) {
		//TODO might be able to do this by calling DbManager.updateItem
		itemData.removeItem(item);
		item.setInGroceryList(true);
		itemData.addItem(item);
		refresh();
	}
}
