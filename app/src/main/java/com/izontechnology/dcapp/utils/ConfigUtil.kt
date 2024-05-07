package com.izontechnology.dcapp.utils

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE
import android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream


/**
 * Before call this function provide device admin premission to application and execute below command in terminal using ADB
 * factory reset your device first
 * install this application
 * give all permission allow
 * run below commands in terminal
 * adb shell
 * dpm set-device-owner com.izontechnology.dcapp/.ControlDeviceAdminReceiver
 */
//fun rebootDevice(context: Context) {
//    try {
//        val devicePolicyManager =
//            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        val componentName = ComponentName(context, ControlDeviceAdminReceiver::class.java)
//        devicePolicyManager.reboot(componentName)
//    } catch (ex: java.lang.Exception) {
//        ex.printStackTrace()
//    }
//}

fun installApplication(context: Context, file: File, packageName: String, version: String?) {
//    if (packageName == context.getPackageName() &&
//        context.getPackageManager()
//            .getLaunchIntentForPackage(LAUNCHER_RESTARTER_PACKAGE_ID) != null
//    ) {
//        // Restart self in EMUI: there's no auto restart after update in EMUI, we must use a helper app
//        startLauncherRestarter()
//    }
    val versionData = if (version == null || version == "0") "" else " $version"
    if (isDeviceOwner(context)) {
        silentInstallApplication(
            context,
            file,
            packageName,
            object : InstallErrorHandler {
                override fun onInstallError() {
//                    if (file.exists()) {
//                        file.delete()
//                    }
                }
            })
    } else {
        requestInstallApplication(context, file, object : InstallErrorHandler {
            override fun onInstallError() {
//                if (file.exists()) {
//                    file.delete()
//                }
            }
        })
    }
}

fun requestInstallApplication(context: Context, file: File, errorHandler: InstallErrorHandler?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        // Let's set Intent.FLAG_ACTIVITY_NEW_TASK here
        // Some devices report:
        // android.util.AndroidRuntimeException
        // Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    } else {
        val apkUri = Uri.fromFile(file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}

fun silentInstallApplication(
    context: Context,
    file: File,
    packageName: String,
    errorHandler: InstallErrorHandler
) {
    try {
        val `in` = FileInputStream(file)
        val packageInstaller = context.packageManager.packageInstaller
        val params = SessionParams(
            SessionParams.MODE_FULL_INSTALL
        )
        params.setAppPackageName(packageName)
        // set params
        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)
        val out = session.openWrite("COSU", 0, -1)
        val buffer = ByteArray(65536)
        var c: Int
        while (`in`.read(buffer).also { c = it } != -1) {
            out.write(buffer, 0, c)
        }
        session.fsync(out)
        `in`.close()
        out.close()
        session.commit(
            createInstallSender(
                context,
                sessionId,
                packageName
            )
        )
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        errorHandler.onInstallError()
    }
}

fun uninstallApplication(context: Context, packageName: String) {
    if (isDeviceOwner(context)) {
        silentUninstallApplication(context, packageName)
    } else {
        requestUninstallApplication(context, packageName)
    }
}

fun isDeviceOwner(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    return dpm.isDeviceOwnerApp(context.packageName)
}

fun requestUninstallApplication(context: Context, packageName: String) {
    val packageUri = Uri.parse("package:$packageName")
    val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
    // Let's set Intent.FLAG_ACTIVITY_NEW_TASK here
    // Some devices report:
    // android.util.AndroidRuntimeException
    // Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
        context.startActivity(intent)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun silentUninstallApplication(context: Context, packageName: String?) {
    val packageInstaller = context.packageManager.packageInstaller
    try {
        packageName?.let { packageInstaller.uninstall(it, createUninstallSender(context, 0, null)) }
    } catch (e: Exception) {
        // If we're trying to remove an unexistent app, it causes an exception so just ignore it
    }
}

fun createUninstallSender(context: Context?, sessionId: Int, packageName: String?): IntentSender {
    val intent = Intent(ACTION_UNINSTALL_COMPLETE)
    if (packageName != null) {
        intent.putExtra(PACKAGE_NAME, packageName)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        sessionId,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )
    return pendingIntent.intentSender
}

fun createInstallSender(context: Context?, sessionId: Int, packageName: String?): IntentSender {
    val intent = Intent(ACTION_INSTALL_COMPLETE)
    if (packageName != null) {
        intent.putExtra(PACKAGE_NAME, packageName)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        sessionId,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )
    return pendingIntent.intentSender
}

interface InstallErrorHandler {
    fun onInstallError()
}

fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Context.setScreenTimeOut(screenOffTimeout:Int = 300000) {
    try {
        Settings.System.putInt(getContentResolver(),
            Settings.System.SCREEN_OFF_TIMEOUT, screenOffTimeout);
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.setVolume(volume:Int){
    try {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val percent = volume * 0.01
        val actVolume = (maxVolume?.times(percent))?.toInt()
        audioManager?.setStreamVolume(AudioManager.STREAM_SYSTEM, actVolume?:30, 0)
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, actVolume?:30, 0)
    }catch (e:Exception){
        e.printStackTrace()
    }
}

fun Context.setBrightness(brightness:Int) {
    try {
        val settingsCanWrite = Settings.System.canWrite(this)
        if (!settingsCanWrite) {
            /** If do not have write settings permission then open the Can modify system settings panel. */
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            startActivity(intent)
        } else {
            val percent = brightness * 0.01
            val actBrightness = (getMaxBrightness().times(percent)).toInt()
            Settings.System.putInt(
                this.contentResolver,
                SCREEN_BRIGHTNESS_MODE,
                SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                this.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                actBrightness
            )
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}