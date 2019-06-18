package com.example.hypechat.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.UserItem
import com.example.hypechat.data.model.rest.response.UserResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_view_channel_users.*

class ViewChannelUsersActivity : AppCompatActivity() {

    companion object {
        val CHANNELID = "CHANNELID"
        val CREATORID = "CREATORID"
    }

    private val adapter = GroupAdapter<ViewHolder>()
    private val TAG = "AddUserToChannel"
    private var channelId: Int = -1
    private var creatorId: Int = -1
    private var arrayList = ArrayList<UserItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_channel_users)

        setSupportActionBar(toolbarViewUsersChannel)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        channelId = intent.getIntExtra(CHANNELID, 0)
        creatorId = intent.getIntExtra(CREATORID, 0)

        viewUsersRecyclerView.layoutManager = LinearLayoutManager(this)
        viewUsersRecyclerView.adapter = adapter

        getUsers()
    }

    private fun getUsers(){

        viewUsersProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getChannelUsers(teamId, channelId){ response ->
            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> loadUsers(it.users)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getChannelUsers:failure")
                errorOccurred(null)
                viewUsersProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun loadUsers(channelUsersList: List<UserResponse>){

        val userId = AppPreferences.getUserId()

        for (user in channelUsersList){
            arrayList.add(UserItem(user))
        }
        adapter.addAll(arrayList)
        if (userId == creatorId){
            adapter.setOnItemClickListener { item, view ->
                val userItem = item as UserItem
                deleteUser(userItem.user)
            }
        }
        viewUsersProgressBar.visibility = View.INVISIBLE
    }

    private fun deleteUser(user: UserResponse){

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Remove")
        builder.setMessage("Are you sure you want to remove ${user.username} from the channel?")

        builder.setPositiveButton("Yes"){ dialog, which ->
            dialog.dismiss()
            remove(user.id)
        }
        builder.setNegativeButton("No"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun remove(userId: Int){

        viewUsersProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().deleteUserFromChannel(teamId, userId, channelId){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.REMOVED.status -> {
                        viewUsersProgressBar.visibility = View.INVISIBLE
                        Toast.makeText(this, "User removed", Toast.LENGTH_SHORT).show()
                        adapter.removeAll(arrayList)
                        arrayList.clear()
                        getUsers()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "deleteUserFromChannel:failure")
                errorOccurred(null)
                viewUsersProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun errorOccurred(error: String?){

        viewUsersProgressBar.visibility = View.INVISIBLE

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
