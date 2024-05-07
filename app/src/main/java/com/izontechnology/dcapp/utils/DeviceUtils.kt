package com.izontechnology.dcapp.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.izontechnology.dcapp.base.common.Network
import com.izontechnology.dcapp.base.common.SharedPrefs

@SuppressLint("MissingPermission")
fun Context.getIccId(): Pair<String, String> {

    try {
//        val tm2 = getSystemService(TELECOM_SERVICE) as TelecomManager
//
//        val phoneAccounts: Iterator<PhoneAccountHandle> =
//            tm2.callCapablePhoneAccounts.listIterator()
//        val phoneAccountHandle = phoneAccounts.next()
//
//        return Pair(phoneAccountHandle.id.substring(0, 19), "")

        val mTelephonyManager: TelephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val iccid = mTelephonyManager.simSerialNumber
        return Pair(iccid, "")
    } catch (e: Exception) {
        return Pair("123456789", "${e.message}")
    }
}

fun Context.getImei(): String {
    return try {
        val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        telephonyManager?.deviceId ?: ""
    } catch (e: Exception) {
        ""
    }
}

fun Context.getConnectedNetworkType(): String? {
    val network = Network(this)
    return if (network.isConnected()) {
        if (network.getNetworkInfo()
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        ) {
            "wi-fi"
        } else {
            "sim"
        }
    } else {
        null
    }
}

fun Context.getExtNo(): String {
    try {
        val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_NUMBERS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return ""
        }
        return telephonyManager?.line1Number ?: ""
    } catch (e: Exception) {
        return ""
    }
}


fun String?.toLocalInt(): Int {
    return try {
        if (this.isNullOrEmpty()) {
            0
        } else {
            this.toInt()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}

fun Context.getBrightnessFactor(): Float {
    return try {
        val powerManager = this.getSystemService(Application.POWER_SERVICE)
        (powerManager.javaClass.getDeclaredField("BRIGHTNESS_ON")
            .getInt(powerManager) / 100).toFloat()
    } catch (e: Exception) {
        (255 / 100).toFloat()
    }

}

fun Context.getMaxBrightness(): Int {
    val powerManager = this.getSystemService(Application.POWER_SERVICE)
    return powerManager.javaClass.getDeclaredField("BRIGHTNESS_ON").getInt(powerManager)
}

fun Context.getBatteryPer(): Int {
    val bm = this.getSystemService(Application.BATTERY_SERVICE) as BatteryManager
    val batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    return batLevel
}

fun Context.getCurrentBrightness(): Int {
    val contentResolver: ContentResolver = this.contentResolver

    // Get the current screen brightness mode
    val brightnessMode = Settings.System.getInt(
        contentResolver,
        Settings.System.SCREEN_BRIGHTNESS_MODE,
        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
    )

    // Get the current screen brightness value
    val brightness: Int =
//            if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
//                // If automatic mode, you might choose a default value
//                50 // Replace with your default value
//            } else {
        Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0)
//            }

    return brightness
}

fun getDeviceName(): String {
    return Build.MANUFACTURER
}

fun getDeviceModel(): String {
    return Build.MODEL
}

fun getFirmwareVersion(): String {
    return Build.VERSION.INCREMENTAL
}

fun getDeviceBuildNumber(): String {
    return Build.DISPLAY
}

fun getDeviceSerial(): String {
    var serial: String = ""
    try {
        serial = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Build.getSerial()
        } else {
            Build.SERIAL
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    Log.d("service", "device info api serial ${serial}")
    return serial
}

fun getDeviceBuildDate(): String {
    return Build.TIME.getDate()
}

fun getDeviceOS(): String? {
    return Build.VERSION.RELEASE
}

fun Context?.getUsedMemorySize(): Triple<Long, Long, Long> {
    var mi = ActivityManager.MemoryInfo()
    this?.getSystemService(ActivityManager::class.java)?.getMemoryInfo(mi)
    var freeSize = 0L
    var totalSize = 0L
    var usedSize = -1L
    try {
        freeSize = (mi.availMem) / 1048576L
        totalSize = (mi.totalMem) / 1048576L
        usedSize = (totalSize - freeSize) / 1048576L
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return Triple(totalSize, freeSize, usedSize)
}

fun Context.getAndroidId(): String {
    val androidId =
        Settings.Secure.getString(
            getContentResolver(),
            android.provider.Settings.Secure.ANDROID_ID
        )
    return androidId
}

fun Context.getVolume(): Int {
    val audioManager = getSystemService(Application.AUDIO_SERVICE) as AudioManager
    return audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)
}

fun Context.getDeviceWifi(): String {
    return if (SharedPrefs(this).getIsWifiOn())
        "ON"
    else
        "OFF"
}