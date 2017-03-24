package com.kevinkyang.inventory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

/**
 * Created by Kevin on 3/21/2017.
 */

public class ListItemTouchHelperCallback extends ItemTouchHelper.Callback {
	private Context context;
	private ListItemTouchHelperListener listener;
	private boolean groceryMode;

	public ListItemTouchHelperCallback(Context context,
									   ListItemTouchHelperListener listener,
									   boolean groceryMode) {
		this.context = context;
		this.listener = listener;
		this.groceryMode = groceryMode;
	}

	@Override
	public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
		int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
		return makeMovementFlags(dragFlags, swipeFlags);
	}

	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
		return false;
	}

	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
		int position = viewHolder.getAdapterPosition();
		if (direction == ItemTouchHelper.START) {
			listener.onSwapList(position);
		} else if (direction == ItemTouchHelper.END) {
			listener.onDelete(position);
		}
	}

	@Override
	public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
			View itemView = viewHolder.itemView;
			float height = (float) itemView.getBottom() -
					(float) itemView.getTop();
			float width = height / 3;

			if (dX > 0) {
				Paint paint = new Paint();
				paint.setColor(Color.parseColor("#FF0000")); // TODO color resource
				Bitmap icon = BitmapFactory.decodeResource(
						context.getResources(),
						R.drawable.ic_delete);

				RectF background =
						new RectF(itemView.getLeft(), itemView.getTop(),
								dX, itemView.getBottom());
				c.drawRect(background, paint);

				RectF icon_loc =
						new RectF((float) itemView.getLeft() + width,
								(float) itemView.getTop() + width,
								(float) itemView.getLeft() + 2 * width,
								(float) itemView.getBottom() - width);
				c.drawBitmap(icon, null, icon_loc, paint);
			} else if (dX < 0){
				Paint paint = new Paint();
				paint.setColor(context.getColor(R.color.colorGreen));

				int drawableResource = groceryMode ?
						R.drawable.ic_playlist_add :
						R.drawable.ic_local_grocery_store;
				Bitmap icon = BitmapFactory.decodeResource(
						context.getResources(),
						drawableResource);

				RectF background =
						new RectF((float)itemView.getRight() + dX,
								itemView.getTop(),
								itemView.getRight(),
								itemView.getBottom());
				c.drawRect(background, paint);

				// TODO can make this rectf slightly bigger if groceryMode to make add icon look same size as the other icons
				RectF icon_loc =
						new RectF((float) itemView.getRight() - 2 *	 width,
								(float) itemView.getTop() + width,
								(float) itemView.getRight() - width,
								(float) itemView.getBottom() - width);
				c.drawBitmap(icon, null, icon_loc, paint);
			}
		}
		super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
	}

	@Override
	public boolean isItemViewSwipeEnabled() {
		return true;
	}

	@Override
	public boolean isLongPressDragEnabled() {
		return false;
	}

	public static interface ListItemTouchHelperListener {
		public void onDelete(int position);

		public void onSwapList(int position);
	}

}
