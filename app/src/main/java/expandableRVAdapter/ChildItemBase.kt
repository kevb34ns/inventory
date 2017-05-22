package expandableRVAdapter

abstract class ChildItemBase : ExpandableRecyclerViewAdapter.ItemBase {

    var groupPosition: Int = -1
    var childPosition: Int = -1
}