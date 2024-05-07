package com.izontechnology.dcapp.data.request


import com.google.gson.annotations.SerializedName

data class SimStatusRequest(
    @SerializedName("operatorCode")
    val operatorCode: String? = null,
    @SerializedName("appversion")
    val appversion: String? = null,
    @SerializedName("operatorName")
    val operatorName: String? = null,
    @SerializedName("simStatus")
    val simStatus: String? = null,
    @SerializedName("uniqueId")
    val uniqueId: String ? = null,
    @SerializedName("deviceserialno")
    val deviceserialno: String ? = "",
    @SerializedName("deviceuniqueid")
    val deviceUniqueId: String ? = null,
    @SerializedName("uniqueiderror")
    val uniqueiderror: String? = null,
    @SerializedName("deviceimei")
    var deviceimei: String? = null,
    @SerializedName("devicemanufacturer")
    var devicemanufacturer: String? = null,
    @SerializedName("devicemodel")
    var devicemodel: String? = null,
    @SerializedName("deviceos")
    var deviceos: String? = null,
)