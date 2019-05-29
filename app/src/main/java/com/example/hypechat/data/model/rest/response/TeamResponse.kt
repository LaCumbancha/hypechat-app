package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class TeamResponse (

    @SerializedName("description") val description : String?,
    @SerializedName("location") val location : String?,
    @SerializedName("team_id") val team_id : Int,
    @SerializedName("team_name") val team_name : String,
    @SerializedName("welcome_message") val welcome_message : String?
)