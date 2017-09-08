package com.kevinkyang.inventory;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object representing an mInventory item
 */

public class Item implements Parcelable{
	private long mRowID;
	private String mName;
	private String mCreatedDate;
	private String mExpiresDate;
	private float mQuantity;
	private String mUnit;
	private String mType;
	private String mInventory;
	private boolean mInGroceryList;

	private int mDaysUntilExpiration;

	public Item(long rowID,
				String name,
				String createdDate,
				String expiresDate,
				float quantity,
				String unit,
				String type,
				String inventory,
				boolean inGroceryList) {
		mRowID = rowID;
		mName = name;
		mCreatedDate = createdDate;
		mExpiresDate = expiresDate;
		mQuantity = quantity;
		mUnit = unit;
		mType = type;
		mInventory = inventory;
		mInGroceryList = inGroceryList;
	}

	private Item(Parcel in) {
		mRowID = in.readLong();
		mName = in.readString();
		mCreatedDate = in.readString();
		mExpiresDate = in.readString();
		mQuantity = in.readFloat();
		mUnit = in.readString();
		mType = in.readString();
		mInventory = in.readString();
		mInGroceryList = in.readByte() != 0;
		mDaysUntilExpiration = in.readInt();
	}

	public long getRowID() {
		return mRowID;
	}

	public void setRowID(long rowID) {
		this.mRowID = rowID;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getCreatedDate() {
		return mCreatedDate;
	}

	public void setCreatedDate(String createdDate) {
		this.mCreatedDate = createdDate;
	}

	public String getExpiresDate() {
		return mExpiresDate;
	}

	public void setExpiresDate(String expiresDate) {
		this.mExpiresDate = expiresDate;
	}

	public float getQuantity() {
		return mQuantity;
	}

	public void setQuantity(float quantity) {
		if (quantity >= 0) {
			// mQuantity MUST be non-negative
			this.mQuantity = quantity;
		}
	}

	public String getUnit() {
		return mUnit;
	}

	public void setUnit(String unit) {
		this.mUnit = unit;
	}

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		this.mType = type;
	}

	public String getInventory() {
		return mInventory;
	}

	public void setInventory(String inventory) {
		this.mInventory = inventory;
	}

	public boolean isInGroceryList() {
		return mInGroceryList;
	}

	public void setInGroceryList(boolean inGroceryList) {
		this.mInGroceryList = inGroceryList;
	}

	public int getDaysUntilExpiration() {
		return mDaysUntilExpiration;
	}

	public void setDaysUntilExpiration(int daysUntilExpiration) {
		this.mDaysUntilExpiration = daysUntilExpiration;
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
		parcel.writeLong(mRowID);
		parcel.writeString(mName);
		parcel.writeString(mCreatedDate);
		parcel.writeString(mExpiresDate);
		parcel.writeFloat(mQuantity);
		parcel.writeString(mUnit);
		parcel.writeString(mType);
		parcel.writeString(mInventory);
		parcel.writeByte((byte) (mInGroceryList ? 1 : 0));
		parcel.writeInt(mDaysUntilExpiration);
	}
}
