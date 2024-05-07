package com.izontechnology.dcapp.data.common

import android.net.wifi.ScanResult
import androidx.annotation.DrawableRes
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.utils.WidgetsViewModel

data class ScannedWifiItem(
    var wifiResult: ScanResult? = null,
    var isConnected: Boolean = false,
    var isConnecting: Boolean = false
) : WidgetsViewModel {
    override fun layoutId(): Int {
        return R.layout.item_wifi
    }
}

data class WifiHeader(
    var title: String? = ""
) : WidgetsViewModel {
    override fun layoutId(): Int {
        return R.layout.item_wifi_header
    }
}

data class SaveWifi(
    var SSID:String,
    var password:String
)

data class SimNetwork(
    var strength:Int?=0,
    var simSlot:Int?=-1,
    var provider:String? = "",
    var networkType:String? = "",
    var isRegistered:Boolean? = false,
    @DrawableRes var icon:Int = R.drawable.ic_sim_not_available,
    var isInternetConnected:Boolean? = false,
    var isAvailableForInternet:Boolean? = false
):WidgetsViewModel {
    override fun layoutId(): Int {
        return R.layout.item_wifi
    }
}