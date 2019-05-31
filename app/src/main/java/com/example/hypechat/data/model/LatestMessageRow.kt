package com.example.hypechat.data.model

import com.example.hypechat.R
import com.example.hypechat.data.model.rest.response.ChatResponse
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chat: ChatResponse): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.latestMessageRowTextView.text = chat.content

        viewHolder.itemView.fullnameLatestMessageRowTextView.text = chat.chatName
        Picasso.get().load(chat.chatPicture)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.profile_placeholder)
            .into(viewHolder.itemView.userLatestMessageRowImageView)
    }
}