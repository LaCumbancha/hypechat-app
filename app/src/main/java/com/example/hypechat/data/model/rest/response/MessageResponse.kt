package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class MessageResponse (

    @SerializedName("unseen") val seen : Boolean,
    @SerializedName("content") val message : String,
    @SerializedName("sender") val sender : UserResponse,
    @SerializedName("timestamp") val timestamp : String,
    @SerializedName("type") val type : String

)