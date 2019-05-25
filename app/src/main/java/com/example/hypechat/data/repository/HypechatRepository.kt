package com.example.hypechat.data.repository

import android.util.Log
import com.example.hypechat.data.model.rest.ApiResponse
import com.example.hypechat.data.model.rest.LoginRequest
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

    fun loginUser(email: String, password: String, onSuccess: (user: ApiResponse?) -> Unit) {

        val body = LoginRequest(email, password)
        //val call = client.loginUser(email, password)
        val call = client.loginUser(body)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.w("HypechatRepository", t)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun registerUser(username:String, email: String, password: String, firstName:String?,
                     lastName: String?, profilePic: String? , onSuccess: (user: ApiResponse?) -> Unit) {

        val body = LoginRequest(email, password)
        //val call = client.loginUser(email, password)
        val call = client.loginUser(body)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.w("HypechatRepository", t)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                onSuccess(response.body())
            }
        })
    }
}