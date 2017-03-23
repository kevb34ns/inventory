package com.kevinkyang.inventory;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.drawer_list_group, parent);

		TextView groupTitle =
				(TextView) view.findViewById(R.id.drawer_group_textview);
		ImageButton button =
				(ImageButton) view.findViewById(R.id.button_expand_collapse);
		button.setFocusable(false);

		return new GroupViewHolder(view, groupTitle, button);
	}

	public ChildViewHolder createChildViewHolder(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.drawer_list_child, parent);

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

	public void bindGroupViewHolder(GroupViewHolder holder, final int listPosition) {
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
						adapter.collapseGroup(listPosition);
					} else {
						adapter.expandGroup(listPosition);
					}
				}
			});
		}
	}

	public void bindChildViewHolder(ChildViewHolder holder, int listPosition) {

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

		DrawerItem item = internalList.get(listPosition);
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
		//TODO do this shit!!!
	}

	public void collapseGroup(int listPosition) {
		if (listPosition < 0 || listPosition >= groups.size()) {
			throw new IndexOutOfBoundsException();
		}
		//TODO do this shit!!!
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
