package com.example.hypechat.data.model

import com.example.hypechat.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class ChatImageFromItem(val image: String, val username: String): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.chat_image_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

    }
}