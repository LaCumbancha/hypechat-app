package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class ForbiddenWordResponse (

    @SerializedName("status") val status : String,
    @SerializedName("words") val teams : List<ForbiddenWord>,
    @SerializedName("message") val message : String
)