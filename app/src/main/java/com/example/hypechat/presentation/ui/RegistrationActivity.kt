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

            var firstName: String? = null
            var lastName: String? = null
            var profilePicUrl: String? = null
            firstNameTextInputLayout.editText?.let {
                firstName = it.text.toString()
            }
            lastNameTextInputLayout.editText?.let {
                lastName = it.text.toString()
            }
            val username = userNameTextInputLayout.editText!!.text.toString()
            val email = registerEmailTextInputLayout.editText!!.text.toString()
            val password = registerPasswordTextInputLayout.editText!!.text.toString()

            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully uploaded profile picture: ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        Log.d(TAG, "File location: $uri")
                        profilePicUrl = uri.toString()
                        register(username, email, password, firstName, lastName, profilePicUrl)
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Failed to upload profile picture to Storage", it.cause)
                    register(username, email, password, firstName, lastName, profilePicUrl)
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
                //verificar si el user es null o no. si es null mostrar message de error
                Log.d(TAG, "signInWithEmail:success")
                Toast.makeText(this, "signInWithEmail:success: ${it.status}", Toast.LENGTH_SHORT).show()
                AppPreferences.setToken(it.user.token)
                AppPreferences.setUserName(it.user.username)
                //setear en app preferences el username
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            if (response == null){
                Toast.makeText(this, "Authentication failed: signInWithEmail:failure", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfilePicture(){

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded profile picture: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "File location: $it")
                    saveUser(it.toString())
                }
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to upload profile picture to Storage", it.cause)
            }
    }

    private fun saveUser(profilePictureUrl: String){

        val uid = auth.uid
        val fullName = userNameTextInputLayout.editText!!.text.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val newUser = User(uid!!, fullName, profilePictureUrl)

        ref.setValue(newUser)
            .addOnSuccessListener {
                Log.d(TAG, "User saved to Database")
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to save user to Database", it.cause)
            }
    }
}
