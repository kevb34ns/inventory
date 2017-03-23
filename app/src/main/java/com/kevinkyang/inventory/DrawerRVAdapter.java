package com.kevinkyang.inventory;

import android.content.Context;
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

	private Context context;

	private ArrayList<String> groups;
	private Map<String, ArrayList<String>> groupToChildrenMap;
	/**
	 * List that represents currently visible drawer items.
	 * Completely inaccessible to clients.
	 */
	private ArrayList<DrawerItem> internalList;

	private DrawerOnClickListener drawerOnClickListener;

	public DrawerRVAdapter(Context context, ArrayList<String> groups,
						   Map<String, ArrayList<String>> groupToChildrenMap) {
		this.context = context;
		this.groups = groups;
		this.groupToChildrenMap = groupToChildrenMap;
		drawerOnClickListener = null;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
													  int viewType) {
		if (viewType == VIEWTYPE_GROUP) {
			return createGroupViewHolder(parent);
		} else if (viewType == VIEWTYPE_CHILD) {
			return createChildViewHolder(parent);
		} else {
			return null;
		}
	}

	public GroupViewHolder createGroupViewHolder(ViewGroup parent) {
		return null;
	}

	public ChildViewHolder createChildViewHolder(ViewGroup parent) {
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder,
								 int position) {

	}

	@Override
	public int getItemCount() {
		return internalList.size();
	}

	@Override
	public int getItemViewType(int position) {
		DrawerItem item = internalList.get(position);
		if (item instanceof GroupItem) {
			return VIEWTYPE_GROUP;
		} else if (item instanceof ChildItem) {
			return VIEWTYPE_CHILD;
		} else {
			return -1;
		}
	}

	public boolean isGroupExpanded(int groupPosition) {
		return false;
	}

	public void expandGroup(int groupPosition) {
		if (groupPosition < 0 || groupPosition >= groups.size()) {
			return;
		}
	}

	public void collapseGroup(int groupPosition) {
		if (groupPosition < 0 || groupPosition >= groups.size()) {
			return;
		}
	}

	public class GroupViewHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener{
		private DrawerOnClickListener listener;

		public View itemView;
		public TextView groupTitle;
		public ImageButton button;

		public GroupViewHolder(View itemView, TextView groupTitle, ImageButton button) {
			super(itemView);
			this.itemView = itemView;
			this.groupTitle = groupTitle;
			this.button = button;

			listener = DrawerRVAdapter.this.getDrawerOnClickListener();
		}

		@Override
		public void onClick(View v) {
			if (listener != null) {
				listener.onGroupClick();
			}
		}
	}

	public class ChildViewHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener{
		private DrawerOnClickListener listener;

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

			listener = DrawerRVAdapter.this.getDrawerOnClickListener();
		}

		@Override
		public void onClick(View v) {
			if (listener != null) {
				listener.onChildClick();
			}
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

	public interface DrawerOnClickListener {
		//TODO these methods need useful parameters like position, itemView
		public boolean onGroupClick();

		public boolean onChildClick();
	}

	public DrawerOnClickListener getDrawerOnClickListener() {
		return drawerOnClickListener;
	}

	public void setDrawerOnClickListener(DrawerOnClickListener drawerOnClickListener) {
		this.drawerOnClickListener = drawerOnClickListener;
	}
}
