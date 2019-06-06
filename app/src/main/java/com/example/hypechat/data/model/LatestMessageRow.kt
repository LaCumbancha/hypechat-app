package com.example.hypechat.data.model

import android.view.View
import com.example.hypechat.R
import com.example.hypechat.data.model.rest.response.ChatResponse
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LatestMessageRow(val chat: ChatResponse): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.latestMessageRowTextView.text = chat.content

        val date = LocalDateTime.parse(chat.timestamp, DateTimeFormatter.RFC_1123_DATE_TIME)
        val format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        val newDateFormat = date.format(format)
        viewHolder.itemView.timeLatestMessageRowTextView.text = newDateFormat

        if (chat.unseen){
            viewHolder.itemView.notificationLatestMessageRowtextView.text = chat.offset.toString()
            viewHolder.itemView.notificationLatestMessageRowtextView.visibility = View.VISIBLE
        }

        viewHolder.itemView.fullnameLatestMessageRowTextView.text = chat.chatName
        Picasso.get().load(chat.chatPicture)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.profile_placeholder)
            .into(viewHolder.itemView.userLatestMessageRowImageView)
    }
}