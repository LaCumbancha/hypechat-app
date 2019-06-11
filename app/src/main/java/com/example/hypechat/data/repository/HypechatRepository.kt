package com.example.hypechat.data.repository

import android.util.Log
import com.example.hypechat.data.model.rest.request.*
import com.example.hypechat.data.model.rest.response.*
import com.example.hypechat.data.rest.ApiClient
import com.example.hypechat.data.rest.utils.AddTokenInterceptor
import com.example.hypechat.data.rest.utils.ReceivedTokenInterceptor
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
    private var httpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var client : ApiClient? = null

    init {
        httpClient = OkHttpClient.Builder()
            .addInterceptor(AddTokenInterceptor())
            .addInterceptor(ReceivedTokenInterceptor())
            .connectTimeout(httpConnectTimeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(httpWriteTimeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(httpReadTimeoutSeconds.toLong(), TimeUnit.SECONDS)
            .build()
        httpClient?.let {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(it)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        client = createService(ApiClient::class.java)
    }

    private fun <S> createService(serviceClass: Class<S>): S? {
        return retrofit?.create(serviceClass)
    }

    fun loginUser(email: String, password: String, onSuccess: (user: LoginResponse?) -> Unit) {

        val body = LoginRequest(email, password)
        val call = client?.loginUser(body)

        call?.enqueue(object : Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun registerUser(username:String, email: String, password: String, firstName:String?,
                     lastName: String?, profilePic: String? , onSuccess: (user: RegisterResponse?) -> Unit) {

        val body = RegisterRequest(
            username,
            email,
            password,
            firstName,
            lastName,
            profilePic
        )
        val call = client?.registerUser(body)

        call?.enqueue(object : Callback<RegisterResponse> {
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun logoutUser(onSuccess: (user: ApiResponse?) -> Unit) {

        val call = client?.logoutUser()

        call?.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun getMyProfile(onSuccess: (user: ProfileResponse?) -> Unit) {

        val call = client?.getMyProfile()

        call?.enqueue(object : Callback<ProfileResponse> {
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun getUserProfile(teamId: Int, userId: Int, onSuccess: (user: ProfileResponse?) -> Unit) {

        val call = client?.getUserProfile(teamId, userId)

        call?.enqueue(object : Callback<ProfileResponse> {
            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun getUsers(teamId: Int, onSuccess: (user: UsersResponse?) -> Unit) {

        val call = client?.getUsers(teamId)

        call?.enqueue(object : Callback<UsersResponse> {
            override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun searchUsers(teamId: Int, query: String, onSuccess: (user: UsersResponse?) -> Unit) {

        val call = client?.searchUsers(teamId, query)

        call?.enqueue(object : Callback<UsersResponse> {
            override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun getMessagesFromChat(teamId: Int, fromId: Int, onSuccess: (user: MessagesResponse?) -> Unit) {

        val call = client?.getMessagesFromChat(teamId, fromId)

        call?.enqueue(object : Callback<MessagesResponse> {
            override fun onFailure(call: Call<MessagesResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<MessagesResponse>, response: Response<MessagesResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun sendMessage(toId: Int, message: String, teamId: Int, onSuccess: (user: ApiResponse?) -> Unit) {

        val type = "TEXT"
        val body = MessageRequest(toId, teamId, message, type)
        val call = client?.sendMessage(body)

        call?.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun getChatsPreviews(teamId: Int, onSuccess: (user: ChatsResponse?) -> Unit) {

        val call = client?.getChatsPreviews(teamId)

        call?.enqueue(object : Callback<ChatsResponse> {
            override fun onFailure(call: Call<ChatsResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ChatsResponse>, response: Response<ChatsResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun getTeams(onSuccess: (teams: TeamsResponse?) -> Unit) {

        val call = client?.getTeams()

        call?.enqueue(object : Callback<TeamsResponse> {
            override fun onFailure(call: Call<TeamsResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<TeamsResponse>, response: Response<TeamsResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun createTeam(teamName: String, location: String?, description: String?, welcomeMessage: String?,
                   profilePicUrl: String?, onSuccess: (user: TeamCreationResponse?) -> Unit) {

        val body = TeamCreationRequest(teamName, location, description, welcomeMessage, profilePicUrl)
        val call = client?.createTeam(body)

        call?.enqueue(object : Callback<TeamCreationResponse> {
            override fun onFailure(call: Call<TeamCreationResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<TeamCreationResponse>, response: Response<TeamCreationResponse>) {
                onSuccess(response.body())
            }
        })
    }

    fun createChannel(teamId: Int, name: String, visibility: String, description: String?, welcomeMessage: String?,
                      onSuccess: (user: ChannelCreationResponse?) -> Unit) {

        val body = ChannelCreationRequest(teamId, name, visibility, description, welcomeMessage)
        val call = client?.createChannel(body)

        call?.enqueue(object : Callback<ChannelCreationResponse> {
            override fun onFailure(call: Call<ChannelCreationResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ChannelCreationResponse>, response: Response<ChannelCreationResponse>) {
                onSuccess(response.body())
            }
        })
    }
}