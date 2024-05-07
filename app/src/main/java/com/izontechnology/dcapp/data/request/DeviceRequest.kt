package com.izontechnology.dcapp.data.request


import com.google.gson.annotations.SerializedName

data class DeviceRequest(
    @SerializedName("appversion")
    var appversion: String? = null,
    @SerializedName("wifiname")
    var wifiname: String? = null,
    @SerializedName("batterypercentage")
    var batterypercentage: String? = null,
    @SerializedName("brightnessvalue")
    var brightnessvalue: String? = null,
    @SerializedName("code")
    var code: String? = null,
    @SerializedName("devicebuildate")
    var devicebuildate: String? = null,
    @SerializedName("devicebuildnumber")
    var devicebuildnumber: String? = null,
    @SerializedName("deviceextno")
    var deviceextno: String? = null,
    @SerializedName("devicefwversion")
    var devicefwversion: String? = null,
    @SerializedName("deviceiccid")
    var deviceiccid: String? = null,
    @SerializedName("deviceimei")
    var deviceimei: String? = null,
    @SerializedName("devicemanufacturer")
    var devicemanufacturer: String? = null,
    @SerializedName("devicemodel")
    var devicemodel: String? = null,
    @SerializedName("deviceos")
    var deviceos: String? = null,
    @SerializedName("deviceuniqueid")
    var deviceuniqueid: String? = null,
    @SerializedName("devicevolume")
    var devicevolume: String? = null,
    @SerializedName("izondevice")
    var izondevice: Any? = null,
    @SerializedName("latitude")
    var latitude: String? = null,
    @SerializedName("longitude")
    var longitude: String? = null,
    @SerializedName("maxbrightnessvalue")
    var maxbrightnessvalue: String? = null,
    @SerializedName("networkconnectiontype")
    var networkconnectiontype: String? = null,
    @SerializedName("networkstatus")
    var networkstatus: String? = null,
    @SerializedName("devicelog")
    var devicelog: String? = null,
    @SerializedName("deviceserialno")
    var deviceserialno: String? = null,
    @SerializedName("devicegpssignalstrength")
    var devicegpssignalstrength: String? = null,
    @SerializedName("devicewifitoggle")
    var devicewifitoggle: String? = null,
    @SerializedName("clientdatetime")
    var clientdatetime: String? = null
)