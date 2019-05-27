package com.example.hypechat.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.ChatFromItem
import com.example.hypechat.data.model.ChatMessage
import com.example.hypechat.data.model.ChatToItem
import com.example.hypechat.data.model.User
import com.example.hypechat.data.model.rest.UserResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val USER = "USER"
        val USERNAME = "USERNAME"
        val USERID = "USERID"
    }

    private var user: UserResponse? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val fromId = auth.uid
    private var selectedUserId: Int? = null
    private val chatLogAdapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        user = intent.getSerializableExtra(USER) as UserResponse?
        val username = intent.getStringExtra(USERNAME)
        val selectedId = intent.getIntExtra(USERID, 0)
        user?.let {
            toolbarChatLog.title = it.username
            selectedUserId = it.id
        }
        if (username != null && selectedId != 0){
            toolbarChatLog.title = username
            selectedUserId = selectedId
        }
        setSupportActionBar(toolbarChatLog)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        chatLogRecyclerView.layoutManager = LinearLayoutManager(this)
        chatLogRecyclerView.adapter = chatLogAdapter
        initializeChatLog()
    }

    private fun initializeChatLog(){

        /*val ref = db.getReference("/users-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {

                    if (chatMessage.fromId == auth.uid){
                        chatLogAdapter.add(ChatFromItem(chatMessage.message))
                    } else {
                        chatLogAdapter.add(ChatToItem(chatMessage.message))
                    }
                }
                chatLogRecyclerView.scrollToPosition(chatLogAdapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })*/

        val username = AppPreferences.getUserName()
        val token = AppPreferences.getToken()
        if (username != null && token != null && selectedUserId != null) {
            HypechatRepository().getMessagesFromChat(username, token, selectedUserId!!){ response ->

                response?.let {
                    val messages = it.messages.sortedBy { message -> LocalDateTime.parse(message.timestamp, DateTimeFormatter.RFC_1123_DATE_TIME) }
                    for (message in messages){
                        if (message.fromId == selectedUserId){
                            chatLogAdapter.add(ChatToItem(message.message))
                        } else {
                            chatLogAdapter.add(ChatFromItem(message.message))
                        }
                    }
                    Toast.makeText(this, "getUsers: ${it.status}", Toast.LENGTH_SHORT).show()
                }
                if (response == null){
                    Toast.makeText(this, "getMessagesFromChat failed", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    fun sendChatMessage(view: View){

        /*val message = chatLogEditText.text.toString()

        val ref = db.getReference("/users-messages/$fromId/$toId").push()
        val toRef = db.getReference("/users-messages/$toId/$fromId").push()
        val latestMessagesRef = db.getReference("/latest-messages/$fromId/$toId")
        val latestMessagesToRef = db.getReference("/latest-messages/$toId/$fromId")

        val chatMessage = ChatMessage(ref.key!!, fromId!!, toId!!, message, System.currentTimeMillis()/1000) //seconds
        ref.setValue(chatMessage)
            .addOnSuccessListener {
                chatLogEditText.text.clear()
                chatLogRecyclerView.scrollToPosition(chatLogAdapter.itemCount - 1)
            }
        toRef.setValue(chatMessage)
        latestMessagesRef.setValue(chatMessage)
        latestMessagesToRef.setValue(chatMessage)*/
    }
}
