package com.example.hypechat.data.repository

import android.util.Log
import com.example.hypechat.data.model.rest.*
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
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun registerUser(username:String, email: String, password: String, firstName:String?,
                     lastName: String?, profilePic: String? , onSuccess: (user: ApiResponse?) -> Unit) {

        val body = RegisterRequest(username, email, password, firstName, lastName, profilePic)
        //val call = client.loginUser(email, password)
        val call = client.registerUser(body)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun logoutUser(username: String, token: String, onSuccess: (user: ApiResponse?) -> Unit) {

        val cookie = "username=$username; auth_token=$token"
        val call = client.logoutUser(cookie)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun getUsers(username: String, token: String, onSuccess: (user: UsersResponse?) -> Unit) {

        val cookie = "username=$username; auth_token=$token"
        val call = client.getUsers(cookie)

        call.enqueue(object : Callback<UsersResponse> {
            override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun getMessagesFromChat(username: String, token: String, fromId: Int, onSuccess: (user: MessagesResponse?) -> Unit) {

        val cookie = "username=$username; auth_token=$token"
        val call = client.getMessagesFromChat(cookie, fromId)

        call.enqueue(object : Callback<MessagesResponse> {
            override fun onFailure(call: Call<MessagesResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<MessagesResponse>, response: Response<MessagesResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun sendMessage(username: String, token: String, toId: Int, message: String, onSuccess: (user: ApiResponse?) -> Unit) {

        val cookie = "username=$username; auth_token=$token"
        val body = MessageRequest(toId, message)
        val call = client.sendMessage(cookie, body)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                onSuccess(response.body())
            }
        })
    }
}