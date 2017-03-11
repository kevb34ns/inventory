package com.kevinkyang.inventory;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object representing an inventory item
 */

public class Item implements Parcelable{
	private long rowID;
	private String name;
	private String createdDate;
	private String expiresDate;
	private int quantity;
	private String unit;
	private String type;
	private String inventory;
	private boolean inGroceryList;

	private int daysUntilExpiration;

	public Item(long rowID,
				String name,
				String createdDate,
				String expiresDate,
				int quantity,
				String unit,
				String type,
				String inventory,
				boolean inGroceryList) {
		this.rowID = rowID;
		this.name = name;
		this.createdDate = createdDate;
		this.expiresDate = expiresDate;
		this.quantity = quantity;
		this.unit = unit;
		this.type = type;
		this.inventory = inventory;
		this.inGroceryList = inGroceryList;
	}

	private Item(Parcel in) {
		rowID = in.readLong();
		name = in.readString();
		createdDate = in.readString();
		expiresDate = in.readString();
		quantity = in.readInt();
		unit = in.readString();
		type = in.readString();
		inventory = in.readString();
		inGroceryList = in.readByte() != 0;
		daysUntilExpiration = in.readInt();
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
		if (quantity >= 0) {
			// quantity MUST be non-negative
			this.quantity = quantity;
		}
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

	public boolean isInGroceryList() {
		return inGroceryList;
	}

	public void setInGroceryList(boolean inGroceryList) {
		this.inGroceryList = inGroceryList;
	}

	public int getDaysUntilExpiration() {
		return daysUntilExpiration;
	}

	public void setDaysUntilExpiration(int daysUntilExpiration) {
		this.daysUntilExpiration = daysUntilExpiration;
	}

	public static final Creator<Item> CREATOR = new Creator<Item>() {
		@Override
		public Item createFromParcel(Parcel in) {
			return new Item(in);
		}

		@Override
		public Item[] newArray(int size) {
			return new Item[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeLong(rowID);
		parcel.writeString(name);
		parcel.writeString(createdDate);
		parcel.writeString(expiresDate);
		parcel.writeInt(quantity);
		parcel.writeString(unit);
		parcel.writeString(type);
		parcel.writeString(inventory);
		parcel.writeByte((byte) (inGroceryList ? 1 : 0));
		parcel.writeInt(daysUntilExpiration);
	}
}
