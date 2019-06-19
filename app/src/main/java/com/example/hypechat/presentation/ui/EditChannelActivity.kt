package com.example.hypechat.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.rest.response.ChannelResponse
import com.example.hypechat.data.model.rest.response.UserResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ChannelVisibility
import com.example.hypechat.data.rest.utils.ServerStatus
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_edit_channel.*

class EditChannelActivity : AppCompatActivity() {

    companion object {
        val CHANNEL = "CHANNEL"
    }

    private var channel: ChannelResponse? = null
    private val TAG = "Edit Channel"
    private val channelVisibility = arrayOf("PUBLIC", "PRIVATE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_channel)

        channel = intent.getSerializableExtra(CHANNEL) as ChannelResponse?
        toolbarEditChannel.title = channel?.name

        setSupportActionBar(toolbarEditChannel)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        setChannelData()
    }

    private fun setChannelData(){

        editChannelCardView.visibility = View.INVISIBLE

        channel?.let { channel ->

            editChannelNameTextInputLayout.editText!!.setText(channel.name)
            channel.description?.let {
                editChannelDescriptionTextInputLayout.editText!!.setText(it)
            }
            channel.message.let {
                editChannelWelcomeMessageTextInputLayout.editText!!.setText(it)
            }
            setInitSpinner()

            if (channel.creator.id == AppPreferences.getUserId()){
                addUserButton.visibility = View.VISIBLE
                deleteChannelButton.visibility = View.VISIBLE
                editChannelButton.visibility = View.VISIBLE
                editChannelCardView.visibility = View.VISIBLE
            } else {
                checkIfUserIsInChannel()
            }
        }
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

    private fun setInitSpinner(){

        val myStrings = arrayOf(channel?.visibility)
        editChannelSpinner.isEnabled = false
        editChannelSpinner.isClickable = false
        editChannelSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, myStrings)
    }

    private fun checkIfUserIsInChannel(){

        editChannelProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getChannelUsers(teamId, channel!!.id){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> setButtonsIsInChannel()
                    ServerStatus.NOT_ENOUGH_PERMISSIONS.status -> setButtonsIsNotInChannel()
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getChannelUsers:failure")
                errorOccurred(null)
                editChannelProgressBar.visibility = View.INVISIBLE
                editChannelCardView.visibility = View.VISIBLE
            }
        }
    }

    private fun setButtonsIsInChannel(){

        editChannelProgressBar.visibility = View.INVISIBLE
        editChannelCardView.visibility = View.VISIBLE
        //val userIsInChannel = users.any { x -> x.id == AppPreferences.getUserId() }
        leaveChannelButton.visibility = View.VISIBLE
    }

    private fun setButtonsIsNotInChannel(){

        editChannelProgressBar.visibility = View.INVISIBLE
        editChannelCardView.visibility = View.VISIBLE
        joinChannelButton.visibility = View.VISIBLE
        viewUsersButton.visibility = View.INVISIBLE

        if (channel!!.visibility == ChannelVisibility.PRIVATE.visibility){
            joinChannelButton.visibility = View.INVISIBLE
        }
    }

    fun editChannel(view: View){

        editChannelButton.visibility = View.INVISIBLE
        cancelChannelButton.visibility = View.VISIBLE
        saveChannelButton.visibility = View.VISIBLE
        editChannelNameTextInputLayout.editText!!.isFocusable = true
        editChannelNameTextInputLayout.editText!!.isFocusableInTouchMode = true
        editChannelDescriptionTextInputLayout.editText!!.isFocusable = true
        editChannelDescriptionTextInputLayout.editText!!.isFocusableInTouchMode = true
        editChannelWelcomeMessageTextInputLayout.editText!!.isFocusable = true
        editChannelWelcomeMessageTextInputLayout.editText!!.isFocusableInTouchMode = true
        editChannelSpinner.isEnabled = true
        editChannelSpinner.isClickable = true
        editChannelSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, channelVisibility)
    }

    fun cancelEditChannel(view: View){

        editChannelButton.visibility = View.VISIBLE
        saveChannelButton.visibility = View.INVISIBLE
        cancelChannelButton.visibility = View.INVISIBLE
        editChannelNameTextInputLayout.editText!!.isFocusable = false
        editChannelNameTextInputLayout.editText!!.isFocusableInTouchMode = false
        editChannelDescriptionTextInputLayout.editText!!.isFocusable = false
        editChannelDescriptionTextInputLayout.editText!!.isFocusableInTouchMode = false
        editChannelWelcomeMessageTextInputLayout.editText!!.isFocusable = false
        editChannelWelcomeMessageTextInputLayout.editText!!.isFocusableInTouchMode = false
        setInitSpinner()
    }

    fun addUser(view: View){
        val intent = Intent(this, AddUserToChannelActivity::class.java)
        intent.putExtra(AddUserToChannelActivity.CHANNELID, channel!!.id)
        startActivity(intent)
    }

    fun viewUsers(view: View){
        val intent = Intent(this, ViewChannelUsersActivity::class.java)
        intent.putExtra(ViewChannelUsersActivity.CHANNELID, channel!!.id)
        intent.putExtra(ViewChannelUsersActivity.CREATORID, channel!!.creator.id)
        startActivity(intent)
    }

    fun joinChannel(view: View){

        editChannelCardView.visibility = View.INVISIBLE
        editChannelProgressBar.visibility = View.VISIBLE

        HypechatRepository().joinChannel(AppPreferences.getTeamId(), channel!!.id){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.JOINED.status -> {
                        editChannelProgressBar.visibility = View.INVISIBLE
                        Toast.makeText(this, "Joined channel successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LatestMessagesActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "joinChannel:failure")
                errorOccurred(null)
                editChannelProgressBar.visibility = View.INVISIBLE
                editChannelCardView.visibility = View.VISIBLE
            }
        }
    }

    fun leaveChannel(view: View){

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Leave Channel")
        builder.setMessage("Are you sure you want to leave the channel?")

        builder.setPositiveButton("Yes"){ dialog, which ->
            dialog.dismiss()
            leave()
        }
        builder.setNegativeButton("No"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun leave(){

        editChannelCardView.visibility = View.INVISIBLE
        editChannelProgressBar.visibility = View.VISIBLE

        HypechatRepository().leaveChannel(AppPreferences.getTeamId(), channel!!.id){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.REMOVED.status -> {
                        editChannelProgressBar.visibility = View.INVISIBLE
                        val intent = Intent(this, LatestMessagesActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "leaveChannel:failure")
                errorOccurred(null)
                editChannelProgressBar.visibility = View.INVISIBLE
                editChannelCardView.visibility = View.VISIBLE
            }
        }
    }

    fun saveChangesChannel(view: View){

        if (validateField(editChannelNameTextInputLayout)){

            editChannelCardView.visibility = View.INVISIBLE
            editChannelProgressBar.visibility = View.VISIBLE
            val channelName = editChannelNameTextInputLayout.editText!!.text.toString()

            var description: String? = null
            if (editChannelDescriptionTextInputLayout.editText!!.text.toString().isNotBlank()){
                description = editChannelDescriptionTextInputLayout.editText!!.text.toString()
            }
            var welcomeMessage: String? = null
            if (editChannelWelcomeMessageTextInputLayout.editText!!.text.toString().isNotBlank()){
                welcomeMessage = editChannelWelcomeMessageTextInputLayout.editText!!.text.toString()
            }
            val visibility = editChannelSpinner.selectedItem.toString()

            save(channelName, visibility, description, welcomeMessage)
        }
    }

    private fun save(channelName: String, visibility: String, description: String?, welcomeMessage: String?){

        val teamId = AppPreferences.getTeamId()

        HypechatRepository().updateChannel(teamId, channel!!.id,
            channelName, visibility, description, welcomeMessage){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.UPDATED.status -> {
                        Toast.makeText(this, "Channel information updated", Toast.LENGTH_SHORT).show()
                        editChannelProgressBar.visibility = View.INVISIBLE
                        editChannelCardView.visibility = View.VISIBLE
                        cancel()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "updateChannel:failure")
                errorOccurred(null)
                editChannelProgressBar.visibility = View.INVISIBLE
                editChannelCardView.visibility = View.VISIBLE
            }
        }
    }

    fun deleteChannel(view: View){

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Delete Channel")
        builder.setMessage("Are you sure you want to delete the channel?")

        builder.setPositiveButton("Yes"){ dialog, which ->
            dialog.dismiss()
            delete()
        }
        builder.setNegativeButton("No"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun delete(){

        editChannelCardView.visibility = View.INVISIBLE
        editChannelProgressBar.visibility = View.VISIBLE

        HypechatRepository().deleteChannel(AppPreferences.getTeamId(), channel!!.id){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.REMOVED.status -> {
                        editChannelProgressBar.visibility = View.INVISIBLE
                        val intent = Intent(this, LatestMessagesActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "deleteChannel:failure")
                errorOccurred(null)
                editChannelProgressBar.visibility = View.INVISIBLE
                editChannelCardView.visibility = View.VISIBLE
            }
        }
    }

    private fun errorOccurred(error: String?){
        editChannelProgressBar.visibility = View.INVISIBLE
        editChannelCardView.visibility = View.VISIBLE

        val builder = android.app.AlertDialog.Builder(this)
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

    private fun cancel(){
        editChannelButton.visibility = View.VISIBLE
        saveChannelButton.visibility = View.INVISIBLE
        cancelChannelButton.visibility = View.INVISIBLE
        editChannelNameTextInputLayout.editText!!.isFocusable = false
        editChannelNameTextInputLayout.editText!!.isFocusableInTouchMode = false
        editChannelDescriptionTextInputLayout.editText!!.isFocusable = false
        editChannelDescriptionTextInputLayout.editText!!.isFocusableInTouchMode = false
        editChannelWelcomeMessageTextInputLayout.editText!!.isFocusable = false
        editChannelWelcomeMessageTextInputLayout.editText!!.isFocusableInTouchMode = false
        setInitSpinner()
    }
}
