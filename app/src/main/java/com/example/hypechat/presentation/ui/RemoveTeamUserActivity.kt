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
import kotlinx.android.synthetic.main.activity_remove_team_user.*

class RemoveTeamUserActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<ViewHolder>()
    private val TAG = "RemoveTeamUser"
    private var arrayList = ArrayList<UserItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_team_user)

        setSupportActionBar(toolbarRemoveTeamUser)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        removeUsersRecyclerView.layoutManager = LinearLayoutManager(this)
        removeUsersRecyclerView.adapter = adapter

        getUsers()
    }

    private fun getUsers(){

        removeUsersProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getUsers(teamId){ response ->
            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> loadUsers(it.users)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getUsers:failure")
                removeUsersProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun loadUsers(users: List<UserResponse>){

        for (user in users){
            arrayList.add(UserItem(user))
        }
        adapter.addAll(arrayList)
        adapter.setOnItemClickListener { item, view ->
            val userItem = item as UserItem
            deleteUser(userItem.user)
        }
        removeUsersProgressBar.visibility = View.INVISIBLE
    }

    private fun deleteUser(user: UserResponse){

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Remove")
        builder.setMessage("Are you sure you want to remove ${user.username} from the team?")

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

        removeUsersProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().removeUserFromTeam(teamId, userId){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.REMOVED.status -> {
                        removeUsersProgressBar.visibility = View.INVISIBLE
                        Toast.makeText(this, "User removed", Toast.LENGTH_SHORT).show()
                        adapter.removeAll(arrayList)
                        arrayList.clear()
                        getUsers()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "deleteUserFromChannel:failure")
                errorOccurred(null)
                removeUsersProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun errorOccurred(error: String?){

        removeUsersProgressBar.visibility = View.INVISIBLE

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
