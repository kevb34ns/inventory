package com.kevinkyang.inventory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
		itemAdapter = new GroceryItemAdapter(parent, itemData.getGroceryListItems(), this);
		inventoryListView.setAdapter(itemAdapter);
		registerForContextMenu(inventoryListView);
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
		itemAdapter.notifyDataSetInvalidated();
		itemAdapter = new GroceryItemAdapter(parent,
				itemData.getGroceryListItems(), this);
		inventoryListView.setAdapter(itemAdapter);
	}

	/**
	 * Removes the item from the grocery list and
	 * adds it to the inventory it belongs to.
	 * @param item the item to be removed.
	 */
	public void removeItem(Item item) {
		//TODO might be able to do this by calling DbManager.updateItem
		itemData.removeItem(item);
		item.setInGroceryList(false);
		itemData.addItem(item);
		refresh();
	}
}
