package com.example.hypechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.hypechat.model.User
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
                    saveUser(auth.currentUser)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    val message = task.exception.toString()
                    val index = message.indexOf(":")
                    Toast.makeText(this, "Authentication failed: ${message.substring(index + 1)}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUser(currentUser: FirebaseUser?){

        val uid = currentUser!!.uid
        val fullName = currentUser.displayName
        val email = currentUser.email
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.w(TAG, "Failed to save user to Database", p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()){
                    val newUser = User(uid, fullName!!, email!!)

                    ref.setValue(newUser)
                        .addOnSuccessListener {
                            Log.d(TAG, "User saved to Database")
                            //Toast.makeText(this@MainActivity, "User saved to Database", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Log.w(TAG, "Failed to save user to Database", it.cause)
                        }
                }
            }
        })
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

    fun loginUser(view: View){

        if (validateField(emailTextInputLayout) && validateField(passwordTextInputLayout)){

            val email = emailTextInputLayout.editText!!.text.toString()
            val password = passwordTextInputLayout.editText!!.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        Toast.makeText(this, "signInWithEmail:success!!!!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        val message = task.exception.toString()
                        val index = message.indexOf(":")
                        Toast.makeText(this, "Authentication failed: ${message.substring(index + 1)}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun register(view: View){

        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
    }
}
