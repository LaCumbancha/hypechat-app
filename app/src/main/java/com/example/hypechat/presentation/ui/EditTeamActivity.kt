package com.example.hypechat.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import kotlinx.android.synthetic.main.activity_edit_team.*

class EditTeamActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_team)

        setSupportActionBar(toolbarEditTeam)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)
    }
}
