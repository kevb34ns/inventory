package com.kevinkyang.inventory;

/**
 * Created by Kevin on 12/13/2016.
 */

interface AddItemDialogListener {
	void onAddItemClicked(String name, int quantity, String unit,
						  String type, String expiresDate, String inventory,
						  boolean inGroceryList);

	void onSaveItemClicked(String name, int quantity, String unit,
						   String type, String expiresDate, String inventory,
						   boolean inGroceryList, Item item, int position);

	boolean isInGroceryMode();
}
