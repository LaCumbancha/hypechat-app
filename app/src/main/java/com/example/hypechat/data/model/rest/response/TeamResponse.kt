package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TeamResponse (

    @SerializedName("description") val description : String?,
    @SerializedName("location") val location : String?,
    @SerializedName("picture") val picture : String?,
    @SerializedName("id") val team_id : Int,
    @SerializedName("messages") val messages : Int,
    @SerializedName("team_name") val team_name : String,
    @SerializedName("role") val role : String,
    @SerializedName("welcome_message") val welcome_message : String?
): Serializable