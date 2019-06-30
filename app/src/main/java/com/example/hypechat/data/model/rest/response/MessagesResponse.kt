package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class MessagesResponse (

    @SerializedName("status") val status : String,
    @SerializedName("chat_type") val chat_type : String,
    @SerializedName("messages") val messages : List<MessageResponse>,
    @SerializedName("message") val message : String
)