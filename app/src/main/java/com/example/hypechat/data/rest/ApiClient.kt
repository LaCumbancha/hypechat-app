package com.example.hypechat.data.rest

import com.example.hypechat.data.model.rest.request.*
import com.example.hypechat.data.model.rest.response.*
import retrofit2.Call
import retrofit2.http.*

interface ApiClient {

    @POST("/users")
    fun registerUser(@Body body: RegisterRequest): Call<RegisterResponse>

    @POST("/users/login")
    fun loginUser(@Body body: LoginRequest): Call<ApiResponse>

    @POST("/users/logout")
    fun logoutUser(): Call<ApiResponse>

    @GET("/teams/{team_id}/users/%")
    fun getUsers(@Path("team_id") team_id: Int): Call<UsersResponse>

    @GET("/teams/{team_id}/users/{query}")
    fun searchUsers(@Path("team_id") team_id: Int, @Path("query") query: String): Call<UsersResponse>

    @POST("/teams/{team_id}/messages")
    fun sendMessage(@Body body: MessageRequest, @Path("team_id") team_id: Int): Call<ApiResponse>

    @GET("/teams/{team_id}/messages/{chat_id}")
    fun getMessagesFromChat(@Path("team_id") team_id: Int, @Path("chat_id") chatId: Int): Call<MessagesResponse>

    @GET("/teams/{team_id}/messages/previews")
    fun getChatsPreviews(@Path("team_id") team_id: Int): Call<ChatsResponse>

    @POST("/teams")
    fun createTeam(@Body body: TeamCreationRequest): Call<TeamCreationResponse>

    @POST("/teams/{team_id}/invite")
    fun inviteUserToTeam(@Path("team_id") team_id: Int, @Body body: InvitationRequest): Call<ApiResponse>

    @GET("/users/teams")
    fun getTeams(): Call<TeamsResponse>
}