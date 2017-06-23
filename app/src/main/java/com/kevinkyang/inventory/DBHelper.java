package com.kevinkyang.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kevinkyang.inventory.DBSchema.TABLE_ITEMS;
import com.kevinkyang.inventory.DBSchema.TABLE_INVENTORY_INFO;

public class DBHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "items.db";
	public static final int DB_VERSION = 1;

	// DB creation SQL statement
	public static final String CREATE_TABLE_ITEMS =
			"CREATE TABLE " +
					TABLE_ITEMS.TABLE_NAME + " (" +
					TABLE_ITEMS.KEY_NAME + " TEXT, " +
					TABLE_ITEMS.KEY_CREATED + " TEXT, " +
					TABLE_ITEMS.KEY_EXPIRES + " TEXT, " +
					TABLE_ITEMS.KEY_QUANTITY + " INTEGER, " +
					TABLE_ITEMS.KEY_UNIT + " TEXT, " +
					TABLE_ITEMS.KEY_TYPE + " TEXT, " +
					TABLE_ITEMS.KEY_INVENTORY + " TEXT" +
					");";

	public static final String CREATE_TABLE_INVENTORY_INFO =
			"CREATE TABLE " +
					TABLE_INVENTORY_INFO.TABLE_NAME + " (" +
					TABLE_INVENTORY_INFO.KEY_NAME +
					" TEXT PRIMARY KEY, " +
					TABLE_INVENTORY_INFO.KEY_COLOR + " TEXT" +
					");";

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL(CREATE_TABLE_ITEMS);
		sqLiteDatabase.execSQL(CREATE_TABLE_INVENTORY_INFO);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
		// TODO upgrade db
	}
}
