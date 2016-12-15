package com.kevinkyang.inventory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kevinkyang.inventory.DBSchema.TABLE_ITEMS;

/**
 * Created by Kevin on 12/14/2016.
 */

public class DBHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "items.db";
	public static final int DB_VERSION = 1;

	// DB creation SQL statement
	public static final String CREATE_TABLE_ITEMS =
			"CREATE TABLE " +
					TABLE_ITEMS.TABLE_NAME + " (" +
					TABLE_ITEMS.KEY_NAME + " TEXT" +
					");";

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL(CREATE_TABLE_ITEMS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
		// TODO upgrade db
	}
}
