package com.example.hypechat.presentation.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.example.hypechat.data.rest.utils.UserRole
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private val TAG = "Main"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()
        AppPreferences.init(this)
        showScreen()
        loginFacebookButton.setReadPermissions("email", "public_profile", "user_photos")
    }

    fun loginWithFacebook(view: View){
        facebookLoadingScreen()
        loginFacebookButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                Toast.makeText(this@MainActivity, "facebook:onCancel", Toast.LENGTH_SHORT).show()
                showScreen()
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                Toast.makeText(this@MainActivity, "facebook:onError: $error", Toast.LENGTH_SHORT).show()
                showScreen()
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val currentToken = AccessToken.getCurrentAccessToken().token
                    AppPreferences.setFacebookToken(currentToken)
                    facebookLogin(currentToken)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    val message = task.exception.toString()
                    val index = message.indexOf(":")
                    Toast.makeText(this, "Authentication failed: ${message.substring(index + 1)}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun facebookLogin(token: String){

        HypechatRepository().facebookLoginUser(token){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.ACTIVE.status -> {
                        navigateToLatestMessages()
                        AppPreferences.setUserId(it.user.id)
                        AppPreferences.setUserName(it.user.first_name!!)
                    }
                    ServerStatus.WRONG_CREDENTIALS.status -> loginFailed(it.message)
                    else -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "facebookLoginUser:failure")
                errorOccurred(null)
            }
        }
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

    fun recoverPassword(view: View){

        val intent = Intent(this, RecoverPasswordActivity::class.java)
        startActivity(intent)
    }

    fun loginUser(view: View){

        if (validateField(emailTextInputLayout) && validateField(passwordTextInputLayout)){

            loadingScreen(view)
            val email = emailTextInputLayout.editText!!.text.toString()
            val password = passwordTextInputLayout.editText!!.text.toString()

            HypechatRepository().loginUser(email, password){ response ->

                response?.let {

                    when (it.status){
                        ServerStatus.ACTIVE.status -> {
                            if (it.user.role != UserRole.ADMIN.role){
                                firebaseLogin(email, password)
                                navigateToLatestMessages()
                                AppPreferences.setUserId(it.user.id)
                                AppPreferences.setUserName(it.user.username)
                            } else {
                                errorOccurred(it.message)
                            }
                        }
                        ServerStatus.WRONG_CREDENTIALS.status -> loginFailed(it.message)
                        else -> errorOccurred(it.message)
                    }
                }
                if (response == null){
                    Log.w(TAG, "loginUser:failure")
                    errorOccurred(null)
                }
            }
        }
    }

    private fun firebaseLogin(email: String, password: String){

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Firebase:signInWithEmail:success")
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                }
            }
    }

    private fun navigateToLatestMessages(){

        Log.d(TAG, "signInWithEmail:success")
        val intent = Intent(this, LatestMessagesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        mainProgressBar.visibility = View.INVISIBLE
    }

    private fun loginFailed(msg: String){
        showScreen()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun register(view: View){

        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun facebookLoadingScreen(){
        mainProgressBar.visibility = View.VISIBLE
        emailTextInputLayout.visibility = View.INVISIBLE
        passwordTextInputLayout.visibility = View.INVISIBLE
        loginButton.visibility = View.INVISIBLE
        loginFacebookButton.visibility = View.INVISIBLE
        troubleLoggingTextView.visibility = View.INVISIBLE
        notYetRegisteredTextView.visibility = View.INVISIBLE
        registerButton.visibility = View.INVISIBLE
    }

    private fun loadingScreen(view: View){
        facebookLoadingScreen()
        view.hideKeyboard()
    }

    private fun showScreen(){
        mainProgressBar.visibility = View.GONE
        emailTextInputLayout.visibility = View.VISIBLE
        passwordTextInputLayout.visibility = View.VISIBLE
        loginButton.visibility = View.VISIBLE
        loginFacebookButton.visibility = View.VISIBLE
        troubleLoggingTextView.visibility = View.VISIBLE
        notYetRegisteredTextView.visibility = View.VISIBLE
        registerButton.visibility = View.VISIBLE
    }

    fun View.hideKeyboard(){

        val inm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun errorOccurred(error: String?){

        showScreen()

        val builder = AlertDialog.Builder(this)
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
        if(!this.isFinishing){
            dialog.show()
        }
    }
}
