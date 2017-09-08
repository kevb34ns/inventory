package com.kevinkyang.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import com.kevinkyang.inventory.DBSchema.TABLE_ITEMS;
import com.kevinkyang.inventory.DBSchema.TABLE_INVENTORY_INFO;

public class DBManager {
	private static DBManager mInstance = null;
	private Context mContext = null;
	private SQLiteDatabase mDatabase = null;

	private ArrayList<String> mInventories = null;

	private DBManager() {

	}

	public static DBManager getInstance() {
		if (mInstance == null) {
			mInstance = new DBManager();
		}
		return mInstance;
	}

	public void init(Context context) {
		this.mContext = context;
		// TODO should not call getWritableDatabase from main thread
		// TODO main thread will have to wait to populate the list
		mDatabase = new DBHelper(context).getWritableDatabase();
	}

	public boolean isInitialized() {
		// TODO mDatabase has a close method, check when to close/if it's needed, and if it's needed, must check if mDatabase is closed in this method
		return (mContext != null && mDatabase != null);
	}

	/**
	 * Returns the list of inventory names. Gets
	 * them from the mDatabase if it is the first
	 * time this method is called.
	 * @return a copy of the inventory names list.
	 */
	public ArrayList<String> getInventories() {
		if (mInventories == null) {
			mInventories = new ArrayList<String>();
			if (mDatabase == null) {
				return mInventories;
			}

			String[] cols = {
					TABLE_INVENTORY_INFO.KEY_NAME,
					TABLE_INVENTORY_INFO.KEY_COLOR
			};

			Cursor cursor = mDatabase.query(
					false, TABLE_INVENTORY_INFO.TABLE_NAME,
					cols, null, null, null, null, null, null);

			if (cursor.moveToFirst()) {
				while(true) {
					String name = cursor.getString(TABLE_INVENTORY_INFO.COL_NAME);
					String color = cursor.getString(TABLE_INVENTORY_INFO.COL_COLOR); //TODO

					mInventories.add(name);
					if (!cursor.moveToNext()) {
						break;
					}
				}
			}

			cursor.close();

			if (mInventories.isEmpty()) {
				addDefaultInventories();
			}
		}

		return new ArrayList<String>(mInventories);
	}

	public ArrayList<Item> getItems() {
		ArrayList<Item> items = new ArrayList<Item>();
		if (mDatabase == null) {
			return items;
		}

		if (mInventories == null) {
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

		Cursor cursor = mDatabase.query(
				false, TABLE_ITEMS.TABLE_NAME,
				cols, null, null, null, null, null, null);

		if (cursor.moveToFirst()) {
			// populate items array
			for (int i = 0; i < cursor.getCount(); i++) {
				long rowID = cursor.getLong(TABLE_ITEMS.COL_ROW_ID);
				String name = cursor.getString(TABLE_ITEMS.COL_NAME);
				String created = cursor.getString(TABLE_ITEMS.COL_CREATED);
				String expires = cursor.getString(TABLE_ITEMS.COL_EXPIRES);
				float quantity = cursor.getFloat(TABLE_ITEMS.COL_QUANTITY);
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

				// add unlisted mInventories to the mDatabase
				if (!inventory.isEmpty() && !mInventories.contains(inventory)) {
					addInventory(inventory);
					mInventories.add(inventory);
				}

				items.add(new Item(rowID, name, created, expires, quantity, unit, type, inventory, inGroceryList));
				cursor.moveToNext();
			}
		}
		cursor.close();

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

		long rowID = mDatabase.insert(TABLE_ITEMS.TABLE_NAME, null, cv);
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
		mDatabase.update(TABLE_ITEMS.TABLE_NAME, values, "rowid=?", whereArgs);
	}

	public int removeItem(Item item) {
		String[] whereArgs = { String.valueOf(item.getRowID()) };
		return mDatabase.delete(TABLE_ITEMS.TABLE_NAME, "rowid=?", whereArgs);
	}

	/**
	 * Adds a new inventory to the app.
	 * @param inventory the mName of the inventory
	 *                  to be added.
	 * @return true if it was added successfully,
	 * false if it already exists in the mDatabase.
	 */
	public boolean addInventory(String inventory) {
		if (inventory == null) {
			return false;
		} else if (inventory.isEmpty() ||
				mInventories.contains(inventory)) {
			return false;
		}

		// TODO handle colors in the future
		ContentValues cv = new ContentValues();
		cv.put(TABLE_INVENTORY_INFO.KEY_NAME, inventory);
		cv.put(TABLE_INVENTORY_INFO.KEY_COLOR, "0x00000000");
		long id = mDatabase.insert(TABLE_INVENTORY_INFO.TABLE_NAME,
				null, cv);

		if (id == -1) {
			return false;
		}

		if (mInventories != null) {
			mInventories.add(inventory);
		}

		return true;
	}

	/**
	 * Update the display color of the specified inventory.
	 * @param inventory The mName of an existing inventory.
	 * @param color A hexadecimal string representing a color, in
	 *              the format "0xRRGGBBAA".
	 * @return true if the inventory was updated, false otherwise.
	 */
	public boolean updateInventory(String inventory, String color) {
		if (inventory == null || inventory.isEmpty() ||
				!mInventories.contains(inventory)) {
			return false;
		}

		StringBuilder sb =
				new StringBuilder(TABLE_INVENTORY_INFO.KEY_NAME);
		sb.append("=?");
		String where = sb.toString();
		String[] args = {inventory};
		ContentValues cv = new ContentValues();
		cv.put(TABLE_INVENTORY_INFO.KEY_NAME, inventory);
		cv.put(TABLE_INVENTORY_INFO.KEY_COLOR, color);

		long result = mDatabase.update(
				TABLE_INVENTORY_INFO.TABLE_NAME,
				cv, where, args);

		return result != -1;
	}

	public boolean removeInventory(String inventory) {
		if (inventory == null || inventory.isEmpty() ||
				!mInventories.contains(inventory)) {
			return false;
		}

		StringBuilder sb =
				new StringBuilder(TABLE_INVENTORY_INFO.KEY_NAME);
		sb.append("=?");
		String where = sb.toString();
		String[] args = {inventory};

		int result = mDatabase.delete(
				TABLE_INVENTORY_INFO.TABLE_NAME, where, args);

		return result > 0;
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
