package com.kevinkyang.inventory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by Kevin on 2/17/2017.
 */

public class GroceryFragment extends Fragment implements CustomFragment {
	private MainActivity parent;

	private ItemData itemData = null;
	private ListView inventoryListView;
	private GroceryItemAdapter itemAdapter;

	private RecyclerView itemRecyclerView;
	private GroceryItemRVAdapter itemRVAdapter;
	private LinearLayoutManager layoutManager;

	private boolean initFinished = false;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_inventory, container, false);
//		inventoryListView = (ListView) view.findViewById(R.id.inventory_listview); TODO
		itemRecyclerView = (RecyclerView) view.findViewById(R.id.inventory_rv);
		registerForContextMenu(itemRecyclerView);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// this fragment can only be attached to MainActivity
		parent = (MainActivity) getActivity();

		itemData = ItemData.getInstance();
//		itemAdapter = new GroceryItemAdapter(parent, itemData.getGroceryListItems(), this);
//		inventoryListView.setAdapter(itemAdapter);
//		registerForContextMenu(inventoryListView); TODO

		itemRVAdapter =
				new GroceryItemRVAdapter(
						itemData.getGroceryListItems(), this);
		itemRecyclerView.setAdapter(itemRVAdapter);
		layoutManager = new LinearLayoutManager(parent);
		itemRecyclerView.setLayoutManager(layoutManager);
		DividerItemDecoration divider = new DividerItemDecoration(parent, DividerItemDecoration.VERTICAL);
		itemRecyclerView.addItemDecoration(divider);
		itemRecyclerView.setHasFixedSize(false); // TODO enable this when you restrict item height later, right now long titles elongate an item

		initFinished = true;
		if (savedInstanceState != null) {
			parent.changeToCurrentList();
		}
		super.onActivityCreated(savedInstanceState);
	}
// TODO solve this context menu problem, this fragment has a different context menu than the invfragment but there's no layout for that menu atm
//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//		if (parent == null) {
//			return;
//		}
//
//		super.onCreateContextMenu(menu, v, menuInfo);
//		MenuInflater inflater = parent.getMenuInflater();
//		inflater.inflate(R.menu.list_item_context_menu, menu);
//	}
//
//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//		switch (item.getItemId()) {
//			case R.id.list_item_delete:
//				Item it = itemAdapter.getItem(menuInfo.position);
//				if (itemData.removeItem(it)) {
//					itemAdapter.notifyDataSetChanged();
//					return true;
//				}
//				else return false;
//			default:
//				return super.onContextItemSelected(item);
//		}
//		return super.onContextItemSelected(item);
//	}

	/**
	 * Call this when changes occur in other parts of the
	 * app that affect the inventory list.
	 */
	@Override
	public void refresh() {
//		itemAdapter.notifyDataSetInvalidated();
//		itemAdapter = new GroceryItemAdapter(parent,
//				itemData.getGroceryListItems(), this);
//		inventoryListView.setAdapter(itemAdapter); TODO
		itemRVAdapter.setItemsList(itemData.getGroceryListItems());
	}

	/**
	 * Removes the item from the grocery list and
	 * adds it to the inventory it belongs to.
	 * @param item the item to be removed.
	 */
	public void removeItem(Item item, int position) {
		//TODO might be able to do this by calling DbManager.updateItemColumn
		itemData.removeItem(item);
		item.setInGroceryList(false);
		itemData.addItem(item);
		itemRVAdapter.removeItem(position);
	}

	public boolean isInitFinished() {
		return initFinished;
	}
}
