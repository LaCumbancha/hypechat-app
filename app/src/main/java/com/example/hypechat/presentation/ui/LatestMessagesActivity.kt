package com.example.hypechat.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.util.forEach
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.ChatMessage
import com.example.hypechat.data.model.LatestMessageRow
import com.example.hypechat.data.model.rest.ChatResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessagesActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val latestMessagesAdapter = GroupAdapter<ViewHolder>()
    private val latestMessagesMap = SparseArray<ChatResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        setSupportActionBar(toolbarLatestMessages)
        AppPreferences.init(this)
        verifyUserIsLoggedIn()

        latestMessagesRecyclerView.layoutManager = LinearLayoutManager(this)
        latestMessagesRecyclerView.adapter = latestMessagesAdapter
        latestMessagesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        initializeLatestMessages()
        setAdapterOnItemClickListener()
    }

    private fun verifyUserIsLoggedIn(){
        val auth = AppPreferences.getCookies()
        if (auth == null){
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun setAdapterOnItemClickListener(){
        latestMessagesAdapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow

            intent.putExtra(ChatLogActivity.USERNAME, row.chat.chatName)
            intent.putExtra(ChatLogActivity.USERID, row.chat.receiverId)
            startActivity(intent)
        }
    }

    private fun refreshLatestMessagesRecyclerView(){
        latestMessagesAdapter.clear()
        latestMessagesMap.forEach { key, value ->
            latestMessagesAdapter.add(LatestMessageRow(value))
        }
    }

    private fun initializeLatestMessages(){
        latestMessagesProgressBar.visibility = View.VISIBLE
        HypechatRepository().getChatsPreviews{response ->
             response?.let {
                 val chats = it.chats
                 for (chat in chats){
                     latestMessagesMap.put(chat.receiverId, chat)
                 }
                 Toast.makeText(this, "getChatsPreviews:success: ${it.status}", Toast.LENGTH_SHORT).show()
                 refreshLatestMessagesRecyclerView()
                 latestMessagesProgressBar.visibility = View.INVISIBLE
             }
            if (response == null){
                Toast.makeText(this, "getUsers failed", Toast.LENGTH_SHORT).show()
                latestMessagesProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    fun newMessage(view: View){
        val intent = Intent(this, NewMessageActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.hypechat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_my_profile -> {
            true
        }

        R.id.action_exit -> {
            logout()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun logout(){
        latestMessagesProgressBar.visibility = View.VISIBLE
        HypechatRepository().logoutUser{ response ->

            response?.let {
                Toast.makeText(this, "logout:success: ${it.status}", Toast.LENGTH_SHORT).show()
                AppPreferences.clearSharedPreferences()
                val intentMain = Intent(this, MainActivity::class.java)
                intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intentMain)
                latestMessagesProgressBar.visibility = View.INVISIBLE
            }
            if (response == null){
                Toast.makeText(this, "Sing out failed", Toast.LENGTH_SHORT).show()
                latestMessagesProgressBar.visibility = View.INVISIBLE
            }
        }
    }
}
