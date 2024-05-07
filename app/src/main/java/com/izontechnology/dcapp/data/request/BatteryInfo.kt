package com.izontechnology.dcapp.data.request

import com.google.gson.annotations.SerializedName

data class BatteryInfo(
    @SerializedName("deviceid")
    var deviceid: String = "",
    @SerializedName("ipAdress")
    var ipadress: String = "",
    @SerializedName("devicestatus")
    var deviceStatus: String = "",
    @SerializedName("devicename")
    var devicename: String = "",
    @SerializedName("batterypercentage")
    var batteryPercentage: String = "",

    )