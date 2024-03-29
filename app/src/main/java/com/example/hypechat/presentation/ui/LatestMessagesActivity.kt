package com.example.hypechat.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.ChannelRow
import com.example.hypechat.data.model.ExpandableHeaderItem
import com.example.hypechat.data.model.LatestMessageRow
import com.example.hypechat.data.model.rest.response.ChannelResponse
import com.example.hypechat.data.model.rest.response.ChatResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
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
    private val channelList = mutableListOf<ChannelResponse>()
    private val TAG = "LatestMessages"
    private var fab_open: Animation? = null
    private var fab_close: Animation? = null
    private var fab_clock: Animation? = null
    private var fab_anticlock: Animation? = null
    private var isOpen = false
    private var isListening = false
    private val handler = Handler()
    private val refresh = object : Runnable {
        override fun run() {
            getLatestMessages()
            handler.postDelayed(this, 8000)
        }
    }

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
        getLatestMessages()
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
        val teamId = AppPreferences.getTeamId()

        if (auth == null){
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else if (teamId == 0) {
            verifyUserTeam()
        }
    }

    private fun verifyUserTeam(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Warning")
        builder.setMessage("You currently have no team selected, please create or choose one.")

        builder.setPositiveButton("Ok"){ dialog, which ->
            dialog.dismiss()
            val intent = Intent(this, TeamsActivity::class.java)
            startActivity(intent)
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun setAdapterOnItemClickListener(){
        latestMessagesAdapter.setOnItemClickListener { item, view ->

            when (item.layout) {
                R.layout.latest_message_row -> {
                    val intent = Intent(this, ChatLogActivity::class.java)
                    val row = item as LatestMessageRow
                    intent.putExtra(ChatLogActivity.USERNAME, row.chat.chatName)
                    intent.putExtra(ChatLogActivity.SENDERID, row.chat.chat_id)
                    startActivity(intent)
                }
                R.layout.channel_row -> {
                    val intent = Intent(this, EditChannelActivity::class.java)
                    val row = item as ChannelRow
                    intent.putExtra(EditChannelActivity.CHANNEL, row.channel)
                    startActivity(intent)
                }
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    private fun refreshLatestMessagesRecyclerView(){
        latestMessagesAdapter.clear()
        val chatList = mutableListOf<LatestMessageRow>()
        for (chat in latestMessagesList){
            chatList.add(LatestMessageRow(chat))
        }
        ExpandableGroup(ExpandableHeaderItem("Chats"), true).apply {
            add(Section(chatList))
            latestMessagesAdapter.add(this)
        }
        val list = mutableListOf<ChannelRow>()
        for (channel in channelList){
            list.add(ChannelRow(channel))
        }
        ExpandableGroup(ExpandableHeaderItem("Channels"), true).apply {
            add(Section(list))
            latestMessagesAdapter.add(this)
        }
        latestMessagesProgressBar.visibility = View.INVISIBLE
        if (!isListening){
            handler.post(refresh)
            isListening = true
        }
    }

    private fun getLatestMessages(){

        if (!isListening){
            latestMessagesProgressBar.visibility = View.VISIBLE
        }

        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getChatsPreviews(teamId){response ->

             response?.let {

                 when (it.status){
                     ServerStatus.LIST.status -> initializeChats(it.chats)
                     ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                     ServerStatus.CHAT_NOT_FOUND.status -> errorOccurred(it.message)
                     ServerStatus.ERROR.status -> errorOccurred(it.message)
                 }
             }
            if (response == null){
                Log.w(TAG, "getChatsPreviews:failure")
                //errorOccurred(null)
            }
        }
    }

    private fun initializeChats(chats: List<ChatResponse>){

        val filterChats = chats.distinctBy { it.chat_id }
        val sortedChats = filterChats.sortedByDescending { chat ->
            LocalDateTime.parse(chat.timestamp, DateTimeFormatter.RFC_1123_DATE_TIME)
        }
        latestMessagesList.clear()
        for (chat in sortedChats){
            latestMessagesList.add(chat)
        }
        Log.d(TAG, "getChatsPreviews:success")
        getChannels()
    }

    private fun initializeChannels(channels: List<ChannelResponse>){

        channelList.clear()
        for (channel in channels){
            channelList.add(channel)
        }
        Log.d(TAG, "getTeamChannels:success")
        refreshLatestMessagesRecyclerView()
    }

    private fun getChannels(){

        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getTeamChannels(teamId){response ->

            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> initializeChannels(it.channels)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getTeamChannels:failure")
                //errorOccurred(null)
            }
        }
    }

    fun openNew(view: View){

        if (isOpen) {
            closeNew()

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

    private fun closeNew(){
        newMessageTextView.visibility = View.INVISIBLE
        newChannelTextView.visibility = View.INVISIBLE
        fabNewChannel.startAnimation(fab_close)
        fabNewMessage.startAnimation(fab_close)
        fabNew.startAnimation(fab_anticlock)
        fabNewChannel.isClickable = false
        fabNewMessage.isClickable =false
        isOpen = false
    }

    fun newMessage(view: View){
        closeNew()
        val intent = Intent(this, NewMessageActivity::class.java)
        startActivity(intent)
    }

    fun newChannel(view: View){
        closeNew()
        val intent = Intent(this, NewChannelActivity::class.java)
        startActivity(intent)
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
                    ServerStatus.LOGGED_OUT.status -> {
                        AppPreferences.getFacebookToken()?.let {
                            LoginManager.getInstance().logOut()
                        }
                        navigateToMain()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                    else -> errorOccurred(it.message)
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

    private fun errorOccurred(error: String?){

        latestMessagesProgressBar.visibility = View.INVISIBLE
        handler.removeCallbacks(refresh)
        isListening = false

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        var msg = "There was a problem during the process. Please, try again."
        error?.let {
            msg = it
        }
        builder.setMessage(msg)

        builder.setPositiveButton("Ok"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        if(!this.isFinishing){
            dialog.show()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "In the onStop() event")
        handler.removeCallbacks(refresh)
        isListening = false
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "In the onResume() event")
        if (!isListening){
            handler.post(refresh)
            isListening = true
        }
    }
}
