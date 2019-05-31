package com.example.hypechat.data.model

import com.example.hypechat.R
import com.example.hypechat.data.model.rest.response.TeamResponse
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.team_row.view.*

class TeamRow (val team: TeamResponse): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.team_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.teamRowLocationTextView.text = team.location

        viewHolder.itemView.teamNameRowTextView.text = team.team_name
        Picasso.get().load(team.picture)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.profile_placeholder)
            .into(viewHolder.itemView.teamRowImageView)
    }
}