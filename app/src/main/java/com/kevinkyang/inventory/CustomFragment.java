package com.kevinkyang.inventory;

/**
 * Created by Kevin on 3/1/2017.
 */

public interface CustomFragment {
	public MainActivity getParent();

	public void refresh();

	public void itemAdded(Item item);

	public void itemSaved(int position);
}
