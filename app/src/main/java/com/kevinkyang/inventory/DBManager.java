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
		database = new DBHelper(context).getWritableDatabase();
	}

	public ArrayList<Item> getItems() {
		ArrayList<Item> items = new ArrayList<Item>();
		if (database == null) return items;

		Cursor cursor = database.query(
				false, TABLE_ITEMS.TABLE_NAME,
				null, null, null, null, null, null, null);

		if (cursor.moveToFirst()) {
			// populate items array
			for(int i = 0; i < cursor.getCount(); i++) {
				String name = cursor.getString(TABLE_ITEMS.COL_NAME);
				items.add(new Item(name));
				cursor.moveToNext();
			}
		}
		cursor.close();

		return items;
	}

	public void addItem(Item item) {
		ContentValues cv = new ContentValues();
		cv.put(TABLE_ITEMS.KEY_NAME, item.getName());

		database.insert(TABLE_ITEMS.TABLE_NAME, null, cv);
	}

}
