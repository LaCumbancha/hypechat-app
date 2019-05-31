package com.example.hypechat.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.LatestMessageRow
import com.example.hypechat.data.model.rest.response.ChatResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        val TEAM_ID = "TEAM_ID"
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val latestMessagesAdapter = GroupAdapter<ViewHolder>()
    private val latestMessagesList = mutableListOf<ChatResponse>()
    private val TAG = "LatestMessages"
    private var fab_open: Animation? = null
    private var fab_close: Animation? = null
    private var fab_clock: Animation? = null
    private var fab_anticlock: Animation? = null
    private var isOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        setSupportActionBar(toolbarLatestMessages)
        AppPreferences.init(this)
        loadAnimations()

        val teamId = intent.getIntExtra(TEAM_ID, 0)
        if (teamId != 0){
            AppPreferences.setTeamId(teamId)
        }

        latestMessagesRecyclerView.layoutManager = LinearLayoutManager(this)
        latestMessagesRecyclerView.adapter = latestMessagesAdapter
        latestMessagesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        verifyUserIsLoggedIn()
        initializeLatestMessages()
        setAdapterOnItemClickListener()
    }

    private fun loadAnimations(){
        fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fab_close = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        fab_clock = AnimationUtils.loadAnimation(this, R.anim.fab_rotate_clock)
        fab_anticlock = AnimationUtils.loadAnimation(this, R.anim.fab_rotate_anticlock)
    }

    private fun verifyUserIsLoggedIn(){
        val auth = AppPreferences.getToken()
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
            intent.putExtra(ChatLogActivity.SENDERID, row.chat.senderId)
            intent.putExtra(ChatLogActivity.RECEIVERID, row.chat.receiverId)
            startActivity(intent)
        }
    }

    private fun refreshLatestMessagesRecyclerView(){
        latestMessagesAdapter.clear()
        for (chat in latestMessagesList){
            latestMessagesAdapter.add(LatestMessageRow(chat))
        }
    }

    private fun initializeLatestMessages(){
        latestMessagesProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getChatsPreviews(teamId){response ->

             response?.let {

                 when (it.status){
                     ServerStatus.LIST.status -> initializeChats(it.chats)
                     ServerStatus.WRONG_TOKEN.status -> tokenFailed(it.message)
                     ServerStatus.CHAT_NOT_FOUND.status -> loadingChatsFailed(it.message)
                 }
             }
            if (response == null){
                Toast.makeText(this, "getChatsPreviews failed", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "getChatsPreviews:failure")
                latestMessagesProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun initializeChats(chats: List<ChatResponse>){

        val sortedChats = chats.sortedByDescending { chat ->
            LocalDateTime.parse(chat.timestamp, DateTimeFormatter.RFC_1123_DATE_TIME)
        }
        for (chat in sortedChats){
            latestMessagesList.add(chat)
        }
        Log.d(TAG, "getChatsPreviews:success")
        refreshLatestMessagesRecyclerView()
        latestMessagesProgressBar.visibility = View.INVISIBLE
    }

    private fun loadingChatsFailed(msg: String){

        latestMessagesProgressBar.visibility = View.INVISIBLE

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(msg)

        builder.setPositiveButton("Refresh"){ dialog, which ->
            dialog.dismiss()
            initializeLatestMessages()
        }
        builder.setNegativeButton("Cancel"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun openNew(view: View){

        if (isOpen) {
            newMessageTextView.visibility = View.INVISIBLE
            newChannelTextView.visibility = View.INVISIBLE
            fabNewChannel.startAnimation(fab_close)
            fabNewMessage.startAnimation(fab_close)
            fabNew.startAnimation(fab_anticlock)
            fabNewChannel.isClickable = false
            fabNewMessage.isClickable =false
            isOpen = false

        } else {
            newMessageTextView.visibility = View.VISIBLE
            newChannelTextView.visibility = View.VISIBLE
            fabNewChannel.startAnimation(fab_open)
            fabNewMessage.startAnimation(fab_open)
            fabNew.startAnimation(fab_clock)
            fabNewChannel.isClickable = true
            fabNewMessage.isClickable = true
            isOpen = true
        }
    }

    fun newMessage(view: View){
        val intent = Intent(this, NewMessageActivity::class.java)
        startActivity(intent)
    }

    fun newChannel(view: View){
        //val intent = Intent(this, NewChannelActivity::class.java)
        //startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.hypechat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.action_my_teams -> {
            val intent = Intent(this, TeamsActivity::class.java)
            startActivity(intent)
            true
        }

        R.id.action_my_profile -> {
            val intent = Intent(this, MyProfileActivity::class.java)
            startActivity(intent)
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
                when (it.status){
                    ServerStatus.LOGGED_OUT.status -> navigateToMain()
                    ServerStatus.WRONG_TOKEN.status -> tokenFailed(it.message)
                }

            }
            if (response == null){
                Toast.makeText(this, "Sing out failed", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "logout:failure")
                latestMessagesProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun navigateToMain(){
        Toast.makeText(this, ServerStatus.LOGGED_OUT.status, Toast.LENGTH_SHORT).show()
        Log.d(TAG, "logout:success")
        AppPreferences.clearSharedPreferences()
        val intentMain = Intent(this, MainActivity::class.java)
        intentMain.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intentMain)
        latestMessagesProgressBar.visibility = View.INVISIBLE
    }

    private fun tokenFailed(msg: String){

        latestMessagesProgressBar.visibility = View.INVISIBLE
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
