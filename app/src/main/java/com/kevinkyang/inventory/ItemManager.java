package com.kevinkyang.inventory;

import android.content.Context;
import android.os.AsyncTask;

import com.kevinkyang.trie.Trie;

import java.util.ArrayList;

/**
 * singleton class that holds the item data
 */

public class ItemManager {
	private ArrayList<Item> mItems = null;
	private Trie mSearchTrie = new Trie();
	private DBManager mDbManager = DBManager.getInstance();

	private static ItemManager mInstance = null;
	private Context mContext = null;

	public static ItemManager getInstance() {
		if (mInstance == null) {
			mInstance = new ItemManager();
		}
		return mInstance;
	}

	private ItemManager() {

	}

	public void init(Context context) {
		this.mContext = context;
		if (!mDbManager.isInitialized()) {
			mDbManager.init(context);
		}
		mItems = mDbManager.getItems();
		mSearchTrie.buildTrie(getItemNames());
	}

	public boolean isInitialized() {
		return mContext != null;
	}

	public void addItem(Item item) {
		mItems.add(item);
		mSearchTrie.addWord(item.getName());
		mDbManager.addItem(item);
	}

	public void updateItem(Item item) {
		mDbManager.updateItem(item);
		mSearchTrie.addWord(item.getName());
	}

	public boolean removeItem(Item item) {
		if (mDbManager.removeItem(item) == 1) {
			mItems.remove(item);
			if (!doesNameExist(item.getName())) {
				mSearchTrie.removeWord(item.getName());
			}
			return true;
		} else return false;
	}

	/**
	 * Get all mItems except for those in the
	 * grocery list.
	 *
	 * @return List of mItems in the inventory.
	 */
	public ArrayList<Item> getInventoryItems() {
		ArrayList<Item> results = new ArrayList<Item>();
		for (Item i : mItems) {
			if (!i.isInGroceryList()) {
				results.add(i);
			}
		}
		return results;
	}

	/**
	 * @param inventory the mName of the inventory the mItems
	 *                  are located in, or null to return
	 *                  all mItems.
	 * @return a subset of the inventory mItems based on the
	 * inventory it belongs to.
	 */
	public ArrayList<Item> getItemsByInventory(String inventory) {
		return getItemsByInventory(inventory, 3);
	}

	public ArrayList<Item> getItemsByInventory(
			String inventory, int expirationInterval) {
		if (inventory == null) {
			return getInventoryItems();
		} else if (inventory.equals("Expiring")) {
			return getExpiringItems(expirationInterval);
		}

		ArrayList<Item> results = new ArrayList<Item>();
		for (Item i : mItems) {
			if (!i.isInGroceryList() &&
					i.getInventory().equals(inventory)) {
				results.add(i);
			}
		}

		return results;
	}

	/**
	 * @param inventory the mName of the inventory the mItems
	 *                  are located in, or null for all mItems.
	 * @return the number of mItems in the specified inventory
	 */
	public int getInventoryItemCount(String inventory) {
		return getItemsByInventory(inventory).size();
	}

	public ArrayList<Item> getGroceryListItems() {
		ArrayList<Item> results = new ArrayList<Item>();
		for (Item i : mItems) {
			if (i.isInGroceryList()) {
				results.add(i);
			}
		}

		return results;
	}

	public ArrayList<Item> getExpiringItems(int rangeInDays) {
		ArrayList<Item> expiring = new ArrayList<>();
		for (Item item : mItems) {
			if (item.isInGroceryList()) {
				// ignore grocery mItems
				continue;
			}

			String expiresDate = item.getExpiresDate();
			if (expiresDate.isEmpty()) {
				// item has no expiration set
				continue;
			}

			int daysUntilExpiration =
					TimeManager.getDateDifferenceInDays(
							TimeManager.getDateTimeLocal(),
							item.getExpiresDate());
			item.setDaysUntilExpiration(daysUntilExpiration);
			if (daysUntilExpiration <= rangeInDays) {
				expiring.add(item);
			}
		}

		return expiring;
	}

	public void search(String query, OnSearchFinishedListener listener) {
		ItemSearchTask itemSearch = new ItemSearchTask(listener);
		itemSearch.execute(query);
	}

	private ArrayList<String> getItemNames() {
		ArrayList<String> names = new ArrayList<>();

		if (mItems != null) {
			for (Item item : mItems) {
				names.add(item.getName());
			}
		}

		return names;
	}

	/**
	 * Checks whether a given item mName exists in the mItems list.
	 *
	 * @param name The mName of the item.
	 * @return true if at least one item has this mName, false otherwise.
	 */
	private boolean doesNameExist(String name) {

		if (mItems != null) {
			for (Item item : mItems) {
				if (item.getName().equals(name)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * A listener interface to be used by clients to receive
	 * the results of an item search.
	 */
	public interface OnSearchFinishedListener {
		public void onSearchFinished(ArrayList<Item> results);
	}

	private class ItemSearchTask
			extends AsyncTask<String, Void, ArrayList<Item>> {

		private OnSearchFinishedListener mListener;

		public ItemSearchTask(OnSearchFinishedListener listener) {
			this.mListener = listener;
		}

		@Override
		protected ArrayList<Item> doInBackground(String... strings) {
			ArrayList<Item> result = new ArrayList<>();

			// search for the first string parameter only
			if (strings.length == 0 || strings[0] == null) {
				return result;
			}

			ArrayList<String> names = mSearchTrie.search(strings[0]);
			for (String name : names) {
				for (Item item : mItems) {
					String itemName = item.getName().toLowerCase();
					if (itemName.equals(name)) {
						result.add(item);
					}
				}
			}

			return result;
		}

		@Override
		protected void onPostExecute(ArrayList<Item> result) {
			if (mListener != null) {
				mListener.onSearchFinished(result);
			}
		}
	}
}

