package com.example.hypechat.presentation.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.ChatFromItem
import com.example.hypechat.data.model.ChatToItem
import com.example.hypechat.data.model.rest.response.MessageResponse
import com.example.hypechat.data.model.rest.response.UserResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.google.firebase.auth.FirebaseAuth
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
        val SENDERID = "SENDERID"
        val USERPROFILE = 5
    }

    private var user: UserResponse? = null
    private val auth = FirebaseAuth.getInstance()
    private var senderId: Int? = null
    private var userName: String? = null
    private val chatLogAdapter = GroupAdapter<ViewHolder>()
    private val TAG = "ChatLog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        user = intent.getSerializableExtra(USER) as UserResponse?
        val username = intent.getStringExtra(USERNAME)
        val sender = intent.getIntExtra(SENDERID, 0)
        user?.let {
            toolbarChatLog.title = it.username
            userName = it.username
            senderId = it.id
        }
        if (username != null && sender != 0){
            toolbarChatLog.title = username
            userName = username
            senderId = sender
        }
        setSupportActionBar(toolbarChatLog)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        chatLogRecyclerView.layoutManager = LinearLayoutManager(this)
        chatLogRecyclerView.adapter = chatLogAdapter
        initializeChatLog()
    }

    private fun verifyUserIsLoggedIn(){
        val auth = AppPreferences.getToken()
        if (auth == null){
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat_log, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.action_view_profile -> {
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra(UserProfileActivity.USERID, senderId!!)
            intent.putExtra(UserProfileActivity.USERNAME, userName!!)
            startActivityForResult(intent, USERPROFILE)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null){
            senderId = data.getIntExtra(UserProfileActivity.USERID, 0)
        }
    }

    private fun initializeChatLog(){

        chatLogProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getMessagesFromChat(teamId, senderId!!){ response ->

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
        val userId = AppPreferences.getUserId()

        for (message in sortedMessages){
            if (message.sender.id == userId){
                chatLogAdapter.add(ChatFromItem(message.message))
            } else {
                chatLogAdapter.add(ChatToItem(message.message))
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
            val teamId = AppPreferences.getTeamId()

            HypechatRepository().sendMessage(senderId!!, message, teamId){ response ->

                response?.let {

                    when (it.status){
                        ServerStatus.SENT.status -> Log.d(TAG, "sendMessage: ${it.status}")
                        ServerStatus.WRONG_TOKEN.status -> tokenFailed(it.message)
                        ServerStatus.USER_NOT_FOUND.status -> sendMessageFailed(it.message)
                        ServerStatus.ERROR.status -> sendMessageFailed(it.message)
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
