package com.kevinkyang.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.kevinkyang.inventory.DBSchema.TABLE_ITEMS;
import com.kevinkyang.inventory.DBSchema.TABLE_INVENTORY_INFO;

/**
 * Created by Kevin on 12/14/2016.
 */

public class DBManager {
	private static DBManager instance = null;
	private Context context = null;
	private SQLiteDatabase database = null;

	private ArrayList<String> inventories = null;
	private ArrayList<Item> items = null;

	private DBManager() {

	}

	public static DBManager getInstance() {
		if (instance == null) {
			instance = new DBManager();
		}
		return instance;
	}

	public void init(Context context) {
		this.context = context;
		// TODO should not call getWritableDatabase from main thread
		// TODO main thread will have to wait to populate the list
		database = new DBHelper(context).getWritableDatabase();
	}

	public boolean isInitialized() {
		// TODO database has a close method, check when to close/if it's needed, and if it's needed, must check if database is closed in this method
		return (context != null && database != null);
	}

	/**
	 * Returns the list of inventory names. Gets
	 * them from the database if it is the first
	 * time this method is called.
	 * @return a copy of the inventory names list.
	 */
	public ArrayList<String> getInventories() {
		if (inventories == null) {
			inventories = new ArrayList<String>();
			if (database == null) {
				return inventories;
			}

			String[] cols = {
					TABLE_INVENTORY_INFO.KEY_NAME,
					TABLE_INVENTORY_INFO.KEY_COLOR
			};

			Cursor cursor = database.query(
					false, TABLE_INVENTORY_INFO.TABLE_NAME,
					cols, null, null, null, null, null, null);

			if (cursor.moveToFirst()) {
				while(true) {
					String name = cursor.getString(TABLE_INVENTORY_INFO.COL_NAME);
					String color = cursor.getString(TABLE_INVENTORY_INFO.COL_COLOR); //TODO

					inventories.add(name);
					if (!cursor.moveToNext()) {
						break;
					}
				}
			}

			cursor.close();

			if (inventories.isEmpty()) {
				addDefaultInventories();
			}
		}

		return new ArrayList<String>(inventories);
	}

	public ArrayList<Item> getItems() {
		if (items == null) {
			items = new ArrayList<Item>();
			if (database == null) {
				return items;
			}

			if (inventories == null) {
				getInventories();
			}

			String[] cols = {
					TABLE_ITEMS.ROW_ID,
					TABLE_ITEMS.KEY_NAME,
					TABLE_ITEMS.KEY_CREATED,
					TABLE_ITEMS.KEY_EXPIRES,
					TABLE_ITEMS.KEY_QUANTITY,
					TABLE_ITEMS.KEY_UNIT,
					TABLE_ITEMS.KEY_TYPE,
					TABLE_ITEMS.KEY_INVENTORY
			};

			Cursor cursor = database.query(
					false, TABLE_ITEMS.TABLE_NAME,
					cols, null, null, null, null, null, null);

			if (cursor.moveToFirst()) {
				// populate items array
				for (int i = 0; i < cursor.getCount(); i++) {
					long rowID = cursor.getLong(TABLE_ITEMS.COL_ROW_ID);
					String name = cursor.getString(TABLE_ITEMS.COL_NAME);
					String created = cursor.getString(TABLE_ITEMS.COL_CREATED);
					String expires = cursor.getString(TABLE_ITEMS.COL_EXPIRES);
					int quantity = cursor.getInt(TABLE_ITEMS.COL_QUANTITY);
					String unit = cursor.getString(TABLE_ITEMS.COL_UNIT);
					String type = cursor.getString(TABLE_ITEMS.COL_TYPE);
					String[] invTokens = cursor.getString(
							TABLE_ITEMS.COL_INVENTORY).split("\\|");
					boolean inGroceryList = false;
					String inventory = "";
					if (invTokens[0].equals("Grocery")) {
						inGroceryList = true;
						if (invTokens.length == 2) {
							inventory = invTokens[1];
						}
					} else {
						inventory = invTokens[0];
					}

					// add unlisted inventories to the database
					if (!inventory.isEmpty() && !inventories.contains(inventory)) {
						addInventory(inventory);
						inventories.add(inventory);
					}

					items.add(new Item(rowID, name, created, expires, quantity, unit, type, inventory, inGroceryList));
					cursor.moveToNext();
				}
			}
			cursor.close();
		}

		return items;
	}

	public void addItem(Item item) {
		ContentValues cv = new ContentValues();
		cv.put(TABLE_ITEMS.KEY_NAME, item.getName());
		cv.put(TABLE_ITEMS.KEY_CREATED, item.getCreatedDate());
		cv.put(TABLE_ITEMS.KEY_EXPIRES, item.getExpiresDate());
		cv.put(TABLE_ITEMS.KEY_QUANTITY, item.getQuantity());
		cv.put(TABLE_ITEMS.KEY_UNIT, item.getUnit());
		cv.put(TABLE_ITEMS.KEY_TYPE, item.getType());
		cv.put(TABLE_ITEMS.KEY_INVENTORY, getInventoryString(item));

		long rowID = database.insert(TABLE_ITEMS.TABLE_NAME, null, cv);
		item.setRowID(rowID);
	}

	//TODO may want to return a boolean and have callers check for success
	public void updateItem(Item item) {
		ContentValues values = new ContentValues();
		values.put(TABLE_ITEMS.KEY_NAME,
				item.getName());
		values.put(TABLE_ITEMS.KEY_CREATED,
				item.getCreatedDate());
		values.put(TABLE_ITEMS.KEY_EXPIRES,
				item.getExpiresDate());
		values.put(TABLE_ITEMS.KEY_QUANTITY,
				item.getQuantity());
		values.put(TABLE_ITEMS.KEY_UNIT,
				item.getUnit());
		values.put(TABLE_ITEMS.KEY_TYPE,
				item.getType());
		values.put(TABLE_ITEMS.KEY_INVENTORY,
				getInventoryString(item));

		String[] whereArgs = { String.valueOf(item.getRowID()) };
		database.update(TABLE_ITEMS.TABLE_NAME, values, "rowid=?", whereArgs);
	}

	public void updateItemColumn(Item item, int col) {
		ContentValues cv = new ContentValues();
		switch (col) {
			case TABLE_ITEMS.COL_NAME:
				cv.put(TABLE_ITEMS.KEY_NAME, item
					.getName());
				break;
			case TABLE_ITEMS.COL_CREATED:
				cv.put(TABLE_ITEMS.KEY_CREATED, item
					.getCreatedDate());
				break;
			case TABLE_ITEMS.COL_EXPIRES:
				cv.put(TABLE_ITEMS.KEY_EXPIRES, item
					.getExpiresDate());
				break;
			case TABLE_ITEMS.COL_QUANTITY:
				cv.put(TABLE_ITEMS.KEY_QUANTITY, item
					.getQuantity());
				break;
			case TABLE_ITEMS.COL_UNIT:
				cv.put(TABLE_ITEMS.KEY_UNIT, item
					.getUnit());
				break;
			case TABLE_ITEMS.COL_TYPE:
				cv.put(TABLE_ITEMS.KEY_TYPE, item
					.getType());
				break;
			case TABLE_ITEMS.COL_INVENTORY:
				cv.put(TABLE_ITEMS.KEY_INVENTORY, getInventoryString(item));
				break;
			default: return;
		}

		String[] whereArgs = { String.valueOf(item.getRowID()) };
		database.update(TABLE_ITEMS.TABLE_NAME, cv, "rowid=?", whereArgs);
	}

	public int removeItem(Item item) {
		String[] whereArgs = { String.valueOf(item.getRowID()) };
		return database.delete(TABLE_ITEMS.TABLE_NAME, "rowid=?", whereArgs);
	}

	/**
	 * Adds a new inventory to the app.
	 * @param inventory the name of the inventory
	 *                  to be added.
	 * @return true if it was added successfully,
	 * false if it already exists in the database.
	 */
	public boolean addInventory(String inventory) {
		if (inventory == null) {
			return false;
		} else if (inventory.isEmpty() ||
				inventories.contains(inventory)) {
			return false;
		}

		// TODO handle colors in the future
		ContentValues cv = new ContentValues();
		cv.put(TABLE_INVENTORY_INFO.KEY_NAME, inventory);
		cv.put(TABLE_INVENTORY_INFO.KEY_COLOR, "0x00000000");
		long id = database.insert(TABLE_INVENTORY_INFO.TABLE_NAME,
				null, cv);

		if (id == -1) {
			return false;
		}

		if (inventories != null) {
			inventories.add(inventory);
		}

		return true;
	}

	public void updateInventory(String inventory, String color) {
		// TODO
	}

	public void removeInventory(String inventory) {
		// TODO
	}

	private void addDefaultInventories() {
		String[] default_inventories = {
				"Fridge",
				"Freezer",
				"Pantry"
		};

		for (String s : default_inventories) {
			addInventory(s);
		}
	}

	private String getInventoryString(Item item) {
		String invString = "";
		if (item.isInGroceryList()) {
			invString += "Grocery|";
		}
		invString += item.getInventory();
		return invString;
	}
}
