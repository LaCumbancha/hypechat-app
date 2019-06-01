package com.example.hypechat.data.model.rest.response

import com.google.gson.annotations.SerializedName

data class TeamCreationResponse (

    @SerializedName("status") val status : String,
    @SerializedName("team") val team : TeamResponse,
    @SerializedName("message") val message : String

)