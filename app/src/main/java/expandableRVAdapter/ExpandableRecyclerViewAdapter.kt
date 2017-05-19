package expandableRVAdapter

import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.ViewGroup

abstract class ExpandableRecyclerViewAdapter
        <GroupItem : GroupItemBase,
        ChildItem : ChildItemBase>
        (var groups: ArrayList<GroupItem>,
         var children: ArrayList<ArrayList<ChildItem>>)
        : Adapter<ViewHolder>() {

    private val VIEWTYPE_GROUP = 0
    private val VIEWTYPE_CHILD = 1
    private var adapterList = ArrayList<ItemBase>()
    private var listener: OnClickListener? = null

    init {
        for (i in groups.indices) {
            adapterList.add(groups[i])
            if (groups[i].expanded) {
                for (j in children[i].indices) {
                    adapterList.add(children[i][j])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        if (viewType == VIEWTYPE_GROUP) {
            return createGroupViewHolder(parent)
        } else if (viewType == VIEWTYPE_CHILD) {
            return createChildViewHolder(parent)
        } else {
            return null
        }
    }

    abstract fun createGroupViewHolder(parent: ViewGroup?) : ViewHolder

    abstract fun createChildViewHolder(parent: ViewGroup?) : ViewHolder

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        if (getItemViewType(position) == VIEWTYPE_GROUP) {
            bindGroupViewHolder(holder, adapterList[position] as GroupItem)
        } else if (getItemViewType(position) == VIEWTYPE_CHILD) {
            bindChildViewHolder(holder, adapterList[position] as ChildItem)
        }
    }

    abstract fun bindGroupViewHolder(holder: ViewHolder?, item: GroupItem)

    abstract fun bindChildViewHolder(holder: ViewHolder?, item: ChildItem)

    override fun getItemCount(): Int {
        return adapterList.size
    }

    fun getGroupCount(): Int {
        return groups.size
    }

    fun getChildrenCount(groupPosition: Int): Int {
        if (groupPosition !in 0 until getGroupCount()) {
            throw IndexOutOfBoundsException()
        }

        return children[groupPosition].size
    }

    fun getGroup(groupPosition: Int): GroupItem {
        if (groupPosition !in groups.indices) {
            throw IndexOutOfBoundsException()
        }

        return groups[groupPosition]
    }

    fun getChild(groupPosition: Int, childPosition: Int): ChildItem {
        if (groupPosition !in groups.indices ||
                childPosition !in children[groupPosition].indices) {

            throw IndexOutOfBoundsException()
        }

        return children[groupPosition][childPosition]
    }

    override fun getItemViewType(position: Int): Int {
        if (adapterList[position] is GroupItemBase) {
            return VIEWTYPE_GROUP
        } else if (adapterList[position] is ChildItemBase) {
            return VIEWTYPE_CHILD
        } else {
            return -1
        }
    }

    fun isGroupExpanded(position: Int): Boolean {
        if (position !in groups.indices) {
            throw IndexOutOfBoundsException()
        }

        return groups[position].expanded
    }

    fun expandGroup(position: Int) {
        if (position !in groups.indices) {
            throw IndexOutOfBoundsException()
        }

        if (!groups[position].expanded) {
            groups[position].expanded = true
            //TODO
        }
    }

    fun collapseGroup(position: Int) {
        if (position !in groups.indices) {
            throw IndexOutOfBoundsException()
        }

        if (groups[position].expanded) {
            groups[position].expanded = false
            //TODO
        }
    }

    internal interface ItemBase

    private interface OnClickListener {

        fun onGroupClick(groupPosition: Int): Boolean

        fun onChildClick(groupPosition: Int, childPosition: Int): Boolean
    }

    // TODO get viewholder clicklistener working: have a listener in this class, which is passed to the viewholder, which is called when clicked, and the viewholder passes adapterPosition to this class, which can then call any listeners the user has set; this would require you to once again make GroupViewHolder and ChildViewHolder classes
}
