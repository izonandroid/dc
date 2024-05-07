package com.izontechnology.dcapp.data.response


import com.google.gson.annotations.SerializedName

data class ValidCodeResponse(
    @SerializedName("error")
    val error: Error,
    @SerializedName("message")
    val message: String,
    @SerializedName("clubcode")
    val clubcode: String,
    @SerializedName("clubid")
    val clubid: String
)