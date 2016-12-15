package com.kevinkyang.inventory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Kevin on 12/14/2016.
 */

public class DBHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "items.db";
	public static final int DB_VERSION = 1;
	public static final String DB_ROOT_PATH = "/data/data/";
	public static final String DB_SUB_PATH = "/databases/";

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

	}
}
