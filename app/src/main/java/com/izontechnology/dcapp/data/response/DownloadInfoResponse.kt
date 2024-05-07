package com.izontechnology.dcapp.data.response


import com.google.gson.annotations.SerializedName

data class DownloadInfoJobResponse(
    @SerializedName("process")
    var process: Boolean = false,
    @SerializedName("tiles")
    var tiles: DownloadInfoResponse = DownloadInfoResponse()
)
data class DownloadInfoResponse(
    @SerializedName("clubid")
    var clubid: Int = 0,
    @SerializedName("filesize")
    var filesize: Int = 0,
    @SerializedName("tilesversion")
    var tilesversion: String = "",
    @SerializedName("tileszip")
    var tileszip: String = "",
    @SerializedName("networktype")
    var networktype: NetworkType = NetworkType(),
)