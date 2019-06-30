package com.example.hypechat.presentation.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_update_password.*

class UpdatePasswordActivity : AppCompatActivity() {

    companion object {
        val EMAIL = "EMAIL"
    }

    private val TAG = "Update Password"
    private lateinit var auth: FirebaseAuth
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_password)

        setSupportActionBar(toolbarUpdatePassword)
        AppPreferences.init(this)
        auth = FirebaseAuth.getInstance()
        userEmail = intent.getStringExtra(EMAIL)
    }

    private fun validateField(field: TextInputLayout): Boolean {

        val fieldStr = field.editText!!.text.toString().trim()
        val length = field.editText!!.text.toString().length

        when {
            fieldStr.isEmpty() -> {
                field.error = "This field can not be empty"
                return false
            }
            length < 6 -> {
                field.error = "Password must have at least 6 characters"
                return false
            }
            else -> {
                field.error = null
                return true
            }
        }
    }

    fun updatePassword(view: View){

        if (validateField(passwordUpdatePasswordTextInputLayout)){

            updatePasswordProgressBar.visibility = View.VISIBLE
            val password = passwordUpdatePasswordTextInputLayout.editText!!.text.toString()
            view.hideKeyboard()

            HypechatRepository().updatePassword(password){ response ->

                response?.let {

                    when (it.status){
                        ServerStatus.ACTIVE.status -> firebaseLogin(password)
                        ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                        ServerStatus.ERROR.status -> errorOccurred(it.message)
                        else -> errorOccurred(it.message)
                    }
                }
                if (response == null){
                    Log.w(TAG, "updatePassword:failure")
                    errorOccurred(null)
                    updatePasswordProgressBar.visibility = View.INVISIBLE
                }
            }
        }
    }

    fun View.hideKeyboard(){

        val inm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun firebaseLogin(password: String){

        Log.d(TAG, "updatePassword:success")

         auth.signInWithEmailAndPassword(userEmail!!, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                    updatePasswordProgressBar.visibility = View.INVISIBLE
                    Log.d(TAG, "Firebase:signInWithEmail:success")
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                }
            }
    }

    private fun errorOccurred(error: String?){

        updatePasswordProgressBar.visibility = View.INVISIBLE

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
