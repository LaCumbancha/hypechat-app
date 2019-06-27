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
import com.example.hypechat.data.model.rest.response.UserResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.util.*

class MyProfileActivity : AppCompatActivity() {

    private val TAG = "My Profile"
    private val REQUEST_CODE_ASK_PERMISSIONS = 123
    private val REQUEST_IMAGE_PICK = 1
    private var currentPhotoPath: String = ""
    private var selectedPhotoUri: Uri? = null
    private var userData: UserResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setSupportActionBar(toolbarMyProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        getUserData()
    }

    private fun getUserData(){
        myProfileProgressBar.visibility = View.VISIBLE
        myProfileCardView.visibility = View.INVISIBLE

        HypechatRepository().getMyProfile{ response ->

            response?.let {

                when (it.status){
                    ServerStatus.ACTIVE.status -> setUserData(it.user)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getUserProfile:failure")
                myProfileProgressBar.visibility = View.INVISIBLE
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

    private fun setUserData(user: UserResponse){

        userData = user
        myProfileProgressBar.visibility = View.INVISIBLE
        myProfileCardView.visibility = View.VISIBLE
        firstNameTextInputLayout.editText!!.setText(user.first_name)
        lastNameTextInputLayout.editText!!.setText(user.last_name)
        userNameTextInputLayout.editText!!.setText(user.username)
        registerEmailTextInputLayout.editText!!.setText(user.email)
        Picasso.get().load(user.profile_pic)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.profile_placeholder)
            .into(myProfileImageView)
        myProfilePictureButton.alpha = 0f
    }

    fun editProfile(view: View){

        myProfileEditButton.visibility = View.INVISIBLE
        myProfileSaveButton.visibility = View.VISIBLE
        myProfileCancelButton.visibility = View.VISIBLE
        myProfilePictureButton.alpha = 1f
        myProfilePictureButton.isEnabled = true
        firstNameTextInputLayout.editText!!.isFocusable = true
        firstNameTextInputLayout.editText!!.isFocusableInTouchMode = true
        lastNameTextInputLayout.editText!!.isFocusable = true
        lastNameTextInputLayout.editText!!.isFocusableInTouchMode = true
        userNameTextInputLayout.editText!!.isFocusable = true
        userNameTextInputLayout.editText!!.isFocusableInTouchMode = true
        registerEmailTextInputLayout.editText!!.isFocusable = true
        registerEmailTextInputLayout.editText!!.isFocusableInTouchMode = true
    }

    fun cancelEdit(view: View){

        myProfileEditButton.visibility = View.VISIBLE
        myProfileSaveButton.visibility = View.INVISIBLE
        myProfileCancelButton.visibility = View.INVISIBLE
        myProfilePictureButton.alpha = 0f
        myProfilePictureButton.isEnabled = false
        firstNameTextInputLayout.editText!!.isFocusable = false
        firstNameTextInputLayout.editText!!.isFocusableInTouchMode = false
        lastNameTextInputLayout.editText!!.isFocusable = false
        lastNameTextInputLayout.editText!!.isFocusableInTouchMode = false
        userNameTextInputLayout.editText!!.isFocusable = false
        userNameTextInputLayout.editText!!.isFocusableInTouchMode = false
        registerEmailTextInputLayout.editText!!.isFocusable = false
        registerEmailTextInputLayout.editText!!.isFocusableInTouchMode = false
    }

    fun selectProfileImageFromDevice(view: View) {
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
            myProfileImageView.setImageBitmap(bitmap)
            myProfilePictureButton.alpha = 0f
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

    fun saveProfile(view: View){

        if (validateField(userNameTextInputLayout) && validateField(registerEmailTextInputLayout)){

            myProfileCardView.visibility = View.INVISIBLE
            myProfileProgressBar.visibility = View.VISIBLE
            val username = userNameTextInputLayout.editText!!.text.toString()
            val email = registerEmailTextInputLayout.editText!!.text.toString()

            var firstName: String? = null
            if (firstNameTextInputLayout.editText!!.text.toString().isNotBlank()){
                firstName = firstNameTextInputLayout.editText!!.text.toString()
            }

            var lastName: String? = null
            if (lastNameTextInputLayout.editText!!.text.toString().isNotBlank()){
                lastName = lastNameTextInputLayout.editText!!.text.toString()
            }

            saveProfilePicture(username, email, firstName, lastName)
        }
    }

    private fun save(username: String, email: String, firstName: String?, lastName: String?, profilePic: String?){

        var userPic: String? = userData!!.profile_pic

        selectedPhotoUri?.let {
            userPic = profilePic
        }

        HypechatRepository().updateMyProfile(username, email, firstName, lastName, userPic){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.ACTIVE.status -> {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                        myProfileProgressBar.visibility = View.INVISIBLE
                        myProfileCardView.visibility = View.VISIBLE
                        cancel()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "updateMyProfile:failure")
                errorOccurred(null)
                myProfileProgressBar.visibility = View.INVISIBLE
                myProfileCardView.visibility = View.VISIBLE
            }
        }
    }

    private fun saveProfilePicture(username: String, email: String, firstName: String?, lastName: String?){

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        if (selectedPhotoUri != null){
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully uploaded team picture: ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        Log.d(TAG, "File location: $uri")
                        val profilePicUrl = uri.toString()
                        save(username, email, firstName, lastName, profilePicUrl)
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Failed to upload team picture to Storage", it.cause)
                    save(username, email, firstName, lastName, userData!!.profile_pic)
                }
        } else {
            save(username, email, firstName, lastName, userData!!.profile_pic)
        }
    }

    private fun errorOccurred(error: String?){
        myProfileProgressBar.visibility = View.INVISIBLE

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

    private fun cancel(){
        myProfileEditButton.visibility = View.VISIBLE
        myProfileSaveButton.visibility = View.INVISIBLE
        myProfileCancelButton.visibility = View.INVISIBLE
        myProfilePictureButton.alpha = 0f
        myProfilePictureButton.isEnabled = false
        firstNameTextInputLayout.editText!!.isFocusable = false
        firstNameTextInputLayout.editText!!.isFocusableInTouchMode = false
        lastNameTextInputLayout.editText!!.isFocusable = false
        lastNameTextInputLayout.editText!!.isFocusableInTouchMode = false
        userNameTextInputLayout.editText!!.isFocusable = false
        userNameTextInputLayout.editText!!.isFocusableInTouchMode = false
        registerEmailTextInputLayout.editText!!.isFocusable = false
        registerEmailTextInputLayout.editText!!.isFocusableInTouchMode = false
    }
}
