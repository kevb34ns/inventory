package com.kevinkyang.inventory;

public class SuggestionItem {
	private String name;
	private String type;
	private String defaultExpiration;
	private String defaultUnit;

	public SuggestionItem() {

	}

	public SuggestionItem(String name, String type,
						  String defaultExpiration,
						  String defaultUnit) {
		this.name = name;
		this.type = type;
		this.defaultExpiration = defaultExpiration;
		this.defaultUnit = defaultUnit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefaultExpiration() {
		return defaultExpiration;
	}

	public void setDefaultExpiration(String defaultExpiration) {
		this.defaultExpiration = defaultExpiration;
	}

	public String getDefaultUnit() {
		return defaultUnit;
	}

	public void setDefaultUnit(String defaultUnit) {
		this.defaultUnit = defaultUnit;
	}

	@Override
	public String toString() {
		return getName();
	}
}
