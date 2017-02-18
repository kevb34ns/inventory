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

import java.util.ArrayList;

/**
 * Created by Kevin on 2/17/2017.
 */

public class GroceryFragment extends Fragment {
	private MainActivity parent;

	private ListView inventoryListView;
	private ItemAdapter itemAdapter;
	private ArrayList<Item> tempItems;

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

		tempItems = new ArrayList<Item>();
		tempItems.add(new Item(0, "Peanut Butter", "02/17/2017", "02/20/2017", 2));
		itemAdapter = new ItemAdapter(parent, tempItems);
		inventoryListView.setAdapter(itemAdapter);
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
		return super.onContextItemSelected(item);
	}

	/**
	 * Call this when changes occur in other parts of the
	 * app that affect the inventory list.
	 */
	public void refresh() {
		itemAdapter.notifyDataSetChanged();
	}
}
