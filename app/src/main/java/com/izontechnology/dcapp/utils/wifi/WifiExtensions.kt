package com.izontechnology.dcapp.utils.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.DrawableRes
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.utils.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder
import kotlin.coroutines.resume


fun Context.wifiManager(): WifiManager? {
    return applicationContext.getSystemService(Context.WIFI_SERVICE)
            as? WifiManager ?: return null
}

fun Context.wifiConnectionInfo(): WifiInfo? {
    return wifiManager()?.connectionInfo
}

fun Context.isWifiEnabled(): Boolean {
    return wifiManager()?.isWifiEnabled ?: false
}

fun Context.turnOnWifi() {
    if(!isWifiEnabled()) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val settingsIntent = Intent(Settings.Panel.ACTION_WIFI)
            startActivity(settingsIntent)
        } else {
            wifiManager()?.let {
                @Suppress("DEPRECATION")
                it.isWifiEnabled = true
            }
        }
    }
}

fun Context.turnOffWifi() {
    if(isWifiEnabled()) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val settingsIntent = Intent(Settings.Panel.ACTION_WIFI)
            startActivity(settingsIntent)
        } else {
            wifiManager()?.let {
                @Suppress("DEPRECATION")
                it.isWifiEnabled = false
            }
        }
    }
}

fun Context.isConnectedToWifi(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION")
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }
}

fun Context.isConnectedToWifiOf(networkSSID: String?): Boolean {
    var isConnected = false
    if(networkSSID?.isNotBlank() == true) {
        getSSIDOfConnectedWifi()?.let { ssid ->
            if(ssid == networkSSID) {
                isConnected = true
            }
            Log.d("Context", "->isConnectedToWifiOf() " +
                    "ssid: $ssid, connected: $isConnected")
        }
    }
    return isConnected
}

fun Context.getWifiInfoOf(networkSSID: String): WifiInfo? {
    var wifiInfo: WifiInfo? = null
    if(isConnectedToWifiOf(networkSSID)) {
        wifiConnectionInfo()?.let { info ->
            wifiInfo = info
        }
    }
    return wifiInfo
}

fun Context.getSSIDOfConnectedWifi(): String? {
    var ssid: String? = null
    if(isConnectedToWifi()) {
        wifiConnectionInfo()?.let { info ->
            if(info.ssid.isNotBlank()) {
                ssid = info.ssid.substring(1, info.ssid.length - 1)
            }
        }
    }
    return ssid
}

fun Context.getIPAddressOfConnectedWifi(): String? {
    var ipAddress: String? = null
    if(isConnectedToWifi()) {
        wifiConnectionInfo()?.let { info ->
            var infoIPAddress: Int = info.ipAddress
            if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                infoIPAddress = Integer.reverseBytes(infoIPAddress)
            }
            val ipByteArray = BigInteger.valueOf(infoIPAddress.toLong()).toByteArray()
            ipAddress = try {
                InetAddress.getByAddress(ipByteArray).hostAddress
            } catch (e: UnknownHostException) {
                null
            }
        }
    }
    return ipAddress
}

fun getIPAddressFrom(wifiInfo: WifiInfo): String? {
    wifiInfo.let { info ->
        var infoIPAddress: Int = info.ipAddress
        if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            infoIPAddress = Integer.reverseBytes(infoIPAddress)
        }
        val ipByteArray = BigInteger.valueOf(infoIPAddress.toLong()).toByteArray()
        return try {
            InetAddress.getByAddress(ipByteArray).hostAddress
        } catch (e: UnknownHostException) {
            null
        }
    }
}

suspend fun Context.getWifiScanResults(): List<ScanResult> {
    val wifiManager = wifiManager() ?: return emptyList()
    return suspendCancellableCoroutine { continuation ->
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    unregisterReceiver(this)
                    continuation.resume(wifiManager.scanResults)
                }
            }
        }
        continuation.invokeOnCancellation {
            unregisterReceiver(wifiScanReceiver)
        }
        registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        @Suppress("DEPRECATION")
        wifiManager.startScan()
    }
}

/** This function is used to check network protected or not */
fun isProtectedNetwork(capability: String?): Boolean {
    return (capability?.contains(WPA) == true) || (capability?.contains(WPA2) == true) ||(capability?.contains(WPA_EAP) == true) ||(capability?.contains(
        IEEE8021X) == true) || (capability?.contains(WEP) == true)
}

/**
 * Get a good looking drawable resource to quickly identify
 * network's level signal and its security policy.
 *
 * @param level: RSSI of the scanned network
 * @param protected: false if the network does not require a password
 *
 * @return a drawable to represent the network to the user
 */
@DrawableRes
fun ScanResult?.getDrawableFromRSSI(): Int {
    val protected = isProtectedNetwork(this?.capabilities)
    val signal =this?.level?:0
    return if (protected) {
        when  {
            (signal >= MINUS_FIFTY)  -> R.drawable.ic_signal_wifi_4_bar_lock_black_24dp
            signal in MINUS_SIXTY..MINUS_FIFTY -> R.drawable.ic_signal_wifi_3_bar_lock_black_24dp
            signal in MINUS_SEVENTY..MINUS_SIXTY -> R.drawable.ic_signal_wifi_2_bar_lock_black_24dp
            else -> R.drawable.ic_signal_wifi_1_bar_lock_black_24dp
        }
    } else {
        when  {
            signal >= MINUS_FIFTY  -> R.drawable.ic_signal_wifi_4_bar_black_24dp
            signal in MINUS_SIXTY..MINUS_FIFTY -> R.drawable.ic_signal_wifi_3_bar_black_24dp
            signal in MINUS_SEVENTY..MINUS_SIXTY -> R.drawable.ic_signal_wifi_2_bar_black_24dp
            else  -> R.drawable.ic_signal_wifi_1_bar_black_24dp
        }
    }
}