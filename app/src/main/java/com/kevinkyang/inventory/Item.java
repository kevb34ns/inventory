package com.kevinkyang.inventory;

/**
 * Object representing an inventory item
 */

public class Item {
	private long rowID;
	private String name;
	private String createdDate;
	private String expiresDate;
	private int quantity;
	private String unit;
	private String type;
	private String inventory;

	private int daysUntilExpiration;

	public Item(long rowID,
				String name,
				String createdDate,
				String expiresDate,
				int quantity,
				String unit,
				String type,
				String inventory) {
		this.rowID = rowID;
		this.name = name;
		this.createdDate = createdDate;
		this.expiresDate = expiresDate;
		this.quantity = quantity;
		this.unit = unit;
		this.type = type;
		this.inventory = inventory;
	}

	public long getRowID() {
		return rowID;
	}

	public void setRowID(long rowID) {
		this.rowID = rowID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getExpiresDate() {
		return expiresDate;
	}

	public void setExpiresDate(String expiresDate) {
		this.expiresDate = expiresDate;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getInventory() {
		return inventory;
	}

	public void setInventory(String inventory) {
		this.inventory = inventory;
	}

	public int getDaysUntilExpiration() {
		return daysUntilExpiration;
	}

	public void setDaysUntilExpiration(int daysUntilExpiration) {
		this.daysUntilExpiration = daysUntilExpiration;
	}
}
