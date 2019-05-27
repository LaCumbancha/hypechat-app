package com.example.hypechat.data.rest

import com.example.hypechat.data.model.rest.*
import retrofit2.Call
import retrofit2.http.*

interface ApiClient {

    @POST("/users")
    fun registerUser(@Body body: RegisterRequest): Call<ApiResponse>

    @POST("/users/login")
    fun loginUser(@Body body: LoginRequest): Call<ApiResponse>

    @POST("/users/logout")
    fun logoutUser(): Call<ApiResponse>

    @GET("/users/%")
    fun getUsers(): Call<UsersResponse>

    @POST("/messages")
    fun sendMessage(@Body body: MessageRequest): Call<ApiResponse>

    @GET("/messages/{chat_id}")
    fun getMessagesFromChat(@Path("chat_id") chatId: Int): Call<MessagesResponse>

    @GET("/messages/previews")
    fun getChatsPreviews(): Call<ChatsResponse>
}