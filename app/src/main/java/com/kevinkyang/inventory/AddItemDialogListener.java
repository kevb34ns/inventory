package com.kevinkyang.inventory;

/**
 * Created by Kevin on 12/13/2016.
 */

interface AddItemDialogListener {
	void onAddItemClicked(String name, int quantity, int expCode,
						  String unit, String type, String inventory);
}
