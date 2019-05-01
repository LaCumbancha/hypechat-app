package com.example.hypechat.data.model

import com.example.hypechat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>() {

    var chatPartnerUser : User? = null

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.latestMessageRowTextView.text = chatMessage.message

        val chatPartnerId =
            if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                chatMessage.toId
            } else {
                chatMessage.fromId
            }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.fullnameLatestMessageRowTextView.text = chatPartnerUser?.fullname
                Picasso.get().load(chatPartnerUser?.profilePictureUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(viewHolder.itemView.userLatestMessageRowImageView)
            }

        })
    }
}