package com.example.hypechat.data.model

import com.example.hypechat.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_image_from_row.view.*

class ChatImageFromItem(val image: String, val username: String): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.chat_image_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.userNameChatImageFromTextView.text = username

        Picasso.get().load(image)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.profile_placeholder)
            .into(viewHolder.itemView.chatFromImageView)
    }
}