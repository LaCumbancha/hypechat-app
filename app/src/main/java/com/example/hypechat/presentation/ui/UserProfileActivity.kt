package com.example.hypechat.presentation.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.rest.response.UserResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : AppCompatActivity() {

    companion object {
        val USERID = "USERID"
        val USERNAME = "USERNAME"
    }

    private val TAG = "User Profile"
    private var userId: Int? = null
    private var userResponse: UserResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        toolbarUserProfile.title = intent.getStringExtra(USERNAME)
        setSupportActionBar(toolbarUserProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        userId = intent.getIntExtra(USERID, 0)
        getUserData()
    }

    private fun getUserData(){
        userProfileProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()
        userProfileCardView.visibility = View.INVISIBLE

        HypechatRepository().getUserProfile(teamId, userId!!){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.ACTIVE.status -> setUserData(it.user)
                    //ServerStatus.WRONG_TOKEN.status -> tokenFailed(it.message)
                    //ServerStatus.TEAM_NOT_FOUND.status -> loadingTeamsFailed(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getUserProfile:failure")
                userProfileProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun setUserData(user: UserResponse){

        userResponse = user
        userProfileCardView.visibility = View.VISIBLE
        userProfileProgressBar.visibility = View.INVISIBLE
        firstNameTextInputLayout.editText!!.setText(user.first_name)
        lastNameTextInputLayout.editText!!.setText(user.last_name)
        userNameTextInputLayout.editText!!.setText(user.username)
        roleTextInputLayout.editText!!.setText(user.role)
        Picasso.get().load(user.profile_pic)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.profile_placeholder)
            .into(userProfileImageView)
    }

    fun viewTeams(view: View){

        val intent = Intent(view.context, ViewUserTeamsActivity::class.java)
        userResponse?.let {
            intent.putExtra(ViewUserTeamsActivity.USER, it)
        }
        startActivity(intent)
    }

    override fun onBackPressed() {

        val intent = Intent()
        intent.putExtra(USERID, userId!!)
        setResult(Activity.RESULT_OK, intent)
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
