package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

class ChatResponse (

    @SerializedName("chat_name") val chatName : String,
    @SerializedName("chat_picture") val chatPicture : String?,
    @SerializedName("content") val content : String,
    @SerializedName("offset") val offset : Int,
    @SerializedName("chat_id") val chat_id : Int,
    @SerializedName("sender") val sender : UserResponse,
    @SerializedName("timestamp") val timestamp : String,
    @SerializedName("type") val type : String,
    @SerializedName("unseen") val unseen : Boolean

)