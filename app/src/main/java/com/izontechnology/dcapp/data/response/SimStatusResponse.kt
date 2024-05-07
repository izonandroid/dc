package com.izontechnology.dcapp.data.response


import com.google.gson.annotations.SerializedName

data class SimStatusResponse(
    @SerializedName("error")
    val error: Error,
    @SerializedName("message")
    val message: String,
    @SerializedName("simheader")
    val simheader: String
)
data class Error(
    @SerializedName("code")
    var code: String = "",
    @SerializedName("message")
    var message: String = ""
)
