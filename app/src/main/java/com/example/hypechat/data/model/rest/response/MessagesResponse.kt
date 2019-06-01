package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class MessagesResponse (

    @SerializedName("status") val status : String,
    @SerializedName("messages") val messages : List<MessageResponse>,
    @SerializedName("message") val message : String
)