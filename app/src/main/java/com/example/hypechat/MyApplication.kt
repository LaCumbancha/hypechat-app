package com.example.hypechat

import android.app.Application
import com.facebook.appevents.AppEventsLogger
import com.facebook.FacebookSdk


class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
    }
}