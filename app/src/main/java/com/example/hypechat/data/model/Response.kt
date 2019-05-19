package com.example.hypechat.data.model

import com.google.gson.annotations.SerializedName

data class Response (
    @SerializedName("auth_token") val authToken : String,
    @SerializedName("message") val message : String
)