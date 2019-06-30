package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

class BotResponse (

    @SerializedName("id") val id : Int,
    @SerializedName("name") val name : String
)