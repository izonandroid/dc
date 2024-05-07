package com.izontechnology.dcapp.data.response


import com.google.gson.annotations.SerializedName

data class BrightnessResponse(
    @SerializedName("devicebrightness")
    val deviceBrightness: String,
    @SerializedName("status")
    val status: String
)