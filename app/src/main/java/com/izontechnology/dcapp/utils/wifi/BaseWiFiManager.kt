package com.izontechnology.dcapp.utils.wifi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.izontechnology.dcapp.utils.LegacyUtils

// This open class is created for base wifi functionality init
open class BaseWiFiManager(context: Context) {
    init {
        mContext = context
        mWifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        wifiScanReceiver = object : WiFiManager.NetworkBroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                super.onReceive(context, intent)
            }
        }

        wifiReceiver = object : WiFiManager.NetworkBroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                super.onReceive(context, intent)
            }
        }
//        mContext?.registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        mContext?.registerReceiver(wifiReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        mContext?.registerReceiver(wifiReceiver, IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        mContext?.registerReceiver(wifiReceiver, IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
    }

    fun openWiFi(activity: Activity?) {
        if (!isWifiEnabledValue) {
            if (LegacyUtils.isDeviceOwner(activity)) {
                mWifiManager?.let {
                    @Suppress("DEPRECATION")
                    it.isWifiEnabled = true
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val settingsIntent = Intent(Settings.Panel.ACTION_WIFI)
                activity?.startActivity(settingsIntent)
            } else {
                mWifiManager?.let {
                    @Suppress("DEPRECATION")
                    it.isWifiEnabled = true
                }
                try {
                    mContext?.registerReceiver(
                        wifiScanReceiver,
                        IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun openWiFi(activity: Context?) {
        if (!isWifiEnabledValue) {
            if (LegacyUtils.isDeviceOwner(activity)) {
                mWifiManager?.let {
                    @Suppress("DEPRECATION")
                    it.isWifiEnabled = true
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val settingsIntent = Intent(Settings.Panel.ACTION_WIFI)
                activity?.startActivity(settingsIntent)
            } else {
                mWifiManager?.let {
                    @Suppress("DEPRECATION")
                    it.isWifiEnabled = true
                }
                try {
                    mContext?.registerReceiver(
                        wifiScanReceiver,
                        IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun closeWiFi(activity: Activity?) {
        if (isWifiEnabledValue) {
            if (LegacyUtils.isDeviceOwner(activity)) {
                mWifiManager?.let {
                    @Suppress("DEPRECATION")
                    it.isWifiEnabled = false
                }
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val settingsIntent = Intent(Settings.Panel.ACTION_WIFI)
                activity?.startActivity(settingsIntent)
            } else {
                Companion.mWifiManager?.let {
                    @Suppress("DEPRECATION")
                    it.isWifiEnabled = false
                }
                try {
                    mContext?.unregisterReceiver(wifiScanReceiver)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    fun closeWiFi(activity: Context?) {
        if (isWifiEnabledValue) {
            if (LegacyUtils.isDeviceOwner(activity)) {
                mWifiManager?.let {
                    @Suppress("DEPRECATION")
                    it.isWifiEnabled = false
                }
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val settingsIntent = Intent(Settings.Panel.ACTION_WIFI)
                activity?.startActivity(settingsIntent)
            } else {
                Companion.mWifiManager?.let {
                    @Suppress("DEPRECATION")
                    it.isWifiEnabled = false
                }
                try {
                    mContext?.unregisterReceiver(wifiScanReceiver)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    fun setOpenNetwork(ssid: String): Int? {
        if (TextUtils.isEmpty(ssid)) {
            return -1
        }
        val wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid)
        return if (null == wifiConfiguration) {
            val wifiConfig = WifiConfiguration()
            wifiConfig.SSID = addDoubleQuotation(ssid)
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            wifiConfig.allowedAuthAlgorithms.clear()
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            addNetwork(wifiConfig)
        } else {
            wifiConfiguration.networkId
        }
    }

    fun setWEPNetwork(ssid: String, password: String, isModify: Boolean): Int? {
        if (TextUtils.isEmpty(ssid)) {
            return -1
        }
        val wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid)
        return if (isModify) {
            wifiConfiguration?.wepKeys?.set(0, "\"" + password + "\"")
            wifiConfiguration?.let { updateNetwork(it) }
        } else {
            if (null == wifiConfiguration) {
                val wifiConfig = WifiConfiguration()
                wifiConfig.SSID = addDoubleQuotation(ssid)
                wifiConfig.wepKeys[0] = "\"" + password + "\""
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                addNetwork(wifiConfig)
            } else wifiConfiguration.networkId
        }
    }

    fun setWPA2Network(ssid: String, password: String, isModify: Boolean): Int? {
//        if (TextUtils.isEmpty(ssid)) {
//            return -1
//        }
        val wifiConfiguration = getConfigFromConfiguredNetworksBySsid(ssid)
        return if (isModify) {
            wifiConfiguration?.preSharedKey = "\"" + password + "\""
            wifiConfiguration?.let { updateNetwork(it) }
        } else {
            if (null == wifiConfiguration) {
                val wifiConfig = WifiConfiguration()
                wifiConfig.SSID = addDoubleQuotation(ssid)
                wifiConfig.preSharedKey = "\"" + password + "\""
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                wifiConfig.status = WifiConfiguration.Status.ENABLED
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                addNetwork(wifiConfig)
            } else {
                wifiConfiguration.networkId
            }
        }
    }

    fun getConfigFromConfiguredNetworksBySsid(ssid1: String): WifiConfiguration? {
        var ssid = ssid1
        ssid = addDoubleQuotation(ssid)
        val existingConfigs = configuredNetworks
        if (null != existingConfigs) {
            for (existingConfig in existingConfigs) {
                if (existingConfig.SSID == ssid) {
                    return existingConfig
                }
            }
        }
        return null
    }

    private fun isConnectedToWifiOf(networkSSID: String): Boolean {
        var isConnected = false
        if (networkSSID.isNotBlank()) {
            getSSIDOfConnectedWifi()?.let { ssid ->
                if (ssid == networkSSID) {
                    isConnected = true
                }
            }
        }
        return isConnected
    }

    fun getWifiInfoOf(networkSSID: String): WifiInfo? {
        var wifiInfo: WifiInfo? = null
        if (isConnectedToWifiOf(networkSSID)) {
            connectionInfo?.let { info ->
                wifiInfo = info
            }
        }
        return wifiInfo
    }

    fun getWifiDHCPInfoOf(): DhcpInfo? {
        var wifiInfo: DhcpInfo? = null
        if (isWifiEnabledValue) {
            wifiInfo = mWifiManager?.dhcpInfo
        }
        return wifiInfo
    }

    fun getSSIDOfConnectedWifi(): String? {
        var ssid: String? = ""
        if (isWifiConnected) {
            connectionInfo?.let { info ->
                if (info.ssid.isNotBlank()) {
                    ssid = info.ssid.substring(1, info.ssid.length - 1)
                }
            }
        }
        return ssid
    }

    val isWifiEnabledValue: Boolean
        get() = null != mWifiManager && mWifiManager?.isWifiEnabled == true

    val isWifiConnected: Boolean
        get() {
            if (null != mConnectivityManager) {
                val networkInfo = mConnectivityManager?.activeNetworkInfo
                return null != networkInfo && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI
            }
            return false
        }

    fun hasNetwork(): Boolean {
        if (null != mConnectivityManager) {
            val networkInfo = mConnectivityManager?.activeNetworkInfo
            return networkInfo != null && networkInfo.isAvailable
        }
        return false
    }

    val connectionInfo: WifiInfo?
        get() = if (null != mWifiManager) {
            mWifiManager?.connectionInfo
        } else null

    fun getWifiConnectionInfo(): Int? {
        return connectionInfo?.rssi?.let { calculateSignalLevel(it) }
    }

    fun startScan() {
        if (null != mWifiManager) {
            mContext?.registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            mWifiManager?.startScan()
        }
    }

    val scanResults: List<ScanResult>?
        get() =
            if (null != mWifiManager) {
                mWifiManager?.scanResults
            } else null

    @get:SuppressLint("MissingPermission")
    val configuredNetworks: List<WifiConfiguration>?
        get() = if (null != mWifiManager) {
            mWifiManager?.configuredNetworks
        } else null

    fun saveConfiguration(): Boolean {
        return null != mWifiManager && mWifiManager?.saveConfiguration() == true
    }

    fun enableNetwork(networkId: Int?): Boolean {
        if (null != mWifiManager) {
            val isDisconnect = disconnectCurrentWifi()
            val isEnableNetwork = networkId?.let { mWifiManager?.enableNetwork(it, true) }
            val isSave = mWifiManager?.saveConfiguration()
            val isReconnect = mWifiManager?.reconnect()
            return isDisconnect && isEnableNetwork == true && isSave == true && isReconnect == true
        }
        return false
    }

    private fun addNetwork(wifiConfig: WifiConfiguration): Int? {
        var networkId:Int? = -1
        if (null != mWifiManager) {
            networkId = mWifiManager?.addNetwork(wifiConfig)
            if (-1 != networkId) {
                val isSave = mWifiManager?.saveConfiguration()
                if (isSave == true) {
                    return networkId
                }
            }
        }
        return networkId?:-1
    }

    private fun updateNetwork(wifiConfig: WifiConfiguration): Int? {
        if (null != mWifiManager) {
            val networkId = mWifiManager?.updateNetwork(wifiConfig)
            if (-1 != networkId) {
                val isSave = mWifiManager?.saveConfiguration()
                if (isSave == true) {
                    return networkId
                }
            }
        }
        return -1
    }

    fun disconnectCurrentWifi(): Boolean {
        val wifiInfo = connectionInfo
        return if (null != wifiInfo && mWifiManager != null) {
            val isDisable = mWifiManager?.disableNetwork(wifiInfo.networkId)
            val isDisconnect = mWifiManager?.disconnect()
            return isDisable == true && isDisconnect == true
        } else {
            false
        }
    }

    fun deleteConfig(ssid: String): Boolean {
        if (null != mWifiManager) {
            val list: List<WifiConfiguration> = mWifiManager?.configuredNetworks as List<WifiConfiguration>
            list.forEach {
                if (addDoubleQuotation(ssid) == it.SSID) {
                    val isRemove = mWifiManager?.removeNetwork(it.networkId)
                    val isSave = mWifiManager?.saveConfiguration()
                    return isRemove == true && isSave == true
                }
            }
        }
        return false
    }

    fun calculateSignalLevel(rssi: Int): Int {
        return WifiManager.calculateSignalLevel(rssi, 5)
    }

    fun getSecurityMode(scanResult: ScanResult): SecurityModeEnum {
        val capabilities = scanResult.capabilities
        return if (capabilities.contains("WPA")) {
            SecurityModeEnum.WPA
        } else if (capabilities.contains("WEP")) {
            SecurityModeEnum.WEP
        }else if (capabilities.contains("WPA2")) {
            SecurityModeEnum.WPA2
        }
        else if (capabilities.contains("WPA_EAP")) {
            SecurityModeEnum.WPA_EAP
        }
        else if (capabilities.contains("IEEE8021X")) {
            SecurityModeEnum.IEEE8021X
        }
        else {
            SecurityModeEnum.OPEN
        }
    }

    fun addDoubleQuotation(text: String): String {
        return if (TextUtils.isEmpty(text)) {
            ""
        } else "\"" + text + "\""
    }

    fun excludeRepetition(scanResults: List<ScanResult>): ArrayList<ScanResult?> {
        val hashMap = HashMap<String, ScanResult>()
        for (scanResult in scanResults) {
            val ssid = scanResult.SSID
            if (TextUtils.isEmpty(ssid)) {
                continue
            }
            val tempResult = hashMap[ssid]
            if (null == tempResult) {
                hashMap[ssid] = scanResult
                continue
            }
            if (WifiManager.calculateSignalLevel(
                    tempResult.level,
                    100
                ) < WifiManager.calculateSignalLevel(scanResult.level, 100)
            ) {
                hashMap[ssid] = scanResult
            }
        }
        val results = ArrayList<ScanResult?>()
        for ((_, value) in hashMap) {
            results.add(value)
        }
        return results
    }

    companion object {
        var mWifiManager: WifiManager? = null
        private var mConnectivityManager: ConnectivityManager? = null
        lateinit var wifiScanReceiver: WiFiManager.NetworkBroadcastReceiver
        lateinit var wifiReceiver: WiFiManager.NetworkBroadcastReceiver

        var mContext: Context? = null
    }
}