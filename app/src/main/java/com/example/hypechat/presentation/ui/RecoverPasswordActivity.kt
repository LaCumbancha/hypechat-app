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
import com.example.hypechat.data.model.rest.response.UserResponse
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.example.hypechat.presentation.utils.RegeneratePasswordDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_recover_password.*

class RecoverPasswordActivity : AppCompatActivity(), RegeneratePasswordDialog.RegeneratePasswordListener {

    private val TAG = "Recover Password"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        setSupportActionBar(toolbarRecoverPassword)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        auth = FirebaseAuth.getInstance()
    }

    private fun validateField(field: TextInputLayout): Boolean {

        val fieldStr = field.editText!!.text.toString().trim()

        if (fieldStr.isEmpty()){
            field.error = "This field can not be empty"
            return false
        } else {
            field.error = null
            return true
        }
    }

    private fun loadingScreen(){
        recoverPasswordProgressBar.visibility = View.VISIBLE
        emailRecoverPasswordTextInputLayout.visibility = View.INVISIBLE
        getRecoveryTokenButton.visibility = View.INVISIBLE
        enterTokenButton.visibility = View.INVISIBLE
    }

    private fun showScreen(){
        recoverPasswordProgressBar.visibility = View.INVISIBLE
        emailRecoverPasswordTextInputLayout.visibility = View.VISIBLE
        getRecoveryTokenButton.visibility = View.VISIBLE
        enterTokenButton.visibility = View.VISIBLE
    }

    fun getRecoveryToken(view: View){

        if (validateField(emailRecoverPasswordTextInputLayout)){

            loadingScreen()
            view.hideKeyboard()
            val email = emailRecoverPasswordTextInputLayout.editText!!.text.toString()
            emailRecoverPasswordTextInputLayout.editText!!.text.clear()

            HypechatRepository().recoverPassword(email){ response ->

                response?.let {

                    when (it.status){
                        ServerStatus.OK.status -> sendPasswordResetEmail(email)
                        ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                        ServerStatus.ERROR.status -> errorOccurred(it.message)
                    }
                }
                if (response == null){
                    Log.w(TAG, "recoverPassword:failure")
                    errorOccurred(null)
                    showScreen()
                }
            }
        }
    }

    private fun sendPasswordResetEmail(email: String){

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "sendPasswordResetEmail:success")
                    Toast.makeText(this, "Recovery token sent", Toast.LENGTH_SHORT).show()
                    showScreen()
                } else {
                    Log.w(TAG, "sendPasswordResetEmail:failure", task.exception)
                    showScreen()
                    val error = task.exception.toString().split(": ")
                    val msg = error[1]
                    errorOccurred(msg)
                }
            }
    }

    fun enterToken(view: View){

        val regeneratePasswordDialog = RegeneratePasswordDialog()
        regeneratePasswordDialog.show(supportFragmentManager, TAG)
    }

    override fun applyData(email: String, token: String) {

        loadingScreen()

        HypechatRepository().regeneratePassword(email, token){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.ACTIVE.status -> navigateToUpdatePassword(it.user)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "recoverPassword:failure")
                errorOccurred(null)
                showScreen()
            }
        }
    }

    fun View.hideKeyboard(){

        val inm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun navigateToUpdatePassword(user: UserResponse){

        AppPreferences.setUserId(user.id)
        AppPreferences.setUserName(user.username)
        Log.d(TAG, "regeneratePassword:success")
        val intent = Intent(this, UpdatePasswordActivity::class.java)
        intent.putExtra(UpdatePasswordActivity.EMAIL, user.email)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        showScreen()
    }

    private fun errorOccurred(error: String?){
        showScreen()

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
