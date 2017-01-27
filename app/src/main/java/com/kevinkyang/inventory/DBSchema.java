package com.kevinkyang.inventory;

/**
 * Schema representing the SQLite database storing app data
 */

public class DBSchema {
	public static final class TABLE_ITEMS {
		public static final String TABLE_NAME = "items";

		// Column names
		public static final String ROW_ID = "rowid"; // provided by sqlite
		public static final String KEY_NAME = "name";
		public static final String KEY_CREATED = "created";
		public static final String KEY_EXPIRES = "expires";
		public static final String KEY_QUANTITY = "quantity";

		// Column indices
		public static final int COL_ROW_ID = 0;
		public static final int COL_NAME = 1;
		public static final int COL_CREATED = 2;
		public static final int COL_EXPIRES = 3;
		public static final int COL_QUANTITY = 4;

	}
}
