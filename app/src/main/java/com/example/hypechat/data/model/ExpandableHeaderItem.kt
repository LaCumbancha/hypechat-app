package com.example.hypechat.data.model

import com.example.hypechat.R
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_expandable_header.view.*

class ExpandableHeaderItem(private val title: String): Item<ViewHolder>(), ExpandableItem {

    private lateinit var expandableGroup: ExpandableGroup

    override fun getLayout(): Int {
        return R.layout.item_expandable_header
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.item_expandable_header_title.text = title
        viewHolder.itemView.item_expandable_header_icon.setImageResource(getRotatedIconResId())

        viewHolder.itemView.item_expandable_header_root.setOnClickListener {
            expandableGroup.onToggleExpanded()
            viewHolder.itemView.item_expandable_header_icon.setImageResource(getRotatedIconResId())
        }
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {

        expandableGroup = onToggleListener
    }

    private fun getRotatedIconResId() =
        if (expandableGroup.isExpanded)
            R.drawable.ic_keyboard_arrow_up_black_24dp
        else
            R.drawable.ic_keyboard_arrow_down_black_24dp
}