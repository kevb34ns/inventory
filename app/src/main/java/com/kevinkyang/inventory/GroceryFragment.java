package com.kevinkyang.inventory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GroceryFragment extends Fragment implements CustomFragment {
	private MainActivity mParent;

	private ItemManager mItemManager = null;

	private RecyclerView mItemRecyclerView;
	private GroceryItemRVAdapter mItemRVAdapter;
	private LinearLayoutManager mLayoutManager;
	private ItemTouchHelper mItemTouchHelper;

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
		// this fragment can only be attached to MainActivity
		mParent = (MainActivity) getActivity();

		mItemManager = ItemManager.getInstance();

		mItemRVAdapter =
				new GroceryItemRVAdapter(
						mItemManager.getGroceryListItems(), this);
		mItemRecyclerView.setAdapter(mItemRVAdapter);
		mLayoutManager = new LinearLayoutManager(mParent);
		mItemRecyclerView.setLayoutManager(mLayoutManager);
		DividerItemDecoration divider = new DividerItemDecoration(mParent, DividerItemDecoration.VERTICAL);
		mItemRecyclerView.addItemDecoration(divider);
		mItemRecyclerView.setHasFixedSize(false);
		mItemTouchHelper = new ItemTouchHelper(
				new ListItemTouchHelperCallback(
						getContext(), mItemRVAdapter, true));
		mItemTouchHelper.attachToRecyclerView(mItemRecyclerView);

		mInitFinished = true;
		if (savedInstanceState != null) {
			mParent.changeToCurrentList();
		}
		super.onActivityCreated(savedInstanceState);
	}
// TODO solve this context menu problem, this fragment has a different context menu than the invfragment but there's no layout for that menu atm
//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//		if (mParent == null) {
//			return;
//		}
//
//		super.onCreateContextMenu(menu, v, menuInfo);
//		MenuInflater inflater = mParent.getMenuInflater();
//		inflater.inflate(R.menu.list_item_context_menu, menu);
//	}
//
//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//		switch (item.getItemId()) {
//			case R.id.list_item_delete:
//				ItemBase it = itemAdapter.getItem(menuInfo.position);
//				if (mItemManager.swapList(it)) {
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
		mItemRVAdapter.setItemsList(mItemManager.getGroceryListItems());
	}

	@Override
	public MainActivity getParent() {
		return mParent;
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

	@Override
	public void removeItem(Item item, int position) {
		if (mItemManager.removeItem(item)) {
			mItemRVAdapter.removeItem(position);
		}
	}

	/**
	 * Removes the item from the grocery list and
	 * adds it to the inventory it belongs to.
	 * @param item the item to be removed.
	 */
	@Override
	public void swapList(Item item, int position) {
		item.setInGroceryList(false);
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
		item.setInGroceryList(true);
		mItemManager.updateItem(item);
		mItemRVAdapter.addItem(item, position);
	}

	public boolean isInitFinished() {
		return mInitFinished;
	}
}
