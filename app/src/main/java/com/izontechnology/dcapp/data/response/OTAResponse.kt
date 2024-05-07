package com.izontechnology.dcapp.data.response


import com.google.gson.annotations.SerializedName

data class OTAResponse(
    @SerializedName("fileurl")
    val fileurl: String? = "",
    @SerializedName("filezip")
    val filezip: String? = "",
    @SerializedName("devicemodel")
    val devicemodel: String? = "",
    @SerializedName("version")
    val version: String? = ""
)