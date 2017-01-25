package com.kevinkyang.inventory;

/**
 * Object representing an inventory item
 */

public class Item {
	private String name;
	private String createdDate;
	private String expiresDate;
	private int quantity;

	public Item(String name,
				String createdDate,
				String expiresDate,
				int quantity) {
		this.name = name;
		this.createdDate = createdDate;
		this.expiresDate = expiresDate;
		this.quantity = quantity;
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
}
