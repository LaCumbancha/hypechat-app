package com.example.hypechat.data.model.rest

import com.google.gson.annotations.SerializedName
import java.text.DateFormat
import java.time.LocalDateTime

data class MessageResponse (

    @SerializedName("seen") val seen : Boolean,
    @SerializedName("text_content") val message : String,
    //@SerializedName("timestamp") val timestamp : LocalDateTime,
    @SerializedName("user_id") val fromId : Int

)