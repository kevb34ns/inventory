package com.kevinkyang.inventory;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Kevin on 3/20/2017.
 */

public class DrawerRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	public static final int VIEWTYPE_GROUP = 0;
	public static final int VIEWTYPE_CHILD = 1;

	private ArrayList<String> groups;
	private Map<String, ArrayList<String>> groupToChildrenMap;
	/**
	 * List that represents currently visible drawer items.
	 * Completely inaccessible to clients.
	 */
	private ArrayList<DrawerItem> internalList;

	public DrawerRVAdapter(ArrayList<String> groups, Map<String, ArrayList<String>> groupToChildrenMap) {
		this.groups = groups;
		this.groupToChildrenMap = groupToChildrenMap;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

	}

	@Override
	public int getItemCount() {
		return 0;
	}

	public static class GroupViewHolder extends RecyclerView.ViewHolder {
		public View itemView;
		public TextView groupTitle;
		public ImageButton button;

		public GroupViewHolder(View itemView, TextView groupTitle, ImageButton button) {
			super(itemView);
			this.itemView = itemView;
			this.groupTitle = groupTitle;
			this.button = button;
		}
	}

	public static class ChildViewHolder extends RecyclerView.ViewHolder {
		public View itemView;
		public TextView childTitle;
		public ImageView colorTag;
		public TextView itemCountLabel;

		public ChildViewHolder(View itemView, TextView childTitle, ImageView colorTag, TextView itemCountLabel) {
			super(itemView);
			this.itemView = itemView;
			this.childTitle = childTitle;
			this.colorTag = colorTag;
			this.itemCountLabel = itemCountLabel;
		}
	}

	private interface DrawerItem {
		public String getName();
	}

	private class GroupItem implements DrawerItem {
		private String name;

		public GroupItem(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}

	private class ChildItem implements DrawerItem {
		private String name;

		public ChildItem(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
