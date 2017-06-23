package com.kevinkyang.inventory;

interface ItemChangeListener {
	void onItemAdded(String name, int quantity, String unit,
					 String type, String expiresDate, String inventory,
					 boolean inGroceryList);

	void onItemSaved(String name, int quantity, String unit,
					 String type, String expiresDate, String inventory,
					 boolean inGroceryList, Item item, int position);

	//TODO the following two methods do not necessarily fit the name/purpose of the class
	void onDialogDismissed();

	boolean isInGroceryMode();
}
