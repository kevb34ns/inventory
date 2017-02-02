package com.kevinkyang.inventory;

import java.util.ArrayList;

/**
 * singleton class that holds the item data
 */

public class ItemData {
	/**
	 * TODO consideration: the app will have to
	 * display different inventories, and various subsets
	 * of inventories. So this class will need to consider the
	 * metadata and be able to produce different data sets
	 */
	private ArrayList<Item> items = null;
	private DBManager dbManager = DBManager.getInstance();

	private static ItemData instance = new ItemData();

	public static ItemData getInstance() {
		return instance;
	}

	private ItemData() {
		items = dbManager.getItems();
	}

	public void addItem(Item item) {
		items.add(item);
		dbManager.addItem(item);
	}

	public boolean removeItem(Item item) {
		if (dbManager.removeItem(item) == 1) {
			items.remove(item);
			return true;
		}
		else return false;
	}

	public ArrayList<Item> getItems() {
		return items;
	}

	public ArrayList<Item> getExpiringItems(int rangeInDays) {
		ArrayList<Item> expiring = new ArrayList<Item>();
		for (Item item : items) {
			int daysUntilExpiration =
					TimeManager.getDateDifferenceInDays(
							item.getCreatedDate(),
							item.getExpiresDate());
			item.setDaysUntilExpiration(daysUntilExpiration);
			if (daysUntilExpiration <= rangeInDays) {
				expiring.add(item);
			}
		}

		return expiring;
	}
}
