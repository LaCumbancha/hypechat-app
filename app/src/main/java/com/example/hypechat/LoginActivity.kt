package com.example.hypechat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        setSupportActionBar(toolbar)
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
}
