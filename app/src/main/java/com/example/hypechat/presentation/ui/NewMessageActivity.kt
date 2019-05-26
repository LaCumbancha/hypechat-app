package com.example.hypechat.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.User
import com.example.hypechat.data.model.UserItem
import com.example.hypechat.data.repository.HypechatRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessageActivity : AppCompatActivity() {

    companion object {
        val USER = "USER"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        setSupportActionBar(toolbarNewMessage)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        newMessageRecyclerView.layoutManager = LinearLayoutManager(this)
        fetchUsers()
    }

    private fun fetchUsers(){
        /*val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach {
                    val user = it.getValue(User::class.java)
                    user?.let {
                        adapter.add(UserItem(it))
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem

                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER, userItem.user)
                    startActivity(intent)
                    finish()
                }
                newMessageRecyclerView.adapter = adapter
            }

        })*/
        val username = AppPreferences.getUserName()
        val token = AppPreferences.getToken()
        if (username != null && token != null) {
            HypechatRepository().getUsers(username, token){response ->

                response?.let {
                    val users = it.users
                    val adapter = GroupAdapter<ViewHolder>()
                    for (user in users){
                        adapter.add(UserItem(user))
                    }
                    Toast.makeText(this, "getUsers:success: ${it.status}", Toast.LENGTH_SHORT).show()
                    adapter.setOnItemClickListener { item, view ->
                        val userItem = item as UserItem

                        val intent = Intent(view.context, ChatLogActivity::class.java)
                        intent.putExtra(USER, userItem.user)
                        startActivity(intent)
                        finish()
                    }
                    newMessageRecyclerView.adapter = adapter
                }
                if (response == null){
                    Toast.makeText(this, "getUsers failed", Toast.LENGTH_SHORT).show()
                }

            }
        }

    }
}
