package com.example.hypechat.data.model.rest

import com.google.gson.annotations.SerializedName

data class ApiResponse (

    @SerializedName("status") val status : String,
    @SerializedName("user") val user : UserResponse,
    @SerializedName("message") val message : String

    /*@SerializedName("auth_token") val authToken : String,
    @SerializedName("message") val message : String*/
)