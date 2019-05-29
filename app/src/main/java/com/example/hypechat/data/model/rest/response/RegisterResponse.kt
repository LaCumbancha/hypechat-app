package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse (

    @SerializedName("status") val status : String,
    @SerializedName("user") val user : UserResponse,
    @SerializedName("message") val message : String
)