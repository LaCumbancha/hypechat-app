package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class ChatsResponse (

    @SerializedName("status") val status : String,
    @SerializedName("chats") val chats : List<ChatResponse>,
    @SerializedName("message") val message : String

)