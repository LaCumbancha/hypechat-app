package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class TeamsResponse (

    @SerializedName("status") val status : String,
    @SerializedName("teams") val teams : List<TeamResponse>,
    @SerializedName("message") val message : String
)