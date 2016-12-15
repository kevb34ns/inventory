package com.kevinkyang.inventory;

/**
 * Schema representing the SQLite database storing app data
 */

public class DBSchema {
	public static final class TABLE_ITEMS {
		// Column names
		public static final String KEY_ID = "id";
		public static final String KEY_NAME = "name";

		// Column indices
		public static final int COL_ID = 0;
		public static final int COL_NAME = 1;
	}
}
