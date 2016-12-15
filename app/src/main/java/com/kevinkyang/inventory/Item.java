package com.kevinkyang.inventory;

/**
 * Object representing an inventory item
 */

public class Item {
	private String name;

	public Item(String name) {
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
