package com.izontechnology.dcapp.presentation.host_activity

import android.content.ContentResolver
import android.content.Context
import android.net.wifi.ScanResult
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import com.izontechnology.dcapp.base.BaseViewModel
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import com.izontechnology.dcapp.utils.DeviceLogs
import com.izontechnology.dcapp.utils.HUNDRED
import com.izontechnology.dcapp.utils.TENTHOUSAND
import com.izontechnology.dcapp.utils.THARTY_THOUSAND
import com.izontechnology.dcapp.utils.WidgetsViewModel
import com.izontechnology.dcapp.utils.wifi.listener.OnWifiConnectListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HostActivityVM @Inject constructor(val repository: DeviceInfoRepository) : BaseViewModel() {
    val wifiNetworksList: ArrayList<WidgetsViewModel> = arrayListOf()
    var connectingWifiSSID = ""
    var wifiScanRunnable: Runnable? = null

    // this fields are used for managing timer at pinentry module
    var timerText = MutableLiveData<String>("0.0")
    var isTimerTicking = MutableLiveData(false)
    var isWifiConnecting = MutableLiveData(false)
    var isWifiScanning = MutableLiveData(false)
    val isWifiConnected = MutableLiveData<Boolean>()
    val isPermissionGranted = MutableLiveData<Boolean>()
    var handlerWifiScan: Handler? = null
    val wifiNetworkList = MutableLiveData<ArrayList<ScanResult?>>()

    var handlerDeviceStatus: Handler? = null
    var handlerBrighnessControl: Handler? = null

    var batteryPercentage = MutableLiveData(0)
    var deviceNames = MutableLiveData("")
    var RAMUsage = MutableLiveData("")
    var CPUUsage = MutableLiveData("")
    var isDeviceOnline = MutableLiveData(false)

    private val decimalFormat = java.text.DecimalFormat("0.00")

    /** This function is used to check wifi is enable or not*/
    fun isWifiEnabled() {
        isWifiEnabled.value = wifiManager.isWifiEnabledValue
    }

    fun getDeviceStatus() {
        handlerDeviceStatus = Handler(Looper.getMainLooper())
        var delay = HUNDRED // 1000 milliseconds == 1 second
        handlerDeviceStatus?.postDelayed(
            {
                isDeviceOnline.value = network.isConnected()
                delay = THARTY_THOUSAND
            }, delay.toLong()
        )
    }

    /** This function is used to start wifi scan*/
    fun startWifiScan(forceScan: Boolean = false) {
        wifiConnectListener()
        if (forceScan) {
            isWifiConnecting.value = false
            stopWifiScan()
        }
        if (handlerWifiScan == null) {
            handlerWifiScan = Handler(Looper.getMainLooper())
            var delay = HUNDRED // 1000 milliseconds == 1 second
            handlerWifiScan?.postDelayed(object : Runnable {
                override fun run() {
                    getScanWifiResults {
                        handlerWifiScan?.postDelayed(this, delay.toLong())
                    }
                    delay = TENTHOUSAND
                }
            }.apply {
                wifiScanRunnable = this
            }, delay.toLong()
            )
        }
    }

    private fun wifiConnectListener() {
        wifiManager.setOnWifiConnectListener(object : OnWifiConnectListener {
            override fun onWiFiConnectLog(log: String?) {
                DeviceLogs.e("wifi log", "wifi log onWiFiConnectLog ${log}")
            }

            override fun onWiFiConnectSuccess(SSID: String?) {
                Thread.sleep(500)
                isWifiConnected.value = true
                connectingWifiSSID = ""
                startWifiScan(true)
                DeviceLogs.e("wifi scan", "wifi scan 1")
                DeviceLogs.e("wifi log", "wifi log onWiFiConnectSuccess ${SSID}")
            }

            override fun onWiFiConnectFailure(SSID: String?) {
                Thread.sleep(1000)
                startWifiScan(true)
                DeviceLogs.e("wifi scan", "wifi scan 2")
                DeviceLogs.e("wifi log", "wifi log onWiFiConnectFailure ${SSID}")
            }

            override fun onWiFiDisconnectSuccess(SSID: String?) {
                Thread.sleep(1000)
                if (isWifiConnecting.value == false) {
                DeviceLogs.e("wifi scan", "wifi scan 3")
                    startWifiScan(true)
                }
                DeviceLogs.e("wifi log", "wifi log onWiFiDisconnectSuccess ${SSID}")
            }

        })
    }

    fun stopWifiScan() {
        wifiScanRunnable?.let { handlerWifiScan?.removeCallbacks(it) }
        handlerWifiScan?.removeCallbacks {}
        handlerWifiScan = null
    }

    /** This function is used to get scan wifi result*/
    fun getScanWifiResults(scanComplete: (() -> Unit)? = null) {
        if ((isWifiConnecting.value == false) and (isWifiScanning.value == false) and (isWifiEnabled.value == true)) {
            isWifiScanning.value = true
            wifiManager.startScan()
            wifiManager.setOnWifiScanResultsListener { scanResults ->
                isWifiScanning.value = false
                wifiNetworkList.value = scanResults?.let { wifiManager.excludeRepetition(it) }
                scanComplete?.invoke()
            }
        }else{
            scanComplete?.invoke()
        }
    }

    fun getRAMUsage(): String {
        val memoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memoryInfo)
        val usedMemory = memoryInfo.getTotalPss().toFloat() / (1024 * 1024)
        RAMUsage.value = "${decimalFormat.format(usedMemory)} MB"

        return "${decimalFormat.format(usedMemory)} MB"
    }

    fun getCPUUsage(): String {
        val pid = Process.myPid()
        val cpuInfo = Debug.threadCpuTimeNanos()
        val elapsedTime = System.nanoTime() - cpuInfo
        val cpuUsage = elapsedTime / 1000000.0 // Convert nanoseconds to milliseconds
        CPUUsage.value = "${decimalFormat.format(cpuUsage)} ms"
        return "${decimalFormat.format(cpuUsage)} ms"

    }

    fun getCurrentBrightness(context: Context): Int {
        val contentResolver: ContentResolver = context.contentResolver

        // Get the current screen brightness mode
        val brightnessMode = Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        )

        // Get the current screen brightness value
        val brightness: Int =
            if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                // If automatic mode, you might choose a default value
                50 // Replace with your default value
            } else {
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0)
            }

        return brightness
    }
}