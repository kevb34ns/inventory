package com.kevinkyang.inventory;

import android.content.Context;
import android.os.AsyncTask;

import com.kevinkyang.trie.Trie;

import java.util.ArrayList;

/**
 * singleton class that holds the item data
 */

public class ItemManager {
	private ArrayList<Item> items = null;
	private Trie searchTrie = new Trie();
	private DBManager dbManager = DBManager.getInstance();

	private static ItemManager instance = null;
	private Context context = null;

	public static ItemManager getInstance() {
		if (instance == null) {
			instance = new ItemManager();
		}
		return instance;
	}

	private ItemManager() {

	}

	public void init(Context context) {
		this.context = context;
		if (!dbManager.isInitialized()) {
			dbManager.init(context);
		}
		items = dbManager.getItems();
		searchTrie.buildTrie(getItemNames());
	}

	public boolean isInitialized() {
		return context != null;
	}

	public void addItem(Item item) {
		items.add(item);
		searchTrie.addWord(item.getName());
		dbManager.addItem(item);
	}

	public void updateItem(Item item) {
		dbManager.updateItem(item);
		searchTrie.addWord(item.getName());
	}

	public boolean removeItem(Item item) {
		if (dbManager.removeItem(item) == 1) {
			items.remove(item);
			if (!doesNameExist(item.getName())) {
				searchTrie.removeWord(item.getName());
			}
			return true;
		} else return false;
	}

	/**
	 * Get all items except for those in the
	 * grocery list.
	 *
	 * @return List of items in the inventory.
	 */
	public ArrayList<Item> getInventoryItems() {
		ArrayList<Item> results = new ArrayList<Item>();
		for (Item i : items) {
			if (!i.isInGroceryList()) {
				results.add(i);
			}
		}
		return results;
	}

	/**
	 * TODO description here
	 *
	 * @param inventory the name of the inventory the items
	 *                  are located in, or null to return
	 *                  all items.
	 * @return a subset of the inventory items based on the
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
		for (Item i : items) {
			if (!i.isInGroceryList() &&
					i.getInventory().equals(inventory)) {
				results.add(i);
			}
		}

		return results;
	}

	/**
	 * TODO inefficient alg, description needed
	 *
	 * @param inventory the name of the inventory the items
	 *                  are located in, or null for all items.
	 * @return the number of items in the specified inventory
	 */
	public int getInventoryItemCount(String inventory) {
		return getItemsByInventory(inventory).size();
	}

	public ArrayList<Item> getGroceryListItems() {
		ArrayList<Item> results = new ArrayList<Item>();
		for (Item i : items) {
			if (i.isInGroceryList()) {
				results.add(i);
			}
		}

		return results;
	}

	public ArrayList<Item> getExpiringItems(int rangeInDays) {
		ArrayList<Item> expiring = new ArrayList<>();
		for (Item item : items) {
			if (item.isInGroceryList()) {
				// ignore grocery items
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

		if (items != null) {
			for (Item item : items) {
				names.add(item.getName());
			}
		}

		return names;
	}

	/**
	 * Checks whether a given item name exists in the items list.
	 *
	 * @param name The name of the item.
	 * @return true if at least one item has this name, false otherwise.
	 */
	private boolean doesNameExist(String name) {

		if (items != null) {
			for (Item item : items) {
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

		private OnSearchFinishedListener listener;

		public ItemSearchTask(OnSearchFinishedListener listener) {
			this.listener = listener;
		}

		@Override
		protected ArrayList<Item> doInBackground(String... strings) {
			ArrayList<Item> result = new ArrayList<>();

			//TODO for now, search the first string in the params only; in the future, could have multiple
			if (strings.length == 0 || strings[0] == null) {
				return result;
			}

			ArrayList<String> names = searchTrie.search(strings[0]);
			for (String name : names) {
				for (Item item : items) {
					if (item.getName() == name) {
						result.add(item);
					}
				}
			}

			return result;
		}

		@Override
		protected void onPostExecute(ArrayList<Item> result) {
			if (listener != null) {
				listener.onSearchFinished(result);
			}
		}
	}
}

