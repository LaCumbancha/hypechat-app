package com.example.hypechat.data.model

import com.example.hypechat.R
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class UserItem(val user: User): Item<ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.usernameTextView.text = user.fullname

        Picasso.get().load(user.profilePictureUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(viewHolder.itemView.profileImageView)
    }
}