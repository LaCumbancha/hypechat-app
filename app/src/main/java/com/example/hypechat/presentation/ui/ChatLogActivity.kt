package com.example.hypechat.presentation.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.ChatFromItem
import com.example.hypechat.data.model.ChatImageFromItem
import com.example.hypechat.data.model.ChatImageToItem
import com.example.hypechat.data.model.ChatToItem
import com.example.hypechat.data.model.rest.response.BotResponse
import com.example.hypechat.data.model.rest.response.MessageResponse
import com.example.hypechat.data.model.rest.response.UserResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.MessageType
import com.example.hypechat.data.rest.utils.ServerStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.hendraanggrian.appcompat.socialview.Mention
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

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
    private lateinit var mentionAdapter: MentionArrayAdapter<Mention>
    private val TAG = "ChatLog"
    private val REQUEST_CODE_ASK_PERMISSIONS = 123
    private val REQUEST_IMAGE_PICK = 1
    private var currentPhotoPath: String = ""
    private var selectedPhotoUri: Uri? = null
    private var mentionList = ArrayList<Mention>()
    private var userChannelList = ArrayList<UserResponse>()
    private var botChannelList = ArrayList<BotResponse>()
    private var isListening = false
    private val handler = Handler()
    private val refresh = object : Runnable {
        override fun run() {
            initializeChatLog()
            handler.postDelayed(this, 8000)
        }
    }

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
        mentionAdapter = MentionArrayAdapter(this)
        chatLogEditText.mentionAdapter = mentionAdapter

        initializeChatLog()
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

    private fun getBots(){

        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getTeamBots(teamId){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> loadBots(it.bots)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.CHAT_NOT_FOUND.status -> loadingChatFailed(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getTeamBots:failure")
            }
        }
    }

    private fun getChannelUsers(){

        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getChannelUsers(teamId, senderId!!){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> loadUsers(it.users)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.CHAT_NOT_FOUND.status -> loadingChatFailed(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getChannelUsers:failure")
            }
        }
    }

    private fun loadBots(botList: List<BotResponse>){

        botChannelList.addAll(botList)
        for (bot in botList){
            mentionList.add(Mention(bot.name))
        }
        mentionAdapter.addAll(mentionList)
    }

    private fun loadUsers(channelUsersList: List<UserResponse>){

        userChannelList.addAll(channelUsersList)
        for (user in channelUsersList){
            mentionList.add(Mention(user.username, null, user.profile_pic))
        }
        mentionList.add(Mention("all"))
        mentionAdapter.addAll(mentionList)
    }

    private fun initializeChatLog(){

        if (!isListening){
            chatLogProgressBar.visibility = View.VISIBLE
        }

        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getMessagesFromChat(teamId, senderId!!){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> initializeChat(it.messages, it.chat_type)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.CHAT_NOT_FOUND.status -> loadingChatFailed(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getMessagesFromChat:failure")
            }
        }
    }

    private fun initializeChat(messages: List<MessageResponse>, chatType: String){

        chatLogAdapter.clear()
        if (chatType == "CHANNEL"){
            getChannelUsers()
            getBots()
        }
        val sortedMessages = messages.sortedBy { message ->
            LocalDateTime.parse(message.timestamp, DateTimeFormatter.RFC_1123_DATE_TIME)
        }
        val userId = AppPreferences.getUserId()

        for (message in sortedMessages){
            if (message.sender.id == userId){
                when (message.type){
                    MessageType.TEXT.type -> chatLogAdapter.add(ChatFromItem(message.message, message.sender.username))
                    MessageType.IMAGE.type -> chatLogAdapter.add(ChatImageFromItem(message.message, message.sender.username))
                }
            } else {
                when (message.type){
                    MessageType.TEXT.type -> chatLogAdapter.add(ChatToItem(message.message, message.sender.username))
                    MessageType.IMAGE.type -> chatLogAdapter.add(ChatImageToItem(message.message, message.sender.username))
                }
            }
        }
        chatLogRecyclerView.scrollToPosition(chatLogAdapter.itemCount - 1)
        Log.d(TAG, "getMessagesFromChat:success")
        chatLogProgressBar.visibility = View.INVISIBLE
        if (!isListening){
            handler.post(refresh)
            isListening = true
        }
    }

    private fun loadingChatFailed(msg: String){

        chatLogProgressBar.visibility = View.INVISIBLE
        Log.w(TAG, msg)

        val builder = AlertDialog.Builder(this)
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

    private fun errorOccurred(error: String?){

        chatLogProgressBar.visibility = View.INVISIBLE

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
        dialog.show()
    }

    private fun <T, U> List<T>.intersect(uList: List<U>, filterPredicate : (T, U) -> Boolean) = filter { m -> uList.any { filterPredicate(m, it)} }

    fun sendChatMessage(view: View){

        val message = chatLogEditText.text.toString()
        val username = AppPreferences.getUserName()
        val mentions = arrayListOf<Int>()

        if (message != ""){

            val mentionList = chatLogEditText.mentions
            if (mentionList.isNotEmpty()){
                if (mentionList.contains("all")){
                    mentions.add(senderId!!)
                }
                val userFilteredList = userChannelList.intersect(mentionList) { a, b ->
                    a.username == b
                }
                for (user in userFilteredList){
                    mentions.add(user.id)
                }
                val botFilteredList = botChannelList.intersect(mentionList) { a, b ->
                    a.name == b
                }
                for (bot in botFilteredList){
                    mentions.add(bot.id)
                }
            }
            chatLogAdapter.add(ChatFromItem(message, username!!))
            chatLogEditText.text?.clear()
            chatLogRecyclerView.scrollToPosition(chatLogAdapter.itemCount - 1)
            send(message, MessageType.TEXT.type, mentions.toList())
        }
    }

    private fun sendPicture(pictureUrl: String){

        val username = AppPreferences.getUserName()
        val list = listOf<Int>()
        chatLogAdapter.add(ChatImageFromItem(pictureUrl, username!!))
        chatLogRecyclerView.scrollToPosition(chatLogAdapter.itemCount - 1)
        send(pictureUrl, MessageType.IMAGE.type, list)
    }

    private fun send(message: String, messageType: String, mentions: List<Int>){
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().sendMessage(senderId!!, message, messageType, teamId, mentions){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.SENT.status -> Log.d(TAG, "sendMessage: ${it.status}")
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
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

    private fun sendMessageFailed(msg: String){

        Log.w(TAG, msg)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("$msg Please, try again.")

        builder.setPositiveButton("Ok"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun pickImageFromDevice(view: View) {
        val hasReadExternalStoragePermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_ASK_PERMISSIONS)
            return
        }
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null){
            if (requestCode == REQUEST_IMAGE_PICK){
                Log.d(TAG, "Photo was selected")
                selectedPhotoUri = data.data
                currentPhotoPath = getRealPathFromUri(selectedPhotoUri!!)
                savePicture()
            } else {

                senderId = data.getIntExtra(UserProfileActivity.USERID, 0)
            }
        }
    }

    private fun getRealPathFromUri(contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(contentUri, proj, null, null, null)
            assert(cursor != null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }

    private fun savePicture(){

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        if (selectedPhotoUri != null){
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully uploaded chat log picture: ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        Log.d(TAG, "File location: $uri")
                        val pictureUrl = uri.toString()
                        sendPicture(pictureUrl)
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Failed to upload chat log picture to Storage", it.cause)
                    //errorOccurred(it.cause.toString())
                }
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
