package com.example.hypechat.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.TeamRow
import com.example.hypechat.data.model.rest.response.TeamResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.example.hypechat.presentation.utils.JoinTeamDialog
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_teams.*

class TeamsActivity : AppCompatActivity(), JoinTeamDialog.TeamTokenListener {

    private val TAG = "Teams"
    private var teamList = mutableListOf<TeamResponse>()
    private val teamAdapter = GroupAdapter<ViewHolder>()
    private var fab_open: Animation? = null
    private var fab_close: Animation? = null
    private var fab_clock: Animation? = null
    private var fab_anticlock: Animation? = null
    private var isOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teams)

        setSupportActionBar(toolbarTeams)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)
        loadAnimations()

        teamsRecyclerView.layoutManager = LinearLayoutManager(this)
        teamsRecyclerView.adapter = teamAdapter
        teamsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        initializeTeams()
        setAdapterOnItemClickListener()
    }

    fun newTeam(view: View){
        closeNew()
        val intent = Intent(this, NewTeamActivity::class.java)
        startActivity(intent)
    }

    fun joinTeam(view: View){
        closeNew()
        val joinTeamDialog = JoinTeamDialog()
        joinTeamDialog.show(supportFragmentManager, TAG)
    }

    override fun applyToken(token: String) {

        teamsRecyclerView.visibility = View.INVISIBLE
        teamsProgressBar.visibility = View.VISIBLE

        HypechatRepository().joinTeam(token){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.ADDED.status -> {
                        Toast.makeText(this, "Joined team successfully", Toast.LENGTH_SHORT).show()
                        initializeTeams()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "joinTeam:failure")
                errorOccurred(null)
                teamsProgressBar.visibility = View.INVISIBLE
                teamsRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    fun openNew(view: View){

        if (isOpen) {
            closeNew()

        } else {
            newTeamTextView.visibility = View.VISIBLE
            joinTeamTextView.visibility = View.VISIBLE
            fabJoinTeam.startAnimation(fab_open)
            fabNewTeam.startAnimation(fab_open)
            fabNew.startAnimation(fab_clock)
            fabJoinTeam.isClickable = true
            fabNewTeam.isClickable = true
            isOpen = true
        }
    }

    private fun closeNew(){
        newTeamTextView.visibility = View.INVISIBLE
        joinTeamTextView.visibility = View.INVISIBLE
        fabJoinTeam.startAnimation(fab_close)
        fabNewTeam.startAnimation(fab_close)
        fabNew.startAnimation(fab_anticlock)
        fabJoinTeam.isClickable = false
        fabNewTeam.isClickable =false
        isOpen = false
    }

    private fun loadAnimations(){
        fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fab_close = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        fab_clock = AnimationUtils.loadAnimation(this, R.anim.fab_rotate_clock)
        fab_anticlock = AnimationUtils.loadAnimation(this, R.anim.fab_rotate_anticlock)
    }

    private fun initializeTeams(){
        teamsProgressBar.visibility = View.VISIBLE

        HypechatRepository().getTeams{ response ->

            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> setTeams(it.teams)
                    //ServerStatus.WRONG_TOKEN.status -> tokenFailed(it.message)
                    ServerStatus.TEAM_NOT_FOUND.status -> loadingTeamsFailed(it.message)
                    else -> errorOccurred(it.message)
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
            val intent = Intent(this, EditTeamActivity::class.java)
            val row = item as TeamRow
            intent.putExtra(EditTeamActivity.TEAM, row.team)
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
            teamList = mutableListOf<TeamResponse>()
            for (team in teams){
                teamList.add(team)
            }
            Log.d(TAG, "getTeams:success")
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

    private fun errorOccurred(error: String?){
        teamsProgressBar.visibility = View.INVISIBLE
        teamsRecyclerView.visibility = View.VISIBLE

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
}
