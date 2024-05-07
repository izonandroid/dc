package com.izontechnology.dcapp.utils.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Message
import com.izontechnology.dcapp.utils.DeviceLogs
import com.izontechnology.dcapp.utils.wifi.listener.OnWifiConnectListener
import com.izontechnology.dcapp.utils.wifi.listener.OnWifiEnabledListener
import com.izontechnology.dcapp.utils.wifi.listener.OnWifiScanResultsListener

// this class is created for wifi setup
class WiFiManager constructor(context: Context) : BaseWiFiManager(context) {

    fun connectOpenNetwork(ssid: String): Boolean {

        val networkId = setOpenNetwork(ssid)
        if (-1 != networkId) {

            val isSave = saveConfiguration()

            val isEnable = enableNetwork(networkId)
            return isSave && isEnable
        }
        return false
    }

    fun connectWEPNetwork(ssid: String, password: String, isModify: Boolean): Boolean {
        val networkId = setWEPNetwork(ssid, password, isModify)
        if (-1 != networkId) {

            val isSave = saveConfiguration()

            val isEnable = enableNetwork(networkId)
            return isSave && isEnable
        }
        return false
    }

    fun connectWPA2Network(ssid: String, password: String, isModify: Boolean): Boolean {
        val networkId = setWPA2Network(ssid, password, isModify)
        if (-1 != networkId) {

            val isSave = saveConfiguration()

            val isEnable = enableNetwork(networkId)
            return isSave && isEnable
        }
        return false
    }

    open class NetworkBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            when (intent.action) {
                WifiManager.WIFI_STATE_CHANGED_ACTION -> when (
                    intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN
                    )
                ) {
                    WifiManager.WIFI_STATE_ENABLING -> DeviceLogs.i(TAG, "onReceive: WIFI...")
                    WifiManager.WIFI_STATE_ENABLED -> {
                        DeviceLogs.i(TAG, "onReceive: WIFI WIFI_STATE_ENABLED")
                        mCallBackHandler.sendEmptyMessage(WIFI_STATE_ENABLED)
                    }

                    WifiManager.WIFI_STATE_DISABLING -> DeviceLogs.i(TAG, "onReceive: WIFI...")
                    WifiManager.WIFI_STATE_DISABLED -> {
                        DeviceLogs.i(TAG, "onReceive: WIFI WIFI_STATE_DISABLED")
                        mCallBackHandler.sendEmptyMessage(WIFI_STATE_DISABLED)
                    }

                    WifiManager.WIFI_STATE_UNKNOWN -> DeviceLogs.i(TAG, "onReceive: WIFI!")
                    else -> DeviceLogs.i(TAG, "onReceive: WIFI!")
                }

                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val isUpdated =
                            intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                        DeviceLogs.i(TAG, "onReceive:   " + if (isUpdated) " true" else "")
                    } else {
                    }
                    mContext?.unregisterReceiver(wifiScanReceiver)
                    DeviceLogs.i(TAG, "onReceive:scanning " + wifiManager.scanResults)
                    val scanResultsMessage = Message.obtain()
                    scanResultsMessage.what = SCAN_RESULTS_UPDATED
                    scanResultsMessage.obj = wifiManager.scanResults
                    mCallBackHandler.sendMessage(scanResultsMessage)
                }

                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
//                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//                    if (null != networkInfo && ConnectivityManager.TYPE_WIFI == networkInfo.getType()) {
//                    }
                    val wifiInfo = intent.getParcelableExtra<WifiInfo>(WifiManager.EXTRA_WIFI_INFO)
                    if (null != wifiInfo && wifiInfo.supplicantState == SupplicantState.COMPLETED) {
                        val ssid = wifiInfo.ssid
                        DeviceLogs.i(TAG, "onReceive: ssid = $ssid")
                    }
                }

                WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION -> {
                    val isConnected =
                        intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)
                    DeviceLogs.i(
                        TAG,
                        "onReceive: SUPPLICANT_CONNECTION_CHANGE_ACTION  isConnected = $isConnected"
                    )
                }

                WifiManager.SUPPLICANT_STATE_CHANGED_ACTION -> {

                    val supplicantState =
                        intent.getParcelableExtra<SupplicantState>(WifiManager.EXTRA_NEW_STATE)

                    val logMessage = Message.obtain()
                    logMessage.what = WIFI_CONNECT_LOG
                    logMessage.obj = supplicantState.toString()
//                    logMessage.obj = supplicantState.toString()
                    mCallBackHandler.sendMessage(logMessage)
                    when (supplicantState) {
                        SupplicantState.INTERFACE_DISABLED -> DeviceLogs.i(
                            TAG,
                            "onReceive: INTERFACE_DISABLED"
                        )

                        SupplicantState.INACTIVE -> {
                            DeviceLogs.i(TAG, "onReceive: INACTIVE")
                        }

                        SupplicantState.DISCONNECTED -> {
                            val connectFailureInfo = wifiManager.connectionInfo
                            DeviceLogs.i(
                                TAG,
                                "onReceive: DISCONNECTED  connectFailureInfo = $connectFailureInfo"
                            )
                            if (intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR,-1) ==WifiManager.ERROR_AUTHENTICATING){
                                val wifiConnectFailureMessage = Message.obtain()
                                wifiConnectFailureMessage.what = WIFI_CONNECT_FAILURE
                                wifiConnectFailureMessage.obj = connectFailureInfo.ssid
                                mCallBackHandler.sendMessage(wifiConnectFailureMessage)
                            }
                            if (null != connectFailureInfo) {
                                val wifiConnectFailureMessage = Message.obtain()
                                wifiConnectFailureMessage.what = WIFI_DISCONNECT_SUCCESS
                                wifiConnectFailureMessage.obj = connectFailureInfo.ssid
                                mCallBackHandler.sendMessage(wifiConnectFailureMessage)

//                                val networkId = connectFailureInfo.networkId
//                                val isDisable = wifiManager.disableNetwork(networkId)
//                                val isDisconnect = wifiManager.disconnect()
                            }
                        }

                        SupplicantState.SCANNING -> {
                            DeviceLogs.i(TAG, "onReceive: SCANNING")
                        }

                        SupplicantState.AUTHENTICATING -> {}
                        SupplicantState.ASSOCIATING -> {}
                        SupplicantState.ASSOCIATED -> {}
                        SupplicantState.FOUR_WAY_HANDSHAKE -> {}
                        SupplicantState.GROUP_HANDSHAKE -> {}
                        SupplicantState.COMPLETED -> {
                            val connectSuccessInfo = wifiManager.connectionInfo
                            DeviceLogs.i(
                                TAG,
                                "onReceive: WIFI_CONNECT_SUCCESS: // " + connectSuccessInfo.ssid
                            )
                            if (null != connectSuccessInfo) {
                                val wifiConnectSuccessMessage = Message.obtain()
                                wifiConnectSuccessMessage.what = WIFI_CONNECT_SUCCESS
                                wifiConnectSuccessMessage.obj = connectSuccessInfo.ssid
                                mCallBackHandler.sendMessage(wifiConnectSuccessMessage)
                            }
                        }

                        SupplicantState.DORMANT -> DeviceLogs.i(TAG, "onReceive: DORMANT:")
                        SupplicantState.UNINITIALIZED -> DeviceLogs.i(
                            TAG,
                            "onReceive: UNINITIALIZED: "
                        )

                        SupplicantState.INVALID -> DeviceLogs.i(TAG, "onReceive: INVALID:")
                        else -> {}
                    }
                }

                else -> {}
            }
        }
    }

    private class CallBackHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                WIFI_STATE_ENABLED -> if (null != mOnWifiEnabledListener) {
                    mOnWifiEnabledListener?.onWifiEnabled(true)
                }

                WIFI_STATE_DISABLED -> if (null != mOnWifiEnabledListener) {
                    mOnWifiEnabledListener?.onWifiEnabled(false)
                }

                SCAN_RESULTS_UPDATED -> if (null != mOnWifiScanResultsListener) {
                    val scanResults = msg.obj as List<ScanResult>
                    mOnWifiScanResultsListener?.onScanResults(scanResults)
                }

                WIFI_CONNECT_LOG -> if (null != mOnWifiConnectListener) {
                    val log = msg.obj as String
                    mOnWifiConnectListener?.onWiFiConnectLog(log)
                }

                WIFI_CONNECT_SUCCESS -> if (null != mOnWifiConnectListener) {
                    val ssid = msg.obj as String
                    mOnWifiConnectListener?.onWiFiConnectSuccess(ssid)
                }

                WIFI_CONNECT_FAILURE -> if (null != mOnWifiConnectListener) {
                    val ssid = msg.obj as String
                    mOnWifiConnectListener?.onWiFiConnectFailure(ssid)
                }

                WIFI_DISCONNECT_SUCCESS -> if (null != mOnWifiConnectListener) {
                    val ssid = msg.obj as String
                    mOnWifiConnectListener?.onWiFiDisconnectSuccess(ssid)
                }

                else -> {}
            }
        }
    }

    fun setOnWifiEnabledListener(listener: OnWifiEnabledListener?) {
        mOnWifiEnabledListener = listener
    }

    fun removeOnWifiEnabledListener() {
        mOnWifiEnabledListener = null
    }

    fun setOnWifiScanResultsListener(listener: OnWifiScanResultsListener?) {
        mOnWifiScanResultsListener = listener
    }

    fun removeOnWifiScanResultsListener() {
        mOnWifiScanResultsListener = null
    }

    fun setOnWifiConnectListener(listener: OnWifiConnectListener?) {
        mOnWifiConnectListener = listener
    }

    fun removeOnWifiConnectListener() {
        mOnWifiConnectListener = null
    }


    companion object {
        private const val TAG = "WIFI"
        private var mWiFiManager: WiFiManager? = null
        private val mCallBackHandler = CallBackHandler()
        private const val WIFI_STATE_ENABLED = 0
        private const val WIFI_STATE_DISABLED = 1
        private const val SCAN_RESULTS_UPDATED = 3
        private const val WIFI_CONNECT_LOG = 4
        private const val WIFI_CONNECT_SUCCESS = 5
        private const val WIFI_CONNECT_FAILURE = 6
        private const val WIFI_DISCONNECT_LOG = 7
        private const val WIFI_DISCONNECT_SUCCESS = 8
        private const val WIFI_DISCONNECT_FAILURE = 9
        fun getInstance(context: Context): WiFiManager? {
            if (null == mWiFiManager) {
                synchronized(WiFiManager::class.java) {
                    if (null == mWiFiManager) {
                        mWiFiManager = WiFiManager(context)
                    }
                }
            }
            return mWiFiManager
        }

        private var mOnWifiEnabledListener: OnWifiEnabledListener? = null
        private var mOnWifiScanResultsListener: OnWifiScanResultsListener? = null
        private var mOnWifiConnectListener: OnWifiConnectListener? = null
    }
}