package com.example.hypechat.data.rest.utils

import com.example.hypechat.data.local.AppPreferences
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AddTokenInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val builder = chain.request().newBuilder()
        val token = AppPreferences.getToken()

        token?.let {
            builder.addHeader("X-Auth-Token", it)
        }

        return chain.proceed(builder.build())
    }
}