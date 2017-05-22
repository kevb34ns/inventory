package expandableRVAdapter

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class ExpandableViewHolder(itemView: View)
    : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    var viewHolderListener: ViewHolderListener? = null

    init {
        itemView.setOnClickListener(this)
    }

    interface ViewHolderListener {
        fun onClick(position: Int)
    }

    override fun onClick(v: View?) {
        viewHolderListener?.onClick(adapterPosition)
    }
}