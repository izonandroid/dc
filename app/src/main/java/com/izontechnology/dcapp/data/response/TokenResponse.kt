package com.izontechnology.dcapp.data.response


import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("token")
    val token: String
)