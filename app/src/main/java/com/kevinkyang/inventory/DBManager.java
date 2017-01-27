package com.kevinkyang.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import com.kevinkyang.inventory.DBSchema.TABLE_ITEMS;

/**
 * Created by Kevin on 12/14/2016.
 */

public class DBManager {
	private static DBManager instance = new DBManager();
	private Context context = null;
	private SQLiteDatabase database = null;

	private DBManager() {

	}

	public static DBManager getInstance() {
		return instance;
	}

	public void init(Context context) {
		this.context = context;
		// TODO should not call getWritableDatabase from main thread
		// TODO main thread will have to wait to populate the list
		database = new DBHelper(context).getWritableDatabase();
	}

	public ArrayList<Item> getItems() {
		ArrayList<Item> items = new ArrayList<Item>();
		if (database == null) return items;

		String[] cols = {
				TABLE_ITEMS.ROW_ID,
				TABLE_ITEMS.KEY_NAME,
				TABLE_ITEMS.KEY_CREATED,
				TABLE_ITEMS.KEY_EXPIRES,
				TABLE_ITEMS.KEY_QUANTITY
		};

		Cursor cursor = database.query(
				false, TABLE_ITEMS.TABLE_NAME,
				cols, null, null, null, null, null, null);

		if (cursor.moveToFirst()) {
			// populate items array
			for(int i = 0; i < cursor.getCount(); i++) {
				long rowID = cursor.getLong(TABLE_ITEMS.COL_ROW_ID);
				String name = cursor.getString(TABLE_ITEMS.COL_NAME);
				String created = cursor.getString(TABLE_ITEMS.COL_CREATED);
				String expires = cursor.getString(TABLE_ITEMS.COL_EXPIRES);
				int quantity = cursor.getInt(TABLE_ITEMS.COL_QUANTITY);
				items.add(new Item(rowID, name, created, expires, quantity));
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

		long rowID = database.insert(TABLE_ITEMS.TABLE_NAME, null, cv);
		item.setRowID(rowID);
	}

	public void updateItem(Item item, int col) {
		ContentValues cv = new ContentValues();
		switch (col) {
			case TABLE_ITEMS.COL_NAME:
				cv.put(TABLE_ITEMS.KEY_NAME, item.getName()); break;
			case TABLE_ITEMS.COL_CREATED:
				cv.put(TABLE_ITEMS.KEY_CREATED, item.getCreatedDate()); break;
			case TABLE_ITEMS.COL_EXPIRES:
				cv.put(TABLE_ITEMS.KEY_EXPIRES, item.getExpiresDate()); break;
			case TABLE_ITEMS.COL_QUANTITY:
				cv.put(TABLE_ITEMS.KEY_QUANTITY, item.getQuantity()); break;
			default: return;
		}

		String[] whereArgs = { String.valueOf(item.getRowID()) };
		database.update(TABLE_ITEMS.TABLE_NAME, cv, "rowid=?", whereArgs);
	}

	public int removeItem(Item item) {
		String[] whereArgs = { String.valueOf(item.getRowID()) };
		return database.delete(TABLE_ITEMS.TABLE_NAME, "rowid=?", whereArgs);
	}
}
