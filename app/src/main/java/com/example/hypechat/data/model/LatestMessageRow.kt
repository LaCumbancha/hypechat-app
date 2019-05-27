package com.example.hypechat.data.model

import com.example.hypechat.R
import com.example.hypechat.data.model.rest.ChatResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(viewHolder.itemView.userLatestMessageRowImageView)
    }
}