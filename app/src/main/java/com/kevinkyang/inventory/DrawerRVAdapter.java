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

import java.io.IOException;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Map;

import static com.kevinkyang.inventory.R.layout.item;

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

	private TypedArray colorArray;

	public DrawerRVAdapter(Context context, ArrayList<String> groups,
						   Map<String, ArrayList<String>> groupToChildrenMap) {
		this.context = context;
		this.groups = groups;
		this.groupToChildrenMap = groupToChildrenMap;
		initInternalList(groups);
		drawerOnClickListener = null;
		colorArray = context.getResources()
				.obtainTypedArray(R.array.array_inventory_colors);
	}

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

	public GroupViewHolder createGroupViewHolder(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(
				R.layout.drawer_list_group, parent, false);

		TextView groupTitle =
				(TextView) view.findViewById(R.id.drawer_group_textview);
		ImageButton button =
				(ImageButton) view.findViewById(R.id.button_expand_collapse);
		button.setFocusable(false);

		return new GroupViewHolder(view, groupTitle, button);
	}

	public ChildViewHolder createChildViewHolder(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(
				R.layout.drawer_list_child, parent, false);

		TextView childTitle =
				(TextView) view.findViewById(R.id.drawer_child_textview);
		ImageView colorTag =
				(ImageView) view.findViewById(R.id.drawer_child_color_tag);
		TextView itemCountLabel =
				(TextView) view.findViewById(R.id.label_item_count);

		return new ChildViewHolder(view, childTitle, colorTag, itemCountLabel);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder,
								 int position) {
		if (holder instanceof GroupViewHolder) {
			bindGroupViewHolder((GroupViewHolder)holder, position);
		} else if (holder instanceof ChildViewHolder) {
			bindChildViewHolder((ChildViewHolder) holder, position);
		}
	}

	public void bindGroupViewHolder(final GroupViewHolder holder, final int listPosition) {
		GroupItem item = (GroupItem) internalList.get(listPosition);

		holder.groupTitle.setText(item.getName());
		if (groupToChildrenMap.get(item.getName()).isEmpty()) {
			holder.button.setVisibility(View.INVISIBLE);
		} else {
			holder.button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DrawerRVAdapter adapter = DrawerRVAdapter.this;
					if (adapter.isGroupExpanded(listPosition)) {
						collapseAnimation(holder.button);
						adapter.collapseGroup(listPosition);
					} else {
						expandAnimation(holder.button);
						adapter.expandGroup(listPosition);
					}
				}
			});
		}
	}

	public void bindChildViewHolder(ChildViewHolder holder, int listPosition) {
		ChildItem item = (ChildItem) internalList.get(listPosition);

		holder.childTitle.setText(item.getName());
		if (item.getGroupPosition() == 0) {
			bindSubInventories(holder, item);
		}
	}

	private void bindSubInventories(ChildViewHolder holder, ChildItem item) {
		ArrayList<String> children =
				groupToChildrenMap.get(groups.get(item.getGroupPosition()));
		if (item.getChildPosition() != children.size() - 1) {
			holder.colorTag.setImageDrawable(null);
			holder.colorTag.getLayoutParams().width = 6;
			holder.colorTag.setBackgroundColor(
					colorArray.getColor(item.getChildPosition(), 0));
			int count = ItemData.getInstance()
					.getInventoryItemCount(item.getName());
			holder.itemCountLabel.setVisibility(View.VISIBLE);
			if (count < 100) {
				holder.itemCountLabel.setText(Integer.toString(count));
			} else {
				holder.itemCountLabel.setText("99+");
			}

		} else {
			Drawable addIcon = context.getResources()
					.getDrawable(R.drawable.ic_add, null);
			addIcon = addIcon.getConstantState().newDrawable().mutate();
			addIcon.setColorFilter(
					new PorterDuffColorFilter(
							context.getColor(
									android.R.color.primary_text_light),
									PorterDuff.Mode.MULTIPLY));
			holder.colorTag.setBackgroundColor(Color.TRANSPARENT);
			holder.colorTag.getLayoutParams().width = 40;
			holder.colorTag.setImageDrawable(addIcon);

			holder.itemCountLabel.setVisibility(View.INVISIBLE);
		}
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

	private class ChildItem implements DrawerItem {
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

	private void expandAnimation(View view) {
		Animation expandAnimation =
				AnimationUtils.loadAnimation(context,
						R.anim.expand_rotation);
		expandAnimation.setFillAfter(true);
		view.startAnimation(expandAnimation);
	}

	private void collapseAnimation(View view) {
		Animation collapseAnimation =
				AnimationUtils.loadAnimation(context,
						R.anim.collapse_rotation);
		collapseAnimation.setFillAfter(true);
		view.startAnimation(collapseAnimation);
	}
}
