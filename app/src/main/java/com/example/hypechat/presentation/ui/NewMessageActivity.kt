package com.example.hypechat.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.User
import com.example.hypechat.data.model.UserItem
import com.example.hypechat.data.model.rest.UserResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
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
    private val TAG = "NewMessage"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        setSupportActionBar(toolbarNewMessage)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        newMessageRecyclerView.layoutManager = LinearLayoutManager(this)
        fetchUsers()
    }

    private fun verifyUserIsLoggedIn(){
        val auth = AppPreferences.getCookies()
        if (auth == null){
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun fetchUsers(){
        newMessageProgressBar.visibility = View.VISIBLE
        HypechatRepository().getUsers{response ->
            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> loadUsers(it.users)
                    ServerStatus.WRONG_TOKEN.status -> tokenFailed(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getUsers:failure")
                newMessageProgressBar.visibility = View.INVISIBLE
            }
        }

    }

    private fun loadUsers(users: List<UserResponse>){

        val adapter = GroupAdapter<ViewHolder>()
        for (user in users){
            adapter.add(UserItem(user))
        }
        Log.d(TAG, "getUsers:success")
        adapter.setOnItemClickListener { item, view ->
            val userItem = item as UserItem
            val intent = Intent(view.context, ChatLogActivity::class.java)
            intent.putExtra(USER, userItem.user)
            startActivity(intent)
            finish()
        }
        newMessageRecyclerView.adapter = adapter
        newMessageProgressBar.visibility = View.INVISIBLE
    }

    private fun tokenFailed(msg: String){

        newMessageProgressBar.visibility = View.INVISIBLE
        Log.w(TAG, msg)

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(msg)

        builder.setPositiveButton("Ok"){ dialog, which ->
            dialog.dismiss()
            verifyUserIsLoggedIn()
        }

        val dialog = builder.create()
        dialog.show()
    }
}
