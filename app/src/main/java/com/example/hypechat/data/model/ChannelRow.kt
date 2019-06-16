package com.example.hypechat.data.model

import android.view.View
import com.example.hypechat.R
import com.example.hypechat.data.model.rest.response.ChannelResponse
import com.example.hypechat.data.rest.utils.ChannelVisibility
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.channel_row.view.*

class ChannelRow(val channel: ChannelResponse): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.channel_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.channelNameRowTextView.text = channel.name

         when (channel.visibility){
             ChannelVisibility.PUBLIC.visibility -> viewHolder.itemView.channelVisibilityRowImageView.visibility = View.INVISIBLE
             ChannelVisibility.PRIVATE.visibility -> viewHolder.itemView.channelVisibilityRowImageView.setImageResource(R.drawable.ic_lock_black_24dp)
        }
    }

}