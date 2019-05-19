package com.example.hypechat.data.rest

import com.example.hypechat.data.model.ChatMessage
import com.example.hypechat.data.model.Response
import com.example.hypechat.data.model.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiClient {

    @POST("/users")
    fun registerUser(id: String, email: String, password: String): Call<Response>

    @POST("/users/login")
    fun loginUser(email: String, password: String): Call<Response>

    @POST("/users")
    fun logoutUser(authToken: String): Call<Response>

    @GET("/users")
    fun getUsers(): Call<List<User>>

    @GET("/messages")
    fun getMessages(): Call<List<ChatMessage>>
}