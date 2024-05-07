package com.izontechnology.dcapp.data.request

import com.google.gson.annotations.SerializedName

data class DeviceInfo(
    @SerializedName("deviceid")
    var deviceid: String = "",

    @SerializedName("devicename")
    var devicename: String = "",

    @SerializedName("devicestatus")
    var deviceStatus: String = "",

    @SerializedName("ipadress")
    var ipadress: String = "",

    @SerializedName("apikey")
    var apikey: String = "",

    @SerializedName("asignedname")
    var asignedName: String = "",

    @SerializedName("availablestorage")
    var availableStorage: String = "",
    @SerializedName("totalstorage")
    var totalStorage: String = "",
    @SerializedName("cpuUsage")
    var cpuUsage: String = "",

    @SerializedName("ramUsage")
    var ramUsage: String = "",

    @SerializedName("devicebrightness")
    var devicebrightness: Int = 0
)