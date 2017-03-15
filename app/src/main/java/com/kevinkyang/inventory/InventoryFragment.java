package com.kevinkyang.inventory;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
//	private ListView inventoryListView; TODO
//	private ItemAdapter itemAdapter; TODO

	private RecyclerView itemRecyclerView;
	private ItemRVAdapter itemRVAdapter;
	private RecyclerView.LayoutManager layoutManager;

	private String inventory = null;
	private boolean initFinished = false;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_inventory, container, false);
//		inventoryListView = (ListView) view.findViewById(R.id.inventory_listview); TODO
		itemRecyclerView = (RecyclerView) view.findViewById(R.id.inventory_rv);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO this fragment can only be attached to com.kevinkyang.inventory.MainActivity, is this safe?
		parent = (MainActivity) getActivity();

		itemData = ItemData.getInstance();
//		itemAdapter = new ItemAdapter(parent,
//				new ArrayList<Item>(), this); TODO
//		inventoryListView.setAdapter(itemAdapter); TODO
//		registerForContextMenu(inventoryListView); TODO

		itemRVAdapter = new ItemRVAdapter(new ArrayList<Item>(), this);
		itemRecyclerView.setAdapter(itemRVAdapter);
		layoutManager = new LinearLayoutManager(parent);
		itemRecyclerView.setLayoutManager(layoutManager);
		itemRecyclerView.setHasFixedSize(false); // TODO enable this when you restrict item height later, right now long titles elongate an item
		registerForContextMenu(itemRecyclerView);

		initFinished = true;

		if (savedInstanceState != null) {
			inventory =
					savedInstanceState
							.getString("currentInventory");
			refresh();
			parent.changeToCurrentList();
		}

		showInventory(inventory);

//		addListeners(); TODO
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("currentInventory", inventory);
		super.onSaveInstanceState(outState);
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
		//TODO need to implement context menu for recyclerview
//		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//		Item it = itemAdapter.getItem(menuInfo.position);
//		switch (item.getItemId()) {
//			case R.id.list_item_delete:
//				if (itemData.removeItem(it)) {
//					refresh();
//					return true;
//				}
//				else return false;
//			case R.id.list_item_add_to_grocery:
//				swapToGroceryList(it);
//				return true;
//			default:
//				return super.onContextItemSelected(item);
//		}
		return super.onContextItemSelected(item); //TODO
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
//		itemAdapter.notifyDataSetInvalidated(); TODO
//		itemAdapter = new ItemAdapter(parent,
//				itemData.getItemsByInventory(inventory), this);
//		inventoryListView.setAdapter(itemAdapter);
	}

	public void setInventory(String inventory) {
		this.inventory = inventory;
	}

	public boolean isInitFinished() {
		return initFinished;
	}

//	private void addListeners() { TODO
//		inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				Item item = (Item) inventoryListView.getItemAtPosition(position);
//				InventoryFragment.this.parent.showEditDialog(item);
//			}
//		});
//	}

	private void swapToGroceryList(Item item) {
		//TODO might be able to do this by calling DbManager.updateItemColumn
		itemData.removeItem(item);
		item.setInGroceryList(true);
		itemData.addItem(item);
		refresh();
	}
}
