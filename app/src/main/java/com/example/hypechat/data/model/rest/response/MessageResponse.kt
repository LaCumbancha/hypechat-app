package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class MessageResponse (

    @SerializedName("seen") val seen : Boolean,
    @SerializedName("text_content") val message : String,
    @SerializedName("timestamp") val timestamp : String,
    @SerializedName("user_id") val fromId : Int

)