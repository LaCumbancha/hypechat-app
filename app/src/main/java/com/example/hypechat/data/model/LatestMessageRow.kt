package com.example.hypechat.data.model

import android.graphics.Typeface
import android.view.View
import com.example.hypechat.R
import com.example.hypechat.data.model.rest.response.ChatResponse
import com.example.hypechat.data.rest.utils.MessageType
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

        when (chat.type){
            MessageType.TEXT.type -> viewHolder.itemView.latestMessageRowTextView.text = chat.content
            MessageType.IMAGE.type -> viewHolder.itemView.latestMessageRowTextView.text = MessageType.IMAGE.type
            MessageType.FILE.type -> viewHolder.itemView.latestMessageRowTextView.text = MessageType.FILE.type
        }

        val date = LocalDateTime.parse(chat.timestamp, DateTimeFormatter.RFC_1123_DATE_TIME)
        val format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        val newDateFormat = date.format(format)
        viewHolder.itemView.timeLatestMessageRowTextView.text = newDateFormat

        if (chat.unseen){
            viewHolder.itemView.notificationLatestMessageRowtextView.text = chat.offset.toString()
            viewHolder.itemView.notificationLatestMessageRowtextView.visibility = View.VISIBLE
            val boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD)
            viewHolder.itemView.latestMessageRowTextView.typeface = boldTypeface
        }

        viewHolder.itemView.fullnameLatestMessageRowTextView.text = chat.chatName
        Picasso.get().load(chat.chatPicture)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.profile_placeholder)
            .into(viewHolder.itemView.userLatestMessageRowImageView)
    }
}