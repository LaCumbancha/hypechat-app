package com.example.hypechat.data.model.rest

import com.example.hypechat.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_snippet_to_row.view.*

class ChatSnippetToItem(val message: String, val username: String): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.chat_snippet_to_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chatSnippetToTextView.setText(message)
        viewHolder.itemView.userNameChatSnippetToTextView.text = username
    }
}