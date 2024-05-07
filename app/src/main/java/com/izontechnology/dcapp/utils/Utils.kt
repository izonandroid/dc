package com.izontechnology.dcapp.utils

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.DownloadManager
import android.content.Context
import android.telephony.TelephonyManager
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.izontechnology.dcapp.base.BaseApplication
import com.izontechnology.dcapp.data.common.ScannedWifiItem
import com.izontechnology.dcapp.data.response.NetworkType
import com.izontechnology.dcapp.downloadmanagerplus.FileItem
import com.izontechnology.dcapp.downloadmanagerplus.classes.Downloader
import com.izontechnology.dcapp.presentation.host_activity.HostActivityVM
import com.izontechnology.dcapp.utils.wifi.WiFiManager
import com.izontechnology.dcapp.utils.wifi.listener.OnWifiConnectListener
import com.izontechnology.dcapp.worker.NightlyUpdateWorker
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit


private const val regex = "^[A-Z0-9]" //alpha-numeric uppercase

fun String?.isUpperCase(): Boolean {
    return this?.chars()?.noneMatch(Character::isLowerCase) == true;
//    return Pattern.compile(regex).matcher(str).find()
}

fun NetworkType?.getDownloadAllowNetwork(): Int {
    var allowNetwork = DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
//    if (this?.simnetwork?.download == true) {
//        allowNetwork = allowNetwork or DownloadManager.Request.NETWORK_MOBILE
//    } else if (this?.wifinetwork?.download == true) {
//        allowNetwork = allowNetwork or DownloadManager.Request.NETWORK_WIFI
//    }
    return allowNetwork
}

fun NetworkType?.isDownloadSkip(context: Context): Boolean {
    return if (this?.priority?.isEmpty() == true) {
        true
    } else if (this?.priority.equals("both", true)) {
        false
    } else !this?.priority.equals(context.getConnectedNetworkType(), true)
}

fun connectToWifi(
    item: ScannedWifiItem,
    password: String?,
    wiFiManager: WiFiManager,
    mainVM: HostActivityVM,
    onStartConnect: () -> Unit,
    onConnectSuccess: () -> Unit,
    onConnectFail: () -> Unit
) {
    onStartConnect.invoke()
    mainVM.isWifiConnecting.value = true
    mainVM.connectingWifiSSID = item.wifiResult?.SSID.toString()
    item.isConnecting = true
    if ((item.wifiResult?.capabilities?.contains(WPA) == true) or (item.wifiResult?.capabilities?.contains(
            WPA2
        ) == true) or (item.wifiResult?.capabilities?.contains(WPA_EAP) == true)
    ) {
        wiFiManager.connectWPA2Network(
            item.wifiResult?.SSID ?: "",
            password ?: "",
            false
        )
    } else if ((item.wifiResult?.capabilities?.contains(WEP) == true)) {
        wiFiManager.connectWEPNetwork(
            item.wifiResult?.SSID ?: "",
            password ?: "",
            false
        )
    } else {
        wiFiManager.connectOpenNetwork(item.wifiResult?.SSID ?: "")
    }
    mainVM.wifiManager.setOnWifiConnectListener(object : OnWifiConnectListener {
        override fun onWiFiConnectLog(log: String?) {

        }

        override fun onWiFiConnectSuccess(SSID: String?) {
            onConnectSuccess.invoke()
        }

        override fun onWiFiConnectFailure(SSID: String?) {
            onConnectFail.invoke()
        }

        override fun onWiFiDisconnectSuccess(SSID: String?) {

        }

    })
}

fun Context.isAppIsInBackground(): Boolean {
    var isInBackground = true
    val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningProcesses = am.runningAppProcesses
    for (processInfo in runningProcesses) {
        if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            for (activeProcess in processInfo.pkgList) {
                if (activeProcess == this.packageName) {
                    isInBackground = false
                }
            }
        }
    }
    return isInBackground
}

fun Context.isIzonGolfAppRunning(): Boolean {
    var isInBackground = false
    try{
    val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningProcesses = am.runningAppProcesses
    for (processInfo in runningProcesses) {
        if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            for (activeProcess in processInfo.pkgList) {
                if (activeProcess == izonPackageName) {
                    isInBackground = true
                }
            }
        }
    }}catch (e:Exception){
        e.printStackTrace()
    }
    return isInBackground
}

fun getSimStatus(simState: Int): String {

    when (simState) {
        TelephonyManager.SIM_STATE_ABSENT -> {
            return "Absent"
        }

        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> {
            return "Locked"
        }

        TelephonyManager.SIM_STATE_PIN_REQUIRED -> {
            return "Pin Required"
        }

        TelephonyManager.SIM_STATE_PUK_REQUIRED -> {
            return "Puk Required"
        }

        TelephonyManager.SIM_STATE_READY -> {
            return "Active"
        }

        else -> {
            return "Unknown"
        }
    }
}


fun scheduleWork(hour: Int, minute: Int) {
    val calendar: Calendar = Calendar.getInstance()
    val nowMillis: Long = calendar.getTimeInMillis()
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    if (calendar.before(Calendar.getInstance())) {
        calendar.add(Calendar.DATE, 1)
    }
    var diff: Long = calendar.getTimeInMillis() - nowMillis
    if (hour == -1) {
        diff = 60000
    }
    val mWorkManager = WorkManager.getInstance(BaseApplication.INSTANCE.applicationContext)
    val constraints: Constraints = Constraints.Builder()
        .build()
    mWorkManager.cancelAllWorkByTag(WORK_TAG)
    val mRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(NightlyUpdateWorker::class.java)
        .setConstraints(constraints)
        .setInitialDelay(diff, TimeUnit.MILLISECONDS)
        .addTag(WORK_TAG)
        .build()
//    mWorkManager.enqueueUniqueWork(WORK_TAG,ExistingWorkPolicy.REPLACE,mRequest)
    mWorkManager.enqueue(mRequest)
}
//fun scheduleNightlyUpdate() {
//    val constraints = Constraints.Builder()
//        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
//        .build()
//
//    val nightlyUpdateRequest = PeriodicWorkRequestBuilder<NightlyUpdateWorker>(
//        repeatInterval = 1, // Repeat every 1 day
//        repeatIntervalTimeUnit = TimeUnit.MINUTES
//    )
//        .setConstraints(constraints)
//        .build()
//
//    WorkManager.getInstance(BaseApplication.INSTANCE.applicationContext).enqueueUniquePeriodicWork("nightupdate",ExistingPeriodicWorkPolicy.UPDATE,nightlyUpdateRequest)
//}

fun Context?.isFileDownloaded(currentItem: FileItem, filePath: String): Boolean {
    try {
        val downloadItem = Downloader.getDownloadItem(this, currentItem.token)
        if (!downloadItem.filePath.isNullOrEmpty()) {
            return File(downloadItem.filePath).length() == currentItem.fileSize.toLong()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    try {
        return if (filePath.isNotEmpty()) {
            if (File(filePath).exists()) {
                File(filePath).length() == currentItem.fileSize.toLong()
            } else {
                false
            }
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}