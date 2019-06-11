package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class ChannelResponse(

    @SerializedName("channel_id") val channelId : Int,
    @SerializedName("creator") val creatorId : Int,
    @SerializedName("description") val description : String?,
    @SerializedName("name") val name : String,
    @SerializedName("team_id") val teamId : Int,
    @SerializedName("visibility") val visibility : String,
    @SerializedName("welcome_message") val message : String?
)