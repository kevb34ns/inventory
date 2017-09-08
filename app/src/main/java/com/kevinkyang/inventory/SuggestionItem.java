package com.kevinkyang.inventory;

public class SuggestionItem {
	private String mName;
	private String mType;
	private String mDefaultExpiration;
	private String mDefaultUnit;

	public SuggestionItem() {

	}

	public SuggestionItem(String name, String type,
						  String defaultExpiration,
						  String defaultUnit) {
		mName = name;
		mType = type;
		mDefaultExpiration = defaultExpiration;
		mDefaultUnit = defaultUnit;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		this.mType = type;
	}

	public String getDefaultExpiration() {
		return mDefaultExpiration;
	}

	public void setDefaultExpiration(String defaultExpiration) {
		this.mDefaultExpiration = defaultExpiration;
	}

	public String getDefaultUnit() {
		return mDefaultUnit;
	}

	public void setDefaultUnit(String defaultUnit) {
		this.mDefaultUnit = defaultUnit;
	}

	@Override
	public String toString() {
		return getName();
	}
}
