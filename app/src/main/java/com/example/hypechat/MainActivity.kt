package com.example.hypechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
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
        loginFacebookButton.setReadPermissions("email", "public_profile")
    }

    fun loginWithFacebook(view: View){
        loginFacebookButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
                Toast.makeText(this@MainActivity, "facebook:onSuccess: $loginResult", Toast.LENGTH_SHORT).show()
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                Toast.makeText(this@MainActivity, "facebook:onCancel", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                Toast.makeText(this@MainActivity, "facebook:onError: $error", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "signInWithCredential:success!!!!!", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun loginWithEmail(view: View){

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun register(view: View){

        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }
}
