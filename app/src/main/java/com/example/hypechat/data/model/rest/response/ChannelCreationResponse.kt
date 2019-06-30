package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class ChannelCreationResponse(

    @SerializedName("status") val status : String,
    @SerializedName("channel") val channel : ChannelResponse,
    @SerializedName("message") val message : String
)