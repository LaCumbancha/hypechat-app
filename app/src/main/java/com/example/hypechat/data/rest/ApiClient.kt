package com.example.hypechat.data.rest

import com.example.hypechat.data.model.rest.request.*
import com.example.hypechat.data.model.rest.response.*
import retrofit2.Call
import retrofit2.http.*

interface ApiClient {

    //USERS

    @POST("/users")
    fun registerUser(@Body body: RegisterRequest): Call<RegisterResponse>

    @POST("/users/login")
    fun loginUser(@Body body: LoginRequest): Call<LoginResponse>

    @POST("/users/logout")
    fun logoutUser(): Call<ApiResponse>

    @GET("/users/profile")
    fun getMyProfile(): Call<ProfileResponse>

    @PATCH("/users/profile")
    fun updateMyProfile(@Body body: UpdateProfileRequest): Call<RegisterResponse>

    @GET("teams/{team_id}/users/{user_id}/profile")
    fun getUserProfile(@Path("team_id") team_id: Int, @Path("user_id") userId: Int): Call<ProfileResponse>

    @GET("/teams/{team_id}/users/%")
    fun getUsers(@Path("team_id") team_id: Int): Call<UsersResponse>

    @GET("/teams/{team_id}/users/{query}")
    fun searchUsers(@Path("team_id") team_id: Int, @Path("query") query: String): Call<UsersResponse>

    //MESSAGES

    @POST("/teams/messages")
    fun sendMessage(@Body body: MessageRequest): Call<ApiResponse>

    @GET("/teams/{team_id}/messages/{chat_id}")
    fun getMessagesFromChat(@Path("team_id") team_id: Int, @Path("chat_id") chatId: Int): Call<MessagesResponse>

    @GET("/teams/{team_id}/messages/previews")
    fun getChatsPreviews(@Path("team_id") team_id: Int): Call<ChatsResponse>

    //TEAMS

    @POST("/teams")
    fun createTeam(@Body body: TeamCreationRequest): Call<TeamCreationResponse>

    @POST("/teams/invite")
    fun inviteUserToTeam(@Body body: InvitationRequest): Call<ApiResponse>

    @POST("/teams/join")
    fun joinTeam(@Body body: JoinTeamRequest): Call<ApiResponse>

    @PATCH("/teams/{team_id}/roles")
    fun updateRoleTeam(@Path("team_id") team_id: Int, @Body body: UpdateRoleRequest): Call<ApiResponse>

    @PATCH("/teams/{team_id}")
    fun updateTeam(@Path("team_id") team_id: Int, @Body body: TeamCreationRequest): Call<TeamCreationResponse>

    @DELETE("teams/{team_id}/leave")
    fun leaveTeam(@Path("team_id") team_id: Int): Call<ApiResponse>

    @DELETE("teams/{team_id}")
    fun deleteTeam(@Path("team_id") team_id: Int): Call<ApiResponse>

    @DELETE("teams/{team_id}/users/{user_id}")
    fun removeUserFromTeam(@Path("team_id") team_id: Int, @Path("user_id") userId: Int): Call<ApiResponse>

    @PUT("teams/forbidden-words")
    fun addForbiddenWord(@Body body: ForbiddenWordsRequest): Call<ApiResponse>

    @GET("teams/{team_id}/forbidden-words")
    fun getForbiddenWords(@Path("team_id") team_id: Int): Call<ForbiddenWordResponse>

    @DELETE("teams/{team_id}/forbidden-words/{word_id}")
    fun deleteForbiddenWord(@Path("team_id") team_id: Int, @Path("word_id") word_id: Int): Call<ApiResponse>

    @GET("/users/teams")
    fun getTeams(): Call<TeamsResponse>

    @GET("/teams/{team_id}/channels")
    fun getTeamChannels(@Path("team_id") team_id: Int): Call<ChannelsResponse>

    //CHANNELS

    @POST("/teams/channels")
    fun createChannel(@Body body: ChannelCreationRequest): Call<ChannelCreationResponse>

    @POST("/teams/channels/users")
    fun addUserToChannel(@Body body: UserChannelRequest): Call<ApiResponse>

    @POST("/teams/channels/join")
    fun joinChannel(@Body body: JoinChannelRequest): Call<ApiResponse>

    @PATCH("/teams/{team_id}/channels/{channel_id}")
    fun updateChannel(@Path("team_id") team_id: Int, @Path("channel_id") channel_id: Int, @Body body: UpdateChannelRequest): Call<ChannelCreationResponse>

    @GET("/teams/{team_id}/channels/{channel_id}/users")
    fun getChannelUsers(@Path("team_id") team_id: Int, @Path("channel_id") channel_id: Int): Call<UsersResponse>

    @DELETE("/teams/{team_id}/channels/{channel_id}/users/{user_id}")
    fun deleteUserFromChannel(@Path("team_id") team_id: Int, @Path("channel_id") channel_id: Int, @Path("user_id") user_id: Int): Call<ApiResponse>

    @DELETE("teams/{team_id}/channels/{channel_id}/leave")
    fun leaveChannel(@Path("team_id") team_id: Int, @Path("channel_id") channel_id: Int): Call<ApiResponse>

    @DELETE("teams/{team_id}/channels/{channel_id}")
    fun deleteChannel(@Path("team_id") team_id: Int, @Path("channel_id") channel_id: Int): Call<ApiResponse>
}