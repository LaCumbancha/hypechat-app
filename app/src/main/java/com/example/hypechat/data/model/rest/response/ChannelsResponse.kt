package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class ChannelsResponse (

    @SerializedName("status") val status : String,
    @SerializedName("channels") val channels : List<ChannelResponse>,
    @SerializedName("message") val message : String
)