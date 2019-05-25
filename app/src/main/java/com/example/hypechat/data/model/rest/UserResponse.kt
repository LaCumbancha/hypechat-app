package com.example.hypechat.data.model.rest

import com.google.gson.annotations.SerializedName

data class UserResponse (

    @SerializedName("email") val email : String,
    @SerializedName("first_name") val first_name : String?,
    @SerializedName("last_name") val last_name : String?,
    @SerializedName("profile_pic") val profile_pic : String?,
    @SerializedName("token") val token : String,
    @SerializedName("username") val username : String
)