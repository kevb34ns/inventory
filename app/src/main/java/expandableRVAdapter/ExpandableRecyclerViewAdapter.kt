package expandableRVAdapter

import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.ViewGroup

abstract class ExpandableRecyclerViewAdapter
        <GroupItem : GroupItemBase,
        ChildItem : ChildItemBase,
        GroupViewHolder : ExpandableViewHolder,
        ChildViewHolder : ExpandableViewHolder>
        (private var groups: ArrayList<GroupItem>,
         private var children: ArrayList<ArrayList<ChildItem>>)
        : Adapter<ViewHolder>() {

    private val VIEWTYPE_GROUP = 0
    private val VIEWTYPE_CHILD = 1
    private var adapterList = ArrayList<ItemBase>()
    var listener: OnItemClickListener? = null

    init {
        for (i in groups.indices) {
            groups[i].groupPosition = i
            adapterList.add(groups[i])
            for (j in children[i].indices) {
                children[i][j].groupPosition = i
                children[i][j].childPosition = j
                if (groups[i].expanded) {
                    adapterList.add(children[i][j])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        if (viewType == VIEWTYPE_GROUP) {
            val vh = createGroupViewHolder(parent)
            vh.viewHolderListener = object : ExpandableViewHolder.ViewHolderListener {
                override fun onClick(position: Int) {
                    if (adapterList[position] !is GroupItemBase) {
                        return
                    }

                    val item = adapterList[position] as GroupItemBase
                    listener?.onGroupClick(item.groupPosition)
                }
            }

            return vh
        } else if (viewType == VIEWTYPE_CHILD) {
            val vh = createChildViewHolder(parent)
            vh.viewHolderListener = object : ExpandableViewHolder.ViewHolderListener {
                override fun onClick(position: Int) {
                    if (adapterList[position] !is ChildItemBase) {
                        return
                    }

                    val item = adapterList[position] as ChildItemBase
                    listener?.onChildClick(
                            item.groupPosition,
                            item.childPosition)
                }
            }
            return vh
        } else {
            return null
        }
    }

    abstract fun createGroupViewHolder(parent: ViewGroup?) : GroupViewHolder

    abstract fun createChildViewHolder(parent: ViewGroup?) : ChildViewHolder

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        if (holder == null) {
            return
        }

        if (getItemViewType(position) == VIEWTYPE_GROUP) {
            bindGroupViewHolder(holder as GroupViewHolder,
                    adapterList[position] as GroupItem)
        } else if (getItemViewType(position) == VIEWTYPE_CHILD) {
            bindChildViewHolder(holder as ChildViewHolder,
                    adapterList[position] as ChildItem)
        }
    }

    abstract fun bindGroupViewHolder(holder: GroupViewHolder, item: GroupItem)

    abstract fun bindChildViewHolder(holder: ChildViewHolder, item: ChildItem)

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

    fun addGroup(newGroup: GroupItem, childList: ArrayList<ChildItem>,
                 position: Int = -1): Boolean {

        if (position < -1 || position > groups.size ||
                groups.indexOf(newGroup) == -1) {
            return false
        }

        var pos = position
        if (pos == -1) {
            pos = groups.size
        }

        newGroup.groupPosition = pos
        for (i in childList.indices) {
            childList[i].groupPosition = pos
            childList[i].childPosition = i
        }

        groups.add(pos, newGroup)
        children.add(pos, childList)
        for (i in pos + 1 until groups.size) {
            groups[i].groupPosition = i
        }

        //TODO add to the adapter list and notify, and add children if expanded, possibly write new method to find a group in the adapter list (have you done this before somewhere else?)

        return true
    }

    fun addChild(newChild: ChildItem, groupPosition: Int,
                 childPosition: Int = -1): Boolean {

        if (groupPosition !in groups.indices ||
                childPosition > children[groupPosition].size ||
                childPosition < -1) {
            return false
        }

        var childPos = childPosition
        if (childPos == -1) {
            childPos = children[groupPosition].size
        }

        newChild.groupPosition = groupPosition
        newChild.childPosition = childPosition

        children[groupPosition].add(childPos, newChild)
        for (i in childPos + 1 until children[groupPosition].size) {
            children[groupPosition][i].childPosition = i
        }

        // TODO add to adapter list and notify if group expanded

        return true
    }

    fun removeGroup(groupItem: GroupItem): Boolean {
        for (i in groups.indices) {
            if (groups[i] == groupItem) {
                groups.removeAt(i)
                children.removeAt(i)
                for (j in i until groups.size) {
                    groups[j].groupPosition = j
                    for (k in children[j].indices) {
                        children[j][k].groupPosition = j
                    }
                }

                return true
            }
        }
        //TODO remove from adapter list and notify

        return false
    }

    fun removeChild(groupItem: GroupItem, childItem: ChildItem): Boolean {
        for (i in groups.indices) {
            if (groups[i] == groupItem) {
                for (j in children[i].indices) {
                    if (children[i][j] == childItem) {
                        children[i].removeAt(j)
                        for (k in j until children[i].size) {
                            children[i][k].childPosition = k
                        }

                        return true
                    }
                }
            }
        }

        // TODO remove from adapter list and notify if expanded

        return false
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

            val index = adapterList.indexOf(groups[position])
            if (index != -1) {
                for (i in children[position].indices) {
                    adapterList.add(index + i + 1, children[position][i])
                    notifyItemInserted(index + i + 1)
                }
            }
        }
    }

    fun collapseGroup(position: Int) {
        if (position !in groups.indices) {
            throw IndexOutOfBoundsException()
        }

        if (groups[position].expanded) {
            groups[position].expanded = false

            val index = adapterList.indexOf(groups[position])
            if (index != -1) {
                for (i in children[position].indices) {
                    if (adapterList[index + i + 1] is ChildItemBase) {
                        adapterList.removeAt(index + i + 1)
                        notifyItemRemoved(index + i + 1)
                    } else {
                        return
                    }
                }
            }
        }
    }

    internal interface ItemBase

    interface OnItemClickListener {

        fun onGroupClick(groupPosition: Int): Boolean

        fun onChildClick(groupPosition: Int, childPosition: Int): Boolean
    }

    //TODO may need to override parent class notify() methods and others in order to prevent user from calling the parent versions and fucking shit up
}
