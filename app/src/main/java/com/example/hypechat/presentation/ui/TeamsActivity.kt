package com.example.hypechat.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.rest.TeamRow
import com.example.hypechat.data.model.rest.response.TeamResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_teams.*

class TeamsActivity : AppCompatActivity() {

    private val TAG = "Teams"
    private val teamList = mutableListOf<TeamResponse>()
    private val teamAdapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teams)

        setSupportActionBar(toolbarTeams)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        teamsRecyclerView.layoutManager = LinearLayoutManager(this)
        teamsRecyclerView.adapter = teamAdapter
        teamsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        initializeTeams()
        //setAdapterOnItemClickListener()
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
                    ServerStatus.TEAM_NOT_FOUND.status -> loadingTeamsFailed(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getTeams:failure")
                teamsProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun setAdapterOnItemClickListener(){
        teamAdapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, LatestMessagesActivity::class.java)
            val row = item as TeamRow

            //intent.putExtra(LatestMessagesActivity.XXXX, row.team.chatName)
            startActivity(intent)
        }
    }

    private fun refreshTeamsRecyclerView(){
        teamAdapter.clear()
        for (team in teamList){
            teamAdapter.add(TeamRow(team))
        }
    }

    private fun setTeams(teams: List<TeamResponse>){

        if (teams.isNotEmpty()){
            teamsRecyclerView.visibility = View.VISIBLE
            noTeamsImageView.visibility = View.INVISIBLE
            noTeamsTextView.visibility = View.INVISIBLE
            for (team in teams){
                teamList.add(team)
            }
            Log.d(TAG, "getChatsPreviews:success")
            refreshTeamsRecyclerView()
            teamsProgressBar.visibility = View.INVISIBLE
        } else {
            teamsProgressBar.visibility = View.INVISIBLE
            teamsRecyclerView.visibility = View.GONE
            noTeamsImageView.visibility = View.VISIBLE
            noTeamsTextView.visibility = View.VISIBLE
        }
    }

    private fun loadingTeamsFailed(msg: String){

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
