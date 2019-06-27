package com.example.hypechat.data.model

import com.example.hypechat.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.team_stat_row.view.*

class TeamStatRow(val teamName: String, val messages: Int): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.team_stat_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.teamsStatTextView.text = teamName

        viewHolder.itemView.messageStatTextView.text = messages.toString()
    }
}