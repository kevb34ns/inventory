package com.kevinkyang.inventory;

public interface CustomFragment {
	public MainActivity getParent();

	public void refresh();

	public void itemAdded(Item item);

	public void itemSaved(int position);

	public void removeItem(Item item, int position);

	public void swapList(Item item, int position);

	public void undoDelete(Item item, Integer position);

	public void undoSwapList(Item item, Integer position);
}
