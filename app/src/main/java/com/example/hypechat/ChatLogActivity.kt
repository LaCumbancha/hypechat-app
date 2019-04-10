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
    val chatLogAdapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        user = intent.getParcelableExtra(NewMessageActivity.USER)
        user?.let {
            toolbarChatLog.title = it.fullname
        }
        setSupportActionBar(toolbarChatLog)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        chatLogRecyclerView.layoutManager = LinearLayoutManager(this)
        chatLogRecyclerView.adapter = chatLogAdapter
        initializeChatLog()
    }

    private fun initializeChatLog(){

        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object : ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {

                    if (chatMessage.fromId == user!!.uid){
                        chatLogAdapter.add(ChatToItem(chatMessage.message))
                    } else {
                        chatLogAdapter.add(ChatFromItem(chatMessage.message))
                    }
                }
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
        val toId = FirebaseAuth.getInstance().uid
        val fromId = user!!.uid

        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()

        val chatMessage = ChatMessage(ref.key!!, fromId, toId!!, message, System.currentTimeMillis()/1000) //seconds
        ref.setValue(chatMessage)
    }
}
