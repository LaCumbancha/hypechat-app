package com.example.hypechat.data.model.rest

import com.google.gson.annotations.SerializedName

class ChatResponse (

    @SerializedName("chat_name") val chatName : String,
    @SerializedName("chat_picture") val chatPicture : String,
    @SerializedName("content") val content : String,
    @SerializedName("offset") val offset : Int,
    @SerializedName("receiver_id") val receiverId : Int,
    @SerializedName("sender_id") val senderId : Int,
    @SerializedName("timestamp") val timestamp : String,
    @SerializedName("unseen") val unseen : Boolean

)