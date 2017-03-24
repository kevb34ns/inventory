package com.kevinkyang.inventory;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Kevin on 3/2/2017.
 */

public class DrawerAdapter extends BaseExpandableListAdapter {
	private Context context;
	private ArrayList<String> titles;
	private Map<String, ArrayList<String>> childrenMap;
	private ExpandableListView parentListView;

	private TypedArray colorArray;

	public DrawerAdapter(Context context,
						 ArrayList<String> titles,
						 Map<String, ArrayList<String>> childrenMap,
						 ExpandableListView parentListView) {
		this.context = context;
		this.titles = titles;
		this.childrenMap = childrenMap;
		this.parentListView = parentListView;

		colorArray = context.getResources()
				.obtainTypedArray(R.array.array_inventory_colors);
	}

	@Override
	public int getGroupCount() {
		return titles.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return childrenMap.get(titles.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return titles.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childrenMap
				.get(titles.get(groupPosition))
				.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		final String titleText = (String) getGroup(groupPosition);

		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.drawer_list_group, null);
		}

		TextView childTextView = (TextView) convertView.findViewById(R.id.drawer_group_textview);
		childTextView.setText(titleText);

		final ImageButton button = (ImageButton) convertView.findViewById(R.id.button_expand_collapse);
		button.setFocusable(false);
		if (childrenMap.get(titles.get(groupPosition)).isEmpty()) {
			button.setVisibility(View.INVISIBLE);
		} else {
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (parentListView.isGroupExpanded(groupPosition)) {
						parentListView.collapseGroup(groupPosition);
					} else {
						parentListView.expandGroup(groupPosition);
					}
				}
			});
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		final String titleText = (String) getChild(groupPosition, childPosition);

		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.drawer_list_child, null);
		}

		TextView childTextView = (TextView) convertView.findViewById(R.id.drawer_child_textview);
		childTextView.setText(titleText);

		ImageView colorTag = (ImageView) convertView.findViewById(R.id.drawer_child_color_tag);
		TextView itemCountLabel = (TextView) convertView.findViewById(R.id.label_item_count);
		if (childPosition != getChildrenCount(groupPosition) - 1) {
			colorTag.setBackgroundColor(colorArray.getColor(childPosition, 0));
			int count = ItemData.getInstance().getInventoryItemCount(titleText);
			if (count < 100) {
				itemCountLabel.setText(Integer.toString(count));
			} else {
				itemCountLabel.setText("99+");
			}
		} else {
			Drawable addIcon = context.getResources().getDrawable(R.drawable.ic_add, null);
			addIcon = addIcon.getConstantState().newDrawable().mutate();
			addIcon.setColorFilter(new PorterDuffColorFilter(context.getColor(android.R.color.primary_text_light), PorterDuff.Mode.MULTIPLY));
			colorTag.getLayoutParams().width = 40;
			colorTag.setImageDrawable(addIcon);

			itemCountLabel.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
