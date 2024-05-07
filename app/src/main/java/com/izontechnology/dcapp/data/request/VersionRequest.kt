package com.izontechnology.dcapp.data.request


import com.google.gson.annotations.SerializedName

data class VersionRequest(
    @SerializedName("dcappversion")
    var dcappversion: String? = null,
    @SerializedName("deviceid")
    var deviceid: String? = null,
    @SerializedName("izonappversion")
    var izonappversion: String? = null
)