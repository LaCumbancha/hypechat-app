package com.example.hypechat.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.rest.response.TeamResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import kotlinx.android.synthetic.main.activity_teams.*

class TeamsActivity : AppCompatActivity() {

    private val TAG = "Teams"
    private val latestMessagesList = mutableListOf<TeamResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teams)

        setSupportActionBar(toolbarTeams)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        initializeTeams()
    }

    fun newTeam(view: View){
        //val intent = Intent(this, NewTeamActivity::class.java)
        //startActivity(intent)
    }

    private fun initializeTeams(){
        teamsProgressBar.visibility = View.VISIBLE

        HypechatRepository().getTeams{ response ->

            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> setTeams(it.teams)
                    //ServerStatus.WRONG_TOKEN.status -> tokenFailed(it.message)
                    ServerStatus.TEAM_NOT_FOUND.status -> loadingChatsFailed(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getUsers:failure")
                teamsProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun setTeams(teams: List<TeamResponse>){

        for (chat in teams){
            latestMessagesList.add(chat)
        }
        Log.d(TAG, "getChatsPreviews:success")
        //refreshLatestMessagesRecyclerView()
        teamsProgressBar.visibility = View.INVISIBLE
    }

    private fun loadingChatsFailed(msg: String){

        teamsProgressBar.visibility = View.INVISIBLE

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(msg)

        builder.setPositiveButton("Refresh"){ dialog, which ->
            dialog.dismiss()
            initializeTeams()
        }
        builder.setNegativeButton("Cancel"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}
