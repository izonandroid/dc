package com.izontechnology.dcapp.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.UserManager
import android.util.Log
import com.izontechnology.dcapp.base.BaseApplication
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.presentation.host_activity.HostActivity
import com.izontechnology.dcapp.receiver.ControlDeviceAdminReceiver


object LegacyUtils {
    fun checkAdminMode(context: Context): Boolean {
        return try {
            val dpm =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponentName = getAdminComponentName(context)
            //            RemoteLogger.log(context, Const.LOG_DEBUG, "Admin component active: " + isAdminActive);
            dpm.isAdminActive(adminComponentName)
        } catch (e: Exception) {
            //            RemoteLogger.log(context, Const.LOG_WARN, "Failed to get device administrator status: " + e.getMessage());
            true
        }
    }

    fun factoryReset(context: Context): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            dpm.wipeData(0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun reboot(context: Context): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponentName = getAdminComponentName(context)
            dpm.reboot(adminComponentName)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getAdminComponentName(context: Context): ComponentName {
        return ComponentName(context.applicationContext, ControlDeviceAdminReceiver::class.java)
    }

    fun isDeviceOwner(context: Context?): Boolean {
        val dpm = context?.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    /**
     * Before call this function provide device admin premission to application and execute below command in terminal using ADB
     * factory reset your device first
     * install this application
     * give all permission allow
     * run below commands in terminal
     * adb shell
     * adb shell dpm set-device-owner com.izontechnology.dcapp/com.izontechnology.dcapp.receiver.ControlDeviceAdminReceiver
     * adb shell dpm remove-active-admin com.izontechnology.dcapp/com.izontechnology.dcapp.receiver.ControlDeviceAdminReceiver
     */
    fun lockDownApp(
        context: Context,
        active: Boolean,
        whitelistPackage: String = "",
        finishActivity: Boolean = false
    ) {
        val prefs = SharedPrefs(context)
        prefs.setIsKiosk(active)
        val devicePolicyManager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponentName = getAdminComponentName(context)

        // disable keyguard and status bar
        devicePolicyManager.setKeyguardDisabled(adminComponentName, active)
        devicePolicyManager.setStatusBarDisabled(adminComponentName, active)
        if (active) {
            // set this Activity as a lock task package
            devicePolicyManager.setLockTaskPackages(
                adminComponentName,
                if (active) arrayOf<String>(
                    context.packageName,
                    whitelistPackage,
                    izonPackageName
                ) else arrayOf<String>()
            )
            val intentFilter = IntentFilter(Intent.ACTION_MAIN)
            intentFilter.addCategory(Intent.CATEGORY_HOME)
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context, HostActivity::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            devicePolicyManager.addUserRestriction(
                adminComponentName,
                UserManager.DISALLOW_SAFE_BOOT
            )
            devicePolicyManager.addUserRestriction(
                adminComponentName,
                UserManager.DISALLOW_FACTORY_RESET
            )
            devicePolicyManager.addUserRestriction(
                adminComponentName,
                UserManager.DISALLOW_ADD_USER
            )
            devicePolicyManager.addUserRestriction(
                adminComponentName,
                UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA
            )
//            devicePolicyManager.addUserRestriction(
//                adminComponentName,
//                UserManager.DISALLOW_ADJUST_VOLUME
//            )
            context.setVolume(30)
            devicePolicyManager.setSystemUpdatePolicy(
                adminComponentName,
                SystemUpdatePolicy.createWindowedInstallPolicy(60, 120)
            )

            // set KIOSK activity as home intent receiver so that it is started
            // on reboot
            devicePolicyManager.addPersistentPreferredActivity(
                adminComponentName,
                intentFilter,
                ComponentName(context.packageName, HostActivity::class.java.name)
            )
        } else {
            devicePolicyManager.clearUserRestriction(
                adminComponentName,
                UserManager.DISALLOW_SAFE_BOOT
            )
            devicePolicyManager.clearUserRestriction(
                adminComponentName,
                UserManager.DISALLOW_FACTORY_RESET
            )
            devicePolicyManager.clearUserRestriction(
                adminComponentName,
                UserManager.DISALLOW_ADD_USER
            )
            devicePolicyManager.clearUserRestriction(
                adminComponentName,
                UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA
            )
            devicePolicyManager.clearUserRestriction(
                adminComponentName,
                UserManager.DISALLOW_ADJUST_VOLUME
            )
            devicePolicyManager.setSystemUpdatePolicy(adminComponentName, null)
            devicePolicyManager.clearPackagePersistentPreferredActivities(
                adminComponentName,
                context.packageName
            )
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (am.lockTaskModeState ==
                ActivityManager.LOCK_TASK_MODE_LOCKED
            ) {
                Log.e("exit kiosk","0")
                if (BaseApplication.activity != null) {
                    BaseApplication.activity?.stopLockTask()
                    Log.e("exit kiosk","1 ${BaseApplication.activity}")
                    if (finishActivity) {
                        Log.e("exit kiosk","2 ${BaseApplication.activity}")
                        BaseApplication.activity?.finish()
                        BaseApplication.activity?.finishAndRemoveTask()
                        if (BaseApplication.activity?.isDestroyed != true){
                            Log.e("exit kiosk","3 ${BaseApplication.activity}")
                            BaseApplication.activity?.finish()
                            BaseApplication.activity?.moveTaskToBack(true)
                            BaseApplication.activity?.finishAffinity()
                            Log.e("exit kiosk","3 ${BaseApplication.activity?.isDestroyed}")
                        }
                    }
                } else if (context is Activity) {
                    Log.e("exit kiosk","4")
                    context.stopLockTask()
                    if (finishActivity) {
                        Log.e("exit kiosk","5")
                        context.finish()
                        context.finishAndRemoveTask()
                        if (!context.isDestroyed){
                            Log.e("exit kiosk","6")
                            context.moveTaskToBack(true)
                            context.finishAffinity()
                        }
                    }
                } else {
                    Log.e("exit kiosk","7")
                    val pm: PackageManager = context.getPackageManager()
                    pm.clearPackagePreferredActivities(context.packageName)


                    val homeIntent = Intent(Intent.ACTION_MAIN)
                    homeIntent.addCategory(Intent.CATEGORY_HOME)
                    val infoList =
                        pm.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    // Scan the list to find the first match that isn't my own app
                    for (info in infoList) {
                        if (context.packageName != info.activityInfo.packageName) {
                            // This is the first match that isn't my package, so copy the
                            //  package and class names into to the HOME Intent
                            homeIntent.setClassName(
                                info.activityInfo.packageName,
                                info.activityInfo.name
                            )
                            break
                        }
                    }
                    // Launch the default HOME screen
                    Log.e("exit kiosk","8")
//                    context.startActivity(homeIntent)
                }
            }
//            if (context is Activity) {
//                context.recreate()
//            } else {
//                context.startActivity(Intent(context, HostActivity::class.java).apply {
//                    setFlags(
//                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//                    )
//                })
//            }
        }
//        if (context is Activity) {
//            context.recreate()
//        }else{
//            context.startActivity(Intent(context,HostActivity::class.java).apply { setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) })
//        }
    }
}