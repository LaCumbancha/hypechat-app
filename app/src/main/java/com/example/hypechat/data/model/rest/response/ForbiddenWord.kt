package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class ForbiddenWord (

    @SerializedName("id") val id : Int,
    @SerializedName("word") val word : String
)