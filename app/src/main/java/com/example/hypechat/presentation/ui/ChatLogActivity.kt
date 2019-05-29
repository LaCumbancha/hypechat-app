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
import com.example.hypechat.data.model.ChatFromItem
import com.example.hypechat.data.model.ChatMessage
import com.example.hypechat.data.model.ChatToItem
import com.example.hypechat.data.model.User
import com.example.hypechat.data.model.rest.MessageResponse
import com.example.hypechat.data.model.rest.UserResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
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
        val RECEIVERID = "RECEIVERID"
        val SENDERID = "SENDERID"
    }

    private var user: UserResponse? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val fromId = auth.uid
    private var selectedUserId: Int? = null
    private var senderId: Int? = null
    private var receiverId: Int? = null
    private val chatLogAdapter = GroupAdapter<ViewHolder>()
    private val TAG = "ChatLog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        user = intent.getSerializableExtra(USER) as UserResponse?
        val username = intent.getStringExtra(USERNAME)
        val receiver = intent.getIntExtra(RECEIVERID, 0)
        val sender = intent.getIntExtra(SENDERID, 0)
        user?.let {
            toolbarChatLog.title = it.username
            receiverId = it.id
        }
        if (username != null && receiver != 0 && sender != 0){
            toolbarChatLog.title = username
            senderId = sender
            receiverId = receiver
        }
        setSupportActionBar(toolbarChatLog)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        chatLogRecyclerView.layoutManager = LinearLayoutManager(this)
        chatLogRecyclerView.adapter = chatLogAdapter
        initializeChatLog()
    }

    private fun verifyUserIsLoggedIn(){
        val auth = AppPreferences.getCookies()
        if (auth == null){
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun initializeChatLog(){

        chatLogProgressBar.visibility = View.VISIBLE
        HypechatRepository().getMessagesFromChat(receiverId!!){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> initializeChat(it.messages)
                    ServerStatus.WRONG_TOKEN.status -> tokenFailed(it.message)
                    ServerStatus.CHAT_NOT_FOUND.status -> loadingChatFailed(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getMessagesFromChat:failure")
                chatLogProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun initializeChat(messages: List<MessageResponse>){

        val sortedMessages = messages.sortedBy { message ->
            LocalDateTime.parse(message.timestamp, DateTimeFormatter.RFC_1123_DATE_TIME)
        }
        for (message in sortedMessages){
            if (message.fromId == receiverId){
                chatLogAdapter.add(ChatToItem(message.message))
            } else {
                chatLogAdapter.add(ChatFromItem(message.message))
            }
        }
        Log.d(TAG, "getMessagesFromChat:success")
        chatLogProgressBar.visibility = View.INVISIBLE
    }

    private fun loadingChatFailed(msg: String){

        chatLogProgressBar.visibility = View.INVISIBLE
        Log.w(TAG, msg)

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(msg)

        builder.setPositiveButton("Refresh"){ dialog, which ->
            dialog.dismiss()
            initializeChatLog()
        }
        builder.setNegativeButton("Cancel"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun tokenFailed(msg: String){

        chatLogProgressBar.visibility = View.INVISIBLE
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

    fun sendChatMessage(view: View){

        val message = chatLogEditText.text.toString()

        if (message != ""){

            chatLogAdapter.add(ChatFromItem(message))
            chatLogEditText.text.clear()
            chatLogRecyclerView.scrollToPosition(chatLogAdapter.itemCount - 1)

            HypechatRepository().sendMessage(receiverId!!, message){ response ->

                response?.let {

                    when (it.status){
                        ServerStatus.SENT.status -> Log.d(TAG, "sendMessage: ${it.status}")
                        ServerStatus.WRONG_TOKEN.status -> tokenFailed(it.message)
                        ServerStatus.USER_NOT_FOUND.status -> sendMessageFailed(it.message)
                        else -> Toast.makeText(this, "sendMessage: ${it.status}", Toast.LENGTH_SHORT).show()
                    }
                }
                if (response == null){
                    Toast.makeText(this, "sendMessage failed", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "sendMessage failed")
                }
            }
        }
    }

    private fun sendMessageFailed(msg: String){

        Log.w(TAG, msg)

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("$msg Please, try again.")

        builder.setPositiveButton("Ok"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}
