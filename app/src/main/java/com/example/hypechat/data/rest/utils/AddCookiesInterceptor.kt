package com.example.hypechat.data.rest.utils

import android.util.Log
import com.example.hypechat.data.local.AppPreferences
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AddCookiesInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val cookies = AppPreferences.getCookies()
        cookies?.let {
            val list = mutableListOf<String>()
            for (cookie in it) {
                //val h = "username=test; auth_token=iAJGVz3vUOT43F5heSv3bHZ01EsjbuVXBlxbdzyg"
                val c = cookie.split(";")
                list.add(c[0])
                Log.v("OkHttp3", "Adding Header: $cookie")
            }
            builder.addHeader("Cookie", "${list[1]}; ${list[0]}")
        }

        return chain.proceed(builder.build())
    }
}