package com.example.hypechat.data.rest

import com.example.hypechat.data.model.ChatMessage
import com.example.hypechat.data.model.User
import com.example.hypechat.data.model.rest.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiClient {

    @POST("/users")
    fun registerUser(@Body body: RegisterRequest): Call<ApiResponse>

    @POST("/users/login")
    fun loginUser(@Body body: LoginRequest): Call<ApiResponse>

    @POST("/users")
    fun logoutUser(@Body body: Request): Call<ApiResponse>

    @GET("/users")
    fun getUsers(@Body body: Request): Call<List<UserResponse>>

    @GET("/messages")
    fun getMessages(@Body body: Request): Call<List<ChatMessage>>
}