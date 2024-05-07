package com.izontechnology.dcapp.data.request


import com.google.gson.annotations.SerializedName

data class ExitKioskRequest(
    @SerializedName("cartid")
    var cartid: String? = "",
    @SerializedName("cartname")
    var cartname: String? = "",
    @SerializedName("clientdatetime")
    var clientdatetime: String? = "",
    @SerializedName("clubcode")
    var clubcode: String? = "",
    @SerializedName("dcappversion")
    var dcappversion: String? = "",
    @SerializedName("deviceiccid")
    var deviceiccid: String? = "",
    @SerializedName("deviceid")
    var deviceid: String? = "",
    @SerializedName("exitcode")
    var exitcode: String? = "",
    @SerializedName("izongolfappversion")
    var izongolfappversion: String? = ""
)