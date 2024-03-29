package com.example.hypechat.data.rest.utils

import com.example.hypechat.data.local.AppPreferences
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class ReceivedTokenInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())

        if (originalResponse.headers("X-Auth-Token").isNotEmpty()){
            val token = originalResponse.headers("X-Auth-Token")
            AppPreferences.setToken(token.first())
        }

        return originalResponse
    }
}