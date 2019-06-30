package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ChannelResponse(

    @SerializedName("channel_id") val channelId : Int,
    @SerializedName("id") val id : Int,
    @SerializedName("creator") val creator : UserResponse,
    @SerializedName("description") val description : String?,
    @SerializedName("name") val name : String,
    @SerializedName("team_id") val teamId : Int,
    @SerializedName("visibility") val visibility : String,
    @SerializedName("welcome_message") val message : String?
): Serializable