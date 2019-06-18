package com.example.hypechat.presentation.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.UserItem
import com.example.hypechat.data.model.rest.response.UserResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_add_user_to_channel.*

class AddUserToChannelActivity : AppCompatActivity() {

    companion object {
        val CHANNELID = "CHANNELID"
    }

    private val adapter = GroupAdapter<ViewHolder>()
    private val TAG = "AddUserToChannel"
    private var channelId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user_to_channel)

        setSupportActionBar(toolbarAddUserToChannel)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        channelId = intent.getIntExtra(CHANNELID, 0)

        addUserRecyclerView.layoutManager = LinearLayoutManager(this)

        getTeamUsers()
    }

    private fun getTeamUsers(){

        addUserProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getUsers(teamId){ response ->
            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> getChannelUsers(it.users)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getUsers:failure")
                addUserProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun getChannelUsers(teamUsersList: List<UserResponse>){

        addUserProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getChannelUsers(teamId, channelId){ response ->
            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> loadUsers(teamUsersList, it.users)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getChannelUsers:failure")
                addUserProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun <T, U> List<T>.intersect(uList: List<U>, filterPredicate : (T, U) -> Boolean) = filterNot { m -> uList.any { filterPredicate(m, it)} }

    private fun loadUsers(teamUsersList: List<UserResponse>, channelUsersList: List<UserResponse>){

        val filteredList = teamUsersList.intersect(channelUsersList) { a, b ->
            a.id == b.id
        }
        for (user in filteredList){
            adapter.add(UserItem(user))
        }
        adapter.setOnItemClickListener { item, view ->
            val userItem = item as UserItem
            addUser(userItem.user)
        }
        addUserRecyclerView.adapter = adapter
        addUserProgressBar.visibility = View.INVISIBLE
    }

    private fun addUser(user: UserResponse){

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Add")
        builder.setMessage("Are you sure you want to add ${user.username} to the channel?")

        builder.setPositiveButton("Yes"){ dialog, which ->
            dialog.dismiss()
            add(user.id)
        }
        builder.setNegativeButton("No"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun add(userId: Int){

        addUserProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().addUserToChannel(teamId, userId, channelId){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.ADDED.status -> {
                        addUserProgressBar.visibility = View.INVISIBLE
                        Toast.makeText(this, "User added", Toast.LENGTH_SHORT).show()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "addUserToChannel:failure")
                errorOccurred(null)
                addUserProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun errorOccurred(error: String?){

        addUserProgressBar.visibility = View.INVISIBLE

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

    override fun onBackPressed() {

        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        android.R.id.home -> {
            onBackPressed()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
