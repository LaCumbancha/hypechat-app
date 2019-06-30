package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserResponse (

    @SerializedName("id") val id : Int,
    @SerializedName("email") val email : String,
    @SerializedName("type") val type : String,
    @SerializedName("name") val name : String?,
    @SerializedName("first_name") val first_name : String?,
    @SerializedName("last_name") val last_name : String?,
    @SerializedName("profile_pic") val profile_pic : String?,
    @SerializedName("username") val username : String,
    @SerializedName("teams") val teams : List<TeamResponse>,
    @SerializedName("role") val role : String,
    @SerializedName("created") val created : String,
    @SerializedName("online") val online : Boolean
): Serializable