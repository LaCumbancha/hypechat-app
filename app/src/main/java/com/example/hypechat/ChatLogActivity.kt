package com.example.hypechat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.model.ChatFromItem
import com.example.hypechat.model.ChatMessage
import com.example.hypechat.model.ChatToItem
import com.example.hypechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    private var user: User? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val fromId = auth.uid
    private var toId: String? = null
    private val chatLogAdapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        user = intent.getParcelableExtra(NewMessageActivity.USER)
        user?.let {
            toolbarChatLog.title = it.fullname
            toId = it.uid
        }
        setSupportActionBar(toolbarChatLog)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        chatLogRecyclerView.layoutManager = LinearLayoutManager(this)
        chatLogRecyclerView.adapter = chatLogAdapter
        initializeChatLog()
    }

    private fun initializeChatLog(){

        val ref = db.getReference("/users-messages/$fromId/$toId")
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

        })
    }

    fun sendChatMessage(view: View){

        val message = chatLogEditText.text.toString()

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
        latestMessagesToRef.setValue(chatMessage)
    }
}
