package com.example.hypechat.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.TeamStatRow
import com.example.hypechat.data.model.rest.response.TeamResponse
import com.example.hypechat.data.model.rest.response.UserResponse
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_view_user_teams.*

class ViewUserTeamsActivity : AppCompatActivity() {

    companion object {
        val USER = "USER"
    }

    private val TAG = "View User Teams"
    private var user: UserResponse? = null
    private val adapter = GroupAdapter<ViewHolder>()
    private var teamStatList = ArrayList<TeamStatRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_user_teams)

        user = intent.getSerializableExtra(USER) as UserResponse?
        toolbarViewUserTeams.title = "${user?.username} Teams"

        setSupportActionBar(toolbarViewUserTeams)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        viewUserTeamsRecyclerView.layoutManager = LinearLayoutManager(this)
        viewUserTeamsRecyclerView.adapter = adapter
        viewUserTeamsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        user?.let {
            initializeList(it.teams)
        }
    }

    private fun initializeList(teams: List<TeamResponse>){

        for (team in teams){
            teamStatList.add(TeamStatRow(team.t_name, team.role, team.messages))
        }
        adapter.addAll(teamStatList)
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
