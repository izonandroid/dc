package com.izontechnology.dcapp.data.response


import com.google.gson.annotations.SerializedName

//{"message":"Success","iscartroundrunning":false,"izonrounddata":{"isfileuploaded":false,"isroundended":false,"roundno":1},"error":{"code":"","message":""}}
data class DeviceSettingResponse(
    @SerializedName("error")
    var error: Error = Error(),
    @SerializedName("iscartroundrunning")
    var iscartroundrunning: Boolean = false,
    @SerializedName("message")
    var message: String = "",
    @SerializedName("izonkiosk")
    var izonkiosk: Izonkiosk? = null,
    @SerializedName("izonrounddata")
    var izonrounddata: Izonrounddata? = null,
)

data class Izonrounddata(
    @SerializedName("isfileuploaded")
    var isfileuploaded: Boolean? = null,
    @SerializedName("isroundended")
    var isroundended: Boolean? = null,
    @SerializedName("roundno")
    var roundno: Int? = null
)

data class Izonkiosk(
    @SerializedName("exitcode")
    var exitcode: String? = null
)