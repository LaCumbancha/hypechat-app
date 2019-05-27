package com.example.hypechat.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
    private val latestMessagesMap = HashMap<Int, ChatResponse>()

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
        val token = AppPreferences.getToken()
        if (token == null){
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
        latestMessagesMap.values.forEach {
            latestMessagesAdapter.add(LatestMessageRow(it))
        }
    }

    private fun initializeLatestMessages(){
        /*val fromId = auth.uid
        val ref = db.getReference("/latest-messages/$fromId")

        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshLatestMessagesRecyclerView()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshLatestMessagesRecyclerView()
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })*/
        val username = AppPreferences.getUserName()
        val token = AppPreferences.getToken()
        if (username != null && token != null) {
            HypechatRepository().getChatsPreviews(username, token){response ->

                response?.let {
                    val chats = it.chats
                    for (chat in chats){
                        latestMessagesMap[chat.senderId] = chat
                    }
                    Toast.makeText(this, "getChatsPreviews:success: ${it.status}", Toast.LENGTH_SHORT).show()
                    refreshLatestMessagesRecyclerView()
                }
                if (response == null){
                    Toast.makeText(this, "getUsers failed", Toast.LENGTH_SHORT).show()
                }

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
        val username = AppPreferences.getUserName()
        val token = AppPreferences.getToken()
        if (username != null && token != null){
            latestMessagesProgressBar.visibility = View.VISIBLE
            HypechatRepository().logoutUser(username, token){ response ->

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
}
