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
import androidx.appcompat.app.AlertDialog
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.User
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "Registration"
    private val REQUEST_CODE_ASK_PERMISSIONS = 123
    private val REQUEST_IMAGE_PICK = 1
    private var currentPhotoPath: String = ""
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = FirebaseAuth.getInstance()
        AppPreferences.init(this)

        setSupportActionBar(toolbarRegistration)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
            registerProfileImageView.setImageBitmap(bitmap)
            registrationPictureButton.alpha = 0f
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

    fun registerUser(view: View){

        if (validateField(userNameTextInputLayout) && validateField(registerEmailTextInputLayout)
            && validateField(registerPasswordTextInputLayout) && selectedPhotoUri != null){

            loadingScreen()
            var firstName: String? = null
            var lastName: String? = null
            firstNameTextInputLayout.editText?.let {
                firstName = it.text.toString()
            }
            lastNameTextInputLayout.editText?.let {
                lastName = it.text.toString()
            }
            val username = userNameTextInputLayout.editText!!.text.toString()
            val email = registerEmailTextInputLayout.editText!!.text.toString()
            val password = registerPasswordTextInputLayout.editText!!.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        saveProfilePicture(username, email, password, firstName, lastName)
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        val error = task.exception.toString().split(": ")
                        val msg = error[1]
                        errorOccurred(msg)
                    }
                }
        } else if (selectedPhotoUri == null){

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Error")
            builder.setMessage("Please, select a picture")
            builder.setPositiveButton("Ok", null)
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun register(username:String, email: String, password: String, firstName:String?,
                         lastName: String?, profilePicUrl: String?){

        HypechatRepository().registerUser(username, email, password, firstName, lastName, profilePicUrl){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.ACTIVE.status -> navigateToLatestMessages()
                    ServerStatus.ALREADY_REGISTERED.status -> registrationFailed(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "registerUser:failure")
                errorOccurred(null)
            }
        }
    }

    private fun navigateToLatestMessages(){

        Log.d(TAG, "registerUser:success")
        val intent = Intent(this, LatestMessagesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        registrationProgressBar.visibility = View.INVISIBLE
    }

    private fun registrationFailed(msg: String){

        registrationProgressBar.visibility = View.INVISIBLE
        registrationCardView.visibility = View.VISIBLE
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

    private fun saveProfilePicture(username: String, email: String, password: String, firstName: String?, lastName: String?){

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded profile picture: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener { uri ->
                    Log.d(TAG, "File location: $uri")
                    val profilePicUrl = uri.toString()
                    register(username, email, password, firstName, lastName, profilePicUrl)
                }
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to upload profile picture to Storage", it.cause)
                register(username, email, password, firstName, lastName, null)
            }
    }

    private fun loadingScreen(){
        registrationProgressBar.visibility = View.VISIBLE
        registrationCardView.visibility = View.GONE
    }

    private fun errorOccurred(error: String?){
        registrationProgressBar.visibility = View.INVISIBLE
        registrationCardView.visibility = View.VISIBLE

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
}
