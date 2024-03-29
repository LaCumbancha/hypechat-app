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
import androidx.appcompat.app.AlertDialog
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_new_team.*
import java.util.*

class NewTeamActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "NewTeam"
    private val REQUEST_CODE_ASK_PERMISSIONS = 123
    private val REQUEST_IMAGE_PICK = 1
    private var currentPhotoPath: String = ""
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_team)

        setSupportActionBar(toolbarNewTeam)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)
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

    fun selectTeamImageFromDevice(view: View) {
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
            newTeamImageView.setImageBitmap(bitmap)
            newTeamPictureButton.alpha = 0f
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

    fun createTeam(view: View){

        if (validateField(newTeamNameTextInputLayout) && selectedPhotoUri != null){

            loadingScreen()
            val teamName = newTeamNameTextInputLayout.editText!!.text.toString()
            val location = newTeamLocationTextInputLayout.editText!!.text.toString()
            val description = newTeamDescriptionTextInputLayout.editText!!.text.toString()
            val welcomeMessage = newTeamWelcomeMessageTextInputLayout.editText!!.text.toString()

            saveProfilePicture(teamName, location, description, welcomeMessage)
        } else if (selectedPhotoUri == null){

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Error")
            builder.setMessage("Please, select a picture")
            builder.setPositiveButton("Ok", null)
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun saveProfilePicture(teamName: String, location: String?, description: String?, welcomeMessage: String?){

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded profile picture: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener { uri ->
                    Log.d(TAG, "File location: $uri")
                    val profilePicUrl = uri.toString()
                    create(teamName, location, description, welcomeMessage, profilePicUrl)
                }
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to upload profile picture to Storage", it.cause)
                create(teamName, location, description, welcomeMessage, null)
            }
    }

    private fun create(teamName: String, location: String?, description: String?, welcomeMessage: String?, profilePicUrl: String?){

        HypechatRepository().createTeam(teamName, location, description, welcomeMessage, profilePicUrl){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.CREATED.status -> navigateToLatestMessages(it.team.team_id)
                    ServerStatus.ALREADY_REGISTERED.status -> creationFailed(it.message)
                    ServerStatus.WRONG_TOKEN.status -> creationFailed(it.message)
                    ServerStatus.ERROR.status -> creationFailed(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "registerUser:failure")
                errorOccurred(null)
            }
        }
    }

    private fun navigateToLatestMessages(teamId: Int){

        Log.d(TAG, "createTeam:success")
        AppPreferences.setTeamId(teamId)
        val intent = Intent(this, LatestMessagesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        newTeamProgressBar.visibility = View.INVISIBLE
        subscribeToFcm()
    }

    private fun subscribeToFcm(){

        val teamId = AppPreferences.getTeamId()
        FirebaseMessaging.getInstance().subscribeToTopic(teamId.toString())
            .addOnCompleteListener { task ->
                var msg = "Subscribed to $teamId Successfully"
                if (!task.isSuccessful) {
                    msg = "Subscribe to $teamId failed"
                }
                Log.d(TAG, msg)
            }
    }

    private fun loadingScreen(){
        newTeamProgressBar.visibility = View.VISIBLE
        newTeamCardView.visibility = View.GONE
    }

    private fun errorOccurred(error: String?){
        newTeamProgressBar.visibility = View.INVISIBLE
        newTeamCardView.visibility = View.VISIBLE

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Error")
        var msg = "There was a problem during the registration process. Please, try again."
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

    private fun creationFailed(msg: String){

        newTeamProgressBar.visibility = View.INVISIBLE
        newTeamCardView.visibility = View.VISIBLE
        Log.w(TAG, msg)

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(msg)

        builder.setPositiveButton("Ok"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}
