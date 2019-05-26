package com.example.hypechat.data.model.rest

import com.google.gson.annotations.SerializedName

data class UsersResponse (

    @SerializedName("status") val status : String,
    @SerializedName("users") val users : List<UserResponse>,
    @SerializedName("message") val message : String
)