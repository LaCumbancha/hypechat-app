package com.example.hypechat.data.repository

import android.util.Log
import com.example.hypechat.data.model.rest.request.LoginRequest
import com.example.hypechat.data.model.rest.request.MessageRequest
import com.example.hypechat.data.model.rest.request.RegisterRequest
import com.example.hypechat.data.model.rest.request.TeamCreationRequest
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

    fun loginUser(email: String, password: String, onSuccess: (user: ApiResponse?) -> Unit) {

        val body = LoginRequest(email, password)
        val call = client?.loginUser(body)

        call?.enqueue(object : Callback<ApiResponse> {
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.w("HypechatRepository: ", t)
            }

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
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

    fun getUserProfile(onSuccess: (user: ProfileResponse?) -> Unit) {

        val call = client?.getUserProfile()

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

        val body = MessageRequest(toId, teamId, message)
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
}