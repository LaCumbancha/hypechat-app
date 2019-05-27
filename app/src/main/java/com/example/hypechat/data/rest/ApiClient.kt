package com.example.hypechat.data.rest

import com.example.hypechat.data.model.ChatMessage
import com.example.hypechat.data.model.User
import com.example.hypechat.data.model.rest.*
import retrofit2.Call
import retrofit2.http.*

interface ApiClient {

    @POST("/users")
    fun registerUser(@Body body: RegisterRequest): Call<ApiResponse>

    @POST("/users/login")
    fun loginUser(@Body body: LoginRequest): Call<ApiResponse>

    @POST("/users/logout")
    fun logoutUser(@Header("Cookie") usernameAndToken: String): Call<ApiResponse>

    @GET("/users/%")
    fun getUsers(@Header("Cookie") usernameAndToken: String): Call<UsersResponse>

    @POST("/messages")
    fun sendMessage(@Header("Cookie") usernameAndToken: String, @Body body: MessageRequest): Call<ApiResponse>

    @GET("/messages/{chat_id}")
    fun getMessagesFromChat(@Header("Cookie") usernameAndToken: String, @Path("chat_id") chatId: Int): Call<MessagesResponse>

    @GET("/messages/previews")
    fun getChatsPreviews(@Header("Cookie") usernameAndToken: String): Call<ChatsResponse>
}