package expandableRVAdapter

abstract class GroupItemBase(var expanded: Boolean = false)
    : ExpandableRecyclerViewAdapter.ItemBase {

    var groupPosition: Int = -1
}