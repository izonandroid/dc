package com.izontechnology.dcapp.data.response


import com.google.gson.annotations.SerializedName

data class NetworkType(
    @SerializedName("priority")
    var priority: String = "",
    @SerializedName("simnetwork")
    var simnetwork: Network = Network(),
    @SerializedName("wifinetwork")
    var wifinetwork: Network = Network()
)

data class Network(
    @SerializedName("download")
    var download: Boolean = false
)
