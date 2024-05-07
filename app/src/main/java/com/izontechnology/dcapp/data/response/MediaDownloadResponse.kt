package com.izontechnology.dcapp.data.response


import com.google.gson.annotations.SerializedName

data class MediaJobResponse(
    @SerializedName("coursemedia")
    var coursemedia: MediaDownloadResponse = MediaDownloadResponse(),
    @SerializedName("process")
    var process: Boolean = false
)

class MediaDownloadResponse : ArrayList<MediaDownloadResponseItem>()
data class MediaDownloadResponseItem(
    @SerializedName("filename")
    val filename: String? = "",
    @SerializedName("format")
    val format: String? = "",
    @SerializedName("size")
    val size: Int? = null,
    @SerializedName("type")
    val type: String? = "",
    @SerializedName("url")
    val url: String?="",
    @SerializedName("networktype")
    var networktype: NetworkType = NetworkType(),
)
