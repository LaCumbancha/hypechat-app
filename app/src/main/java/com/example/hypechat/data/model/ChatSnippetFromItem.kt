package com.example.hypechat.data.model

import com.example.hypechat.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_snippet_from_row.view.*

class ChatSnippetFromItem(val message: String, val username: String): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.chat_snippet_from_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.chatSnippetFromTextView.setText(message)
        viewHolder.itemView.userNameChatSnippetFromTextView.text = username
    }
}