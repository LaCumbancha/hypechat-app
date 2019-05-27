package com.example.hypechat.data.repository

import android.util.Log
import com.example.hypechat.data.model.rest.*
import com.example.hypechat.data.rest.ApiClient
import com.example.hypechat.data.rest.utils.AddCookiesInterceptor
import com.example.hypechat.data.rest.utils.ReceivedCookiesInterceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class HypechatRepository {

    private val httpConnectTimeoutSeconds = 10
    private val httpWriteTimeoutSeconds = 10
    private val httpReadTimeoutSeconds = 10
    private val BASE_URL = "https://hypechat-server.herokuapp.com"
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(AddCookiesInterceptor())
        .addInterceptor(ReceivedCookiesInterceptor())
        .connectTimeout(httpConnectTimeoutSeconds.toLong(), TimeUnit.SECONDS)
        .writeTimeout(httpWriteTimeoutSeconds.toLong(), TimeUnit.SECONDS)
        .readTimeout(httpReadTimeoutSeconds.toLong(), TimeUnit.SECONDS)
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
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

    fun logoutUser(onSuccess: (user: ApiResponse?) -> Unit) {

        //val cookie = "username=$username; auth_token=$token"
        val call = client.logoutUser()

        call.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun getUsers(onSuccess: (user: UsersResponse?) -> Unit) {

        //val cookie = "username=$username; auth_token=$token"
        val call = client.getUsers()

        call.enqueue(object : Callback<UsersResponse> {
            override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun getMessagesFromChat(fromId: Int, onSuccess: (user: MessagesResponse?) -> Unit) {

        //val cookie = "username=$username; auth_token=$token"
        val call = client.getMessagesFromChat(fromId)

        call.enqueue(object : Callback<MessagesResponse> {
            override fun onFailure(call: Call<MessagesResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<MessagesResponse>, response: Response<MessagesResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun sendMessage(toId: Int, message: String, onSuccess: (user: ApiResponse?) -> Unit) {

        //val cookie = "username=$username; auth_token=$token"
        val body = MessageRequest(toId, message)
        val call = client.sendMessage(body)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun getChatsPreviews(onSuccess: (user: ChatsResponse?) -> Unit) {

        //val cookie = "username=$username; auth_token=$token"
        val call = client.getChatsPreviews()

        call.enqueue(object : Callback<ChatsResponse> {
            override fun onFailure(call: Call<ChatsResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ChatsResponse>, response: Response<ChatsResponse>) {
                onSuccess(response.body())
            }
        })
    }
}