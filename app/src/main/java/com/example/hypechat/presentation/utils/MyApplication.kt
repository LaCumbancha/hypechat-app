package com.example.hypechat.presentation.utils

import android.app.Application
import com.example.hypechat.data.local.AppPreferences
import com.facebook.appevents.AppEventsLogger
import com.facebook.FacebookSdk


class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

        AppPreferences.init(this)
    }
}