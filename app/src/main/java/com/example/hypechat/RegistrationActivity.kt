package com.example.hypechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.hypechat.model.User
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "Registration"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = FirebaseAuth.getInstance()

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

    fun registerUser(view: View){

        if (validateField(fullnameTextInputLayout) && validateField(registerEmailTextInputLayout)
            && validateField(registerPasswordTextInputLayout)){

            val email = registerEmailTextInputLayout.editText!!.text.toString()
            val password = registerPasswordTextInputLayout.editText!!.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        Toast.makeText(this, "createUserWithEmail:success!!!!!", Toast.LENGTH_SHORT).show()
                        saveUser()
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this, "Authentication failed: ${task.exception}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun saveUser(){

        val uid = auth.uid
        val fullName = fullnameTextInputLayout.editText!!.text.toString()
        val email = registerEmailTextInputLayout.editText!!.text.toString()
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val newUser = User(uid!!, fullName, email)

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
