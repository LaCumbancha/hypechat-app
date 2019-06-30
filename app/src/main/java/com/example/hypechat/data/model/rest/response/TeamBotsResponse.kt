package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

class TeamBotsResponse (

    @SerializedName("status") val status : String,
    @SerializedName("bots") val bots : List<BotResponse>,
    @SerializedName("message") val message : String
)