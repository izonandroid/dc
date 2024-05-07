package com.izontechnology.dcapp.data.response


import com.google.gson.annotations.SerializedName

data class VersionJobResponse(
    @SerializedName("appversions")
    var appversions: VersionResponse = VersionResponse(),
    @SerializedName("process")
    var process: Boolean = false
)
data class VersionResponse(
    @SerializedName("dcapp")
    var dcapp: App? = null,
    @SerializedName("izonapp")
    var izonapp: App? = null
)

data class App(
    @SerializedName("networktype")
    var networktype: NetworkType = NetworkType(),
    @SerializedName("app")
    var app: String? = null,
    @SerializedName("downloadlink")
    var downloadlink: String? = null,
    @SerializedName("version")
    var version: String? = null
)

