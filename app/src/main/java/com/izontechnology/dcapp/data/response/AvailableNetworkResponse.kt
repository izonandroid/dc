package com.izontechnology.dcapp.data.response


import com.google.gson.annotations.SerializedName

data class AvailableNetworkResponse(
    @SerializedName("availablenetworks")
    var availablenetworks: ArrayList<Availablenetwork> = ArrayList()
)

data class  Availablenetwork(
    @SerializedName("password")
    var password: String = "",
    @SerializedName("ssid")
    var ssid: String = ""
)
