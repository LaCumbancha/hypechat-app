package com.example.hypechat.data.repository

import android.util.Log
import com.example.hypechat.data.rest.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HypechatRepository {

    private val BASE_URL = "https://hypechat-server.herokuapp.com"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val client = createService(ApiClient::class.java)
    //private var authToken: String? = null

    private fun <S> createService(serviceClass: Class<S>): S {
        return retrofit.create(serviceClass)
    }

    private fun loginUser(email: String, password: String): String? {

        val call = client.loginUser(email, password)
        var authToken : String? = null

        call.enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.w("HypechatRepository", t)
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                authToken = response.body()
            }

        })
        return authToken
    }
}