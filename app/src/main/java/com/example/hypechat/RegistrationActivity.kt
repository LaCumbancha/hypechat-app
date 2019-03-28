package com.example.hypechat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        setSupportActionBar(toolbarRegistration)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
