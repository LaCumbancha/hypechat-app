package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class ForbiddenWordResponse (

    @SerializedName("status") val status : String,
    @SerializedName("forbidden_words") val words : List<ForbiddenWord>,
    @SerializedName("message") val message : String
)