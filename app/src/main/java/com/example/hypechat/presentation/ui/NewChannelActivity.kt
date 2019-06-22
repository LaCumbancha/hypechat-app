package com.example.hypechat.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.rest.response.ChannelResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.MessageType
import com.example.hypechat.data.rest.utils.ServerStatus
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_new_channel.*

class NewChannelActivity : AppCompatActivity() {

    private val TAG = "NewChannel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_channel)

        setSupportActionBar(toolbarNewChannel)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        setSpinner()
    }

    private fun setSpinner(){

        val myStrings = arrayOf("PUBLIC", "PRIVATE")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, myStrings)
    }

    private fun validateField(field: TextInputLayout): Boolean {

        val fieldStr = field.editText!!.text.toString().trim()

        if (fieldStr.isEmpty()){
            field.error = "The field can not be empty"
            return false
        } else {
            field.error = null
            return true
        }
    }

    fun createChannel(view: View){

        if (validateField(newChannelNameTextInputLayout)){

            var description: String? = null
            var welcomeMessage: String? = null
            val name = newChannelNameTextInputLayout.editText!!.text.toString()
            val visibility = spinner.selectedItem.toString()
            newChannelDescriptionTextInputLayout.editText?.let {
                description = it.text.toString()
            }
            newChannelWelcomeMessageTextInputLayout.editText?.let {
                welcomeMessage = it.text.toString()
            }
            val teamId = AppPreferences.getTeamId()

            newChannelProgressBar.visibility = View.VISIBLE
            newChannelCardView.visibility = View.INVISIBLE

            HypechatRepository().createChannel(teamId, name, visibility, description, welcomeMessage){ response ->

                response?.let {

                    when (it.status){
                        ServerStatus.CREATED.status -> sendWelcomeMessage(it.channel)
                        ServerStatus.ALREADY_REGISTERED.status -> errorOccurred(it.message)
                        ServerStatus.ERROR.status -> errorOccurred(it.message)
                    }
                }
                if (response == null){
                    Log.w(TAG, "createChannel:failure")
                    errorOccurred(null)
                }
            }
        }
    }

    private fun sendWelcomeMessage(channel: ChannelResponse){

        Log.d(TAG, "createChannel:success")
        val teamId = AppPreferences.getTeamId()
        var welcomeMessage = "Hello World"
        channel.message?.let {
            welcomeMessage = it
        }

        HypechatRepository().sendMessage(channel.channelId, welcomeMessage, MessageType.TEXT.type, teamId){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.SENT.status -> navigateToLatestMessages()
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.USER_NOT_FOUND.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "sendMessage failed")
                errorOccurred(null)
            }
        }
    }

    private fun navigateToLatestMessages(){

        Log.d(TAG, "sendMessage:success")
        val intent = Intent(this, LatestMessagesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        newChannelProgressBar.visibility = View.INVISIBLE
    }

    private fun errorOccurred(error: String?){
        newChannelProgressBar.visibility = View.INVISIBLE
        newChannelCardView.visibility = View.VISIBLE

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Error")
        var msg = "There was a problem during the creation process. Please, try again."
        error?.let {
            msg = it
        }
        Log.w(TAG, msg)
        builder.setMessage(msg)

        builder.setPositiveButton("Ok"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}
