package com.kevinkyang.inventory;

/**
 * Schema representing the SQLite database storing app data
 */

public class DBSchema {
	public static final class TABLE_ITEMS {
		public static final String TABLE_NAME = "items";

		// Column names
		public static final String KEY_NAME = "name";

		// Column indices
		public static final int COL_NAME = 0;
	}
}
