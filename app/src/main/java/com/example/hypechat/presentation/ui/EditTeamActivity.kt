package com.example.hypechat.presentation.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.rest.response.TeamResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.example.hypechat.data.rest.utils.UserRole
import com.example.hypechat.presentation.utils.TeamInvitationDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_team.*
import java.util.*

class EditTeamActivity : AppCompatActivity(), TeamInvitationDialog.TeamInvitationListener {

    companion object {
        val TEAM = "TEAM"
    }

    private var team: TeamResponse? = null
    private val TAG = "Edit Teams"
    private lateinit var auth: FirebaseAuth
    private val REQUEST_CODE_ASK_PERMISSIONS = 123
    private val REQUEST_IMAGE_PICK = 1
    private var currentPhotoPath: String = ""
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_team)

        team = intent.getSerializableExtra(TEAM) as TeamResponse?
        toolbarEditTeam.title = team?.team_name

        setSupportActionBar(toolbarEditTeam)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        setTeamData()
    }

    private fun setTeamData(){

        team?.let { team ->

            editTeamNameTextInputLayout.editText!!.setText(team.team_name)
            team.description?.let {
                editTeamDescriptionTextInputLayout.editText!!.setText(it)
            }
            team.welcome_message.let {
                editTeamWelcomeMessageTextInputLayout.editText!!.setText(it)
            }
            team.location?.let {
                editTeamLocationTextInputLayout.editText!!.setText(it)
            }
            team.picture?.let {
                Picasso.get().load(it)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.profile_placeholder)
                    .into(editTeamImageView)
                editTeamPictureButton.alpha = 0f
            }

            checkIfUserIsSwitchInCurrentTeam()
        }
    }

    private fun checkIfUserIsSwitchInCurrentTeam(){

        val teamId = AppPreferences.getTeamId()

        team?.let { team ->

            if (team.team_id == teamId){
                if (team.role == UserRole.CREATOR.role){
                    inviteTeamButton.visibility = View.VISIBLE
                    deleteTeamButton.visibility = View.VISIBLE
                    editTeamButton.visibility = View.VISIBLE
                    removeTeamUserButton.visibility = View.VISIBLE
                    teamForbiddenWordsButton.visibility = View.VISIBLE
                } else {
                    leaveTeamButton.visibility = View.VISIBLE
                }
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

    fun viewForbiddenWords(view: View){

        val intent = Intent(this, ForbiddenWordsActivity::class.java)
        startActivity(intent)
    }

    fun switchTeam(view: View){

        val intent = Intent(this, LatestMessagesActivity::class.java)
        team?.let {
            intent.putExtra(LatestMessagesActivity.TEAM_ID, it.team_id)
            subscribeToFcm(it.team_id)
        }
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun removeTeamUser(view: View){

        val intent = Intent(this, RemoveTeamUserActivity::class.java)
        startActivity(intent)
    }

    fun editTeam(view: View){

        editTeamButton.visibility = View.INVISIBLE
        cancelTeamButton.visibility = View.VISIBLE
        saveTeamButton.visibility = View.VISIBLE
        editTeamPictureButton.alpha = 1f
        editTeamPictureButton.isEnabled = true
        editTeamNameTextInputLayout.editText!!.isFocusable = true
        editTeamNameTextInputLayout.editText!!.isFocusableInTouchMode = true
        editTeamDescriptionTextInputLayout.editText!!.isFocusable = true
        editTeamDescriptionTextInputLayout.editText!!.isFocusableInTouchMode = true
        editTeamWelcomeMessageTextInputLayout.editText!!.isFocusable = true
        editTeamWelcomeMessageTextInputLayout.editText!!.isFocusableInTouchMode = true
        editTeamLocationTextInputLayout.editText!!.isFocusable = true
        editTeamLocationTextInputLayout.editText!!.isFocusableInTouchMode = true
    }

    fun cancelEditTeam(view: View){

        editTeamButton.visibility = View.VISIBLE
        saveTeamButton.visibility = View.INVISIBLE
        cancelTeamButton.visibility = View.INVISIBLE
        editTeamPictureButton.alpha = 0f
        editTeamPictureButton.isEnabled = false
        editTeamNameTextInputLayout.editText!!.isFocusable = false
        editTeamNameTextInputLayout.editText!!.isFocusableInTouchMode = false
        editTeamDescriptionTextInputLayout.editText!!.isFocusable = false
        editTeamDescriptionTextInputLayout.editText!!.isFocusableInTouchMode = false
        editTeamWelcomeMessageTextInputLayout.editText!!.isFocusable = false
        editTeamWelcomeMessageTextInputLayout.editText!!.isFocusableInTouchMode = false
        editTeamLocationTextInputLayout.editText!!.isFocusable = false
        editTeamLocationTextInputLayout.editText!!.isFocusableInTouchMode = false
    }

    fun saveChangesTeam(view: View){

        if (validateField(editTeamNameTextInputLayout)){

            editTeamCardView.visibility = View.INVISIBLE
            editTeamProgressBar.visibility = View.VISIBLE
            val teamName = editTeamNameTextInputLayout.editText!!.text.toString()

            var description: String? = null
            if (editTeamDescriptionTextInputLayout.editText!!.text.toString().isNotBlank()){
                description = editTeamDescriptionTextInputLayout.editText!!.text.toString()
            }
            var welcomeMessage: String? = null
            if (editTeamWelcomeMessageTextInputLayout.editText!!.text.toString().isNotBlank()){
                welcomeMessage = editTeamWelcomeMessageTextInputLayout.editText!!.text.toString()
            }
            var teamLocation: String? = null
            if (editTeamLocationTextInputLayout.editText!!.text.toString().isNotBlank()){
                teamLocation = editTeamLocationTextInputLayout.editText!!.text.toString()
            }

            saveTeamPicture(teamName, teamLocation, description, welcomeMessage)
        }
    }

    private fun save(teamName: String, teamLocation: String?, description: String?, welcomeMessage: String?, teamPicUrl: String?){

        var teamPic: String? = team!!.picture

        selectedPhotoUri?.let {
            teamPic = teamPicUrl
        }
        
        HypechatRepository().updateTeam(team!!.team_id, teamName, teamLocation, description, welcomeMessage, teamPic){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.UPDATED.status -> {
                        Toast.makeText(this, "Team information updated", Toast.LENGTH_SHORT).show()
                        editTeamProgressBar.visibility = View.INVISIBLE
                        editTeamCardView.visibility = View.VISIBLE
                        cancel()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "deleteTeam:failure")
                errorOccurred(null)
                editTeamProgressBar.visibility = View.INVISIBLE
                editTeamCardView.visibility = View.VISIBLE
            }
        }
    }

    fun selectImageFromDevice(view: View) {
        val hasReadExternalStoragePermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_ASK_PERMISSIONS)
            return
        }
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_PICK && data != null){
            Log.d(TAG, "Photo was selected")
            selectedPhotoUri = data.data
            currentPhotoPath = getRealPathFromUri(selectedPhotoUri!!)
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            editTeamImageView.setImageBitmap(bitmap)
            editTeamPictureButton.alpha = 0f
        }
    }

    private fun getRealPathFromUri(contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(contentUri, proj, null, null, null)
            assert(cursor != null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }

    private fun saveTeamPicture(teamName: String, teamLocation: String?, description: String?, welcomeMessage: String?){

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        if (selectedPhotoUri != null){
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully uploaded team picture: ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        Log.d(TAG, "File location: $uri")
                        val profilePicUrl = uri.toString()
                        save(teamName, teamLocation, description, welcomeMessage, profilePicUrl)
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Failed to upload team picture to Storage", it.cause)
                    save(teamName, teamLocation, description, welcomeMessage, team!!.picture)
                }
        } else {
            save(teamName, teamLocation, description, welcomeMessage, team!!.picture)
        }
    }

    fun deleteTeam(view: View){

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Delete Team")
        builder.setMessage("Are you sure you want to delete the team?")

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

        editTeamCardView.visibility = View.INVISIBLE
        editTeamProgressBar.visibility = View.VISIBLE

        HypechatRepository().deleteTeam(team!!.team_id){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.REMOVED.status -> {
                        unsubscribeToFcm(team!!.team_id)
                        navigateToTeams()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "deleteTeam:failure")
                errorOccurred(null)
                editTeamProgressBar.visibility = View.INVISIBLE
                editTeamCardView.visibility = View.VISIBLE
            }
        }
    }

    private fun errorOccurred(error: String?){
        editTeamProgressBar.visibility = View.INVISIBLE
        editTeamCardView.visibility = View.VISIBLE

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

    private fun navigateToTeams(){
        editTeamProgressBar.visibility = View.INVISIBLE

        val intent = Intent(this, TeamsActivity::class.java)
        //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun inviteUser(view: View){

        val teamInvitationDialog = TeamInvitationDialog()
        teamInvitationDialog.show(supportFragmentManager, TAG)
    }

    override fun applyEmail(email: String) {
        editTeamCardView.visibility = View.INVISIBLE
        editTeamProgressBar.visibility = View.VISIBLE

        HypechatRepository().inviteUserToTeam(team!!.team_id, email){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.INVITED.status -> {
                        Toast.makeText(this, "Invitation sent", Toast.LENGTH_SHORT).show()
                        editTeamProgressBar.visibility = View.INVISIBLE
                        editTeamCardView.visibility = View.VISIBLE
                    }
                    ServerStatus.ALREADY_INVITED.status -> errorOccurred(it.message)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "inviteUserToTeam:failure")
                errorOccurred(null)
                editTeamProgressBar.visibility = View.INVISIBLE
                editTeamCardView.visibility = View.VISIBLE
            }
        }
    }

    private fun unsubscribeToFcm(teamId: Int){

        FirebaseMessaging.getInstance().unsubscribeFromTopic(teamId.toString())
            .addOnCompleteListener { task ->
                var msg = "Unsubscribed from $teamId Successfully"
                if (!task.isSuccessful) {
                    msg = "Unsubscribe from $teamId failed"
                }
                Log.d(TAG, msg)
            }
    }

    private fun subscribeToFcm(teamId: Int){

        FirebaseMessaging.getInstance().subscribeToTopic(teamId.toString())
            .addOnCompleteListener { task ->
                var msg = "Subscribed to $teamId Successfully"
                if (!task.isSuccessful) {
                    msg = "Subscribe to $teamId failed"
                }
                Log.d(TAG, msg)
            }
    }

    fun leaveTeam(view: View){

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Leave Team")
        builder.setMessage("Are you sure you want to leave the team?")

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
        editTeamCardView.visibility = View.INVISIBLE
        editTeamProgressBar.visibility = View.VISIBLE

        HypechatRepository().leaveTeam(team!!.team_id){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.REMOVED.status -> {
                        unsubscribeToFcm(team!!.team_id)
                        navigateToTeams()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "leaveTeam:failure")
                errorOccurred(null)
                editTeamProgressBar.visibility = View.INVISIBLE
                editTeamCardView.visibility = View.VISIBLE
            }
        }
    }

    private fun cancel(){

        editTeamButton.visibility = View.VISIBLE
        saveTeamButton.visibility = View.INVISIBLE
        cancelTeamButton.visibility = View.INVISIBLE
        editTeamPictureButton.alpha = 0f
        editTeamPictureButton.isEnabled = false
        editTeamNameTextInputLayout.editText!!.isFocusable = false
        editTeamNameTextInputLayout.editText!!.isFocusableInTouchMode = false
        editTeamDescriptionTextInputLayout.editText!!.isFocusable = false
        editTeamDescriptionTextInputLayout.editText!!.isFocusableInTouchMode = false
        editTeamWelcomeMessageTextInputLayout.editText!!.isFocusable = false
        editTeamWelcomeMessageTextInputLayout.editText!!.isFocusableInTouchMode = false
        editTeamLocationTextInputLayout.editText!!.isFocusable = false
        editTeamLocationTextInputLayout.editText!!.isFocusableInTouchMode = false
    }
}
