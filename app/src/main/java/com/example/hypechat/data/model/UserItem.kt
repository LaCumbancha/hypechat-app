package com.example.hypechat.data.model

import com.example.hypechat.R
import com.example.hypechat.data.model.rest.response.UserResponse
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class UserItem(val user: UserResponse): Item<ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        //viewHolder.itemView.usernameTextView.text = user.fullname
        viewHolder.itemView.usernameTextView.text = user.username

        //user.profilePictureUrl
        Picasso.get().load(user.profile_pic)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.profile_placeholder)
                .into(viewHolder.itemView.profileImageView)
    }
}