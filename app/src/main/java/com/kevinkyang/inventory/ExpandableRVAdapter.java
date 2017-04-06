package com.kevinkyang.inventory;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by kevinyang on 4/6/17.
 */

@Retention(RetentionPolicy.SOURCE)
@interface Needed { //TODO get rid of this later
	boolean makeAbstract() default false;
	boolean makeInterface() default false;
	boolean needChanges() default false;
}


public abstract class ExpandableRVAdapter
		extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	public static final int VIEWTYPE_GROUP = 0;
	public static final int VIEWTYPE_CHILD = 1;

	private ArrayList<String> groups;
	private Map<String, ArrayList<String>> groupToChildrenMap;
	/**
	 * List that represents currently visible drawer items.
	 */
	private ArrayList<ListItem> internalList;

	public ExpandableRVAdapter(@NonNull ArrayList<String> groups,
	                           @NonNull Map<String, ArrayList<String>>
			                           groupToChildrenMap) {
		this.groups = groups;
		this.groupToChildrenMap = groupToChildrenMap;
		initInternalList(groups);
	}

	@Needed(needChanges = true) //TODO might want to init with some groups expanded? idk
	private void initInternalList(ArrayList<String> groups) {
		internalList = new ArrayList<>();

		for (int pos = 0; pos < groups.size(); pos++) {
			internalList.add(new GroupItem(groups.get(pos), pos));
		}
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

	public abstract GroupViewHolder createGroupViewHolder(ViewGroup parent);

	public abstract ChildViewHolder createChildViewHolder(ViewGroup parent);

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder,
	                             int position) {
		if (holder instanceof GroupViewHolder) {
			bindGroupViewHolder((GroupViewHolder) holder, position);
		} else if (holder instanceof ChildViewHolder) {
			bindChildViewHolder((ChildViewHolder) holder, position);
		}
	}

	public abstract void bindGroupViewHolder(final GroupViewHolder holder, final int listPosition);

	public abstract void bindChildViewHolder(
			ChildViewHolder holder, int listPosition);

	@Override
	public int getItemCount() {
		return internalList.size();
	}

	public int getGroupCount() {
		return groups.size();
	}

	public int getChildrenCount(int groupPosition) {
		if (groupPosition < 0 || groupPosition >= groups.size()) {
			throw new IndexOutOfBoundsException();
		}

		ArrayList<String> children =
				groupToChildrenMap.get(groups.get(groupPosition));
		return children.size();
	}

	@Override
	public int getItemViewType(int position) {
		ListItem item = internalList.get(position);
		if (item instanceof GroupItem) {
			return VIEWTYPE_GROUP;
		} else if (item instanceof ChildItem) {
			return VIEWTYPE_CHILD;
		} else {
			return -1;
		}
	}

	public ListItem getInternalListItem(int listPosition) {
		return internalList.get(listPosition);
	}

	public String getGroup(int groupPosition) {
		if (groupPosition < 0 || groupPosition >= groups.size()) {
			throw new IndexOutOfBoundsException();
		}
		return groups.get(groupPosition);
	}

	public String getChild(int groupPosition, int childPosition) {
		if (groupPosition < 0 || groupPosition >= groups.size()) {
			throw new IndexOutOfBoundsException();
		}

		ArrayList<String> children =
				groupToChildrenMap.get(groups.get(groupPosition));
		if (childPosition < 0 || childPosition >= children.size()) {
			throw new IndexOutOfBoundsException();
		}

		return children.get(childPosition);
	}

	public boolean isGroupExpanded(int listPosition) {
		if (listPosition < 0 || listPosition >= internalList.size()) {
			throw new IndexOutOfBoundsException();
		}

		GroupItem item = (GroupItem) internalList.get(listPosition);
		if (groupToChildrenMap.get(item.getName()) != null &&
				!groupToChildrenMap.get(item.getName()).isEmpty() &&
				listPosition < internalList.size() - 1 &&
				internalList.get(listPosition + 1) instanceof ChildItem) {
			return true;
		} else {
			return false;
		}
	}

	public void expandGroup(int listPosition) {
		if (listPosition < 0 || listPosition >= groups.size()) {
			throw new IndexOutOfBoundsException();
		}

		if (isGroupExpanded(listPosition)) {
			return;
		}

		GroupItem groupItem = (GroupItem) internalList.get(listPosition);
		ArrayList<String> children =
				groupToChildrenMap.get(groupItem.getName());
		for (int pos = 0; pos < children.size(); pos++) {
			internalList.add(listPosition + pos + 1,
					new ChildItem(children.get(pos), groupItem.getGroupPosition(), pos));
			notifyItemInserted(listPosition + 1);
		}
	}

	public void collapseGroup(int listPosition) {
		if (listPosition < 0 || listPosition >= groups.size()) {
			throw new IndexOutOfBoundsException();
		}

		if (!isGroupExpanded(listPosition)) {
			return;
		}

		GroupItem groupItem = (GroupItem) internalList.get(listPosition);
		ArrayList<String> children =
				groupToChildrenMap.get(groupItem.getName());
		for (int i = 0; i < children.size(); i++) {
			internalList.remove(listPosition + 1);
			notifyItemRemoved(listPosition + 1);
		}
	}

	public abstract class GroupViewHolder extends RecyclerView.ViewHolder {
		protected GroupViewHolder(View itemView) {
			super(itemView);
		}
	}

	public abstract class ChildViewHolder extends RecyclerView.ViewHolder {
		protected ChildViewHolder(View itemView) {
			super(itemView);
		}
	}

	private interface ListItem {
		public String getName();
	}

	@Needed(makeAbstract = true) //TODO this and childItem may not need to be abstract if they can just remain private(never used by child classes), but you may not be able to since bindgroupviewholder in drawerrvadapter uses groupitem
	private class GroupItem implements ListItem {
		private String name;
		private int groupPosition;

		public GroupItem(String name, int groupPosition) {
			this.name = name;
			this.groupPosition = groupPosition;
		}

		@Override
		public String getName() {
			return name;
		}

		public int getGroupPosition() {
			return groupPosition;
		}

		public void setGroupPosition(int groupPosition) {
			this.groupPosition = groupPosition;
		}
	}

	@Needed(makeAbstract = true)
	private class ChildItem implements ListItem {
		private String name;
		private int groupPosition;
		private int childPosition;

		public ChildItem(String name, int groupPosition, int childPosition) {
			this.name = name;
			this.groupPosition = groupPosition;
			this.childPosition = childPosition;
		}

		@Override
		public String getName() {
			return name;
		}

		public int getGroupPosition() {
			return groupPosition;
		}

		public void setGroupPosition(int groupPosition) {
			this.groupPosition = groupPosition;
		}

		public int getChildPosition() {
			return childPosition;
		}

		public void setChildPosition(int childPosition) {
			this.childPosition = childPosition;
		}
	}
}
