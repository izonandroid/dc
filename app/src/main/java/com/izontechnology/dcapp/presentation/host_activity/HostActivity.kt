package com.izontechnology.dcapp.presentation.host_activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.izontechnology.dcapp.BuildConfig
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.base.view.BaseActivity
import com.izontechnology.dcapp.data.request.ExitKioskRequest
import com.izontechnology.dcapp.databinding.ActivityHostBinding
import com.izontechnology.dcapp.services.AccessiblityService
import com.izontechnology.dcapp.services.ForegroundService
import com.izontechnology.dcapp.utils.API_RETURN_UPDATE_FORMAT
import com.izontechnology.dcapp.utils.LegacyUtils
import com.izontechnology.dcapp.utils.PermissionCallBack
import com.izontechnology.dcapp.utils.confirmationDialog
import com.izontechnology.dcapp.utils.getAndroidId
import com.izontechnology.dcapp.utils.getDeviceName
import com.izontechnology.dcapp.utils.getIccId
import com.izontechnology.dcapp.utils.hideSoftKeyboard
import com.izontechnology.dcapp.utils.izonPackageName
import com.izontechnology.dcapp.utils.log.HyperLog
import com.izontechnology.dcapp.utils.scheduleWork
import com.izontechnology.dcapp.utils.setBrightness
import com.izontechnology.dcapp.utils.setVolume
import com.izontechnology.dcapp.utils.simpleAlertOverLay
import com.izontechnology.dcapp.utils.simpleInputDialog
import com.izontechnology.dcapp.utils.toastMessage
import com.izontechnology.dcapp.utils.todayDate
import com.izontechnology.dcapp.utils.validateKioskCodeV2
import com.izontechnology.dcapp.utils.wifi.getIPAddressOfConnectedWifi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HostActivity : BaseActivity<ActivityHostBinding, HostActivityVM>() {
    private var ipAddress = ""
    private var deviceName = ""
    private var ramUsage = ""
    private var cpuUsage = ""
    private var deviceBrightness = 0
//    private lateinit var telephonyManager: TelephonyManager

    var mDialog: Dialog? = null

    var isBackAllow = false
    val TAG = HostActivity::class.java.canonicalName

    @Inject
    lateinit var prefs: SharedPrefs

    override fun observeViewModel() {
        prefs.setIsGolfAppLaunch(false)
        viewModel.apply {
        }
    }


    val permissionLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
            askForAllPermission()
//            }
        }

    fun enableAccessibility(activity: Activity) {
        var accessibilityServices: String? = Settings.Secure.getString(
            activity.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )

        if (accessibilityServices == null) {
            accessibilityServices = ""
        } else if (accessibilityServices.isNotEmpty()) {
            accessibilityServices = ":$accessibilityServices"
        }
        Settings.Secure.putString(
            activity.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            "${activity.applicationInfo.packageName}/${AccessiblityService::class.java.canonicalName}$accessibilityServices"
        )
        Settings.Secure.putString(
            activity.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED, "1"
        )
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName =
            "${applicationInfo.packageName}/${AccessiblityService::class.java.canonicalName}"
        var accessibilityEnabled = 0
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        val splitter = SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                splitter.setString(settingValue)
                while (splitter.hasNext()) {
                    val accessibilityService = splitter.next()
                    if (accessibilityService.equals(serviceName, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun initViewBinding() {
        // Schedule nightly update using WorkManager
        scheduleWork(
            if (prefs.getNightUpdateHour() == -1) 1 else prefs.getNightUpdateHour(),
            if (prefs.getNightUpdateMinute() == -1) 1 else prefs.getNightUpdateMinute()
        )
//        scheduleWork(-1,1)
//        startService(Intent(this,ForegroundService::class.java))
        val backpressCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                /*if (findNavController(binding.navHostFragment.id).currentDestination?.id == R.id.action_wifi) {
                    LegacyUtils.lockDownApp(
                        this@HostActivity,
                        false,
                        finishActivity = true
                    )
                    remove()
                    onBackPressedDispatcher.onBackPressed()
                    onBackPressedDispatcher.addCallback(this@HostActivity, this)
                } else */
                if (isBackAllow) {
                    remove()
                    onBackPressedDispatcher.onBackPressed()
                    isBackAllow = false
                    onBackPressedDispatcher.addCallback(this@HostActivity, this)
                } else {
                    exitKioskMode(noKiosk = {
                        remove()
                        onBackPressedDispatcher.onBackPressed()
                        onBackPressedDispatcher.addCallback(this@HostActivity, this)
                    })
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, backpressCallback)

        askForAllPermission()
        viewModel.isWifiEnabled()
        try {
//        if (prefs.getIsKiosk()){
//            if (LegacyUtils.isDeviceOwner(this)) {
//                LegacyUtils.lockDownApp(this, false, finishActivity = false)
//            }
//        }
            if (LegacyUtils.isDeviceOwner(this)) {
                prefs.setIsKiosk(true)
                LegacyUtils.lockDownApp(
                    this,
                    true,
                    whitelistPackage = com.izontechnology.dcapp.utils.izonPackageName
                )
            } else {
                toastMessage("Please make this application device owner")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ipAddress = this@HostActivity.getIPAddressOfConnectedWifi().toString()

//        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager


        viewModel.getDeviceStatus()
        deviceName = getDeviceName()
        ramUsage = viewModel.getRAMUsage()
        cpuUsage = viewModel.getCPUUsage()
        deviceBrightness = viewModel.getCurrentBrightness(this)
    }

    private fun askForAllPermission() {
        askForAccessibilityPermission {
            askForBatteryRestrictionRemove {
                askForSettingPermission {
                    askForFileManage {
                        askForOverLayPermission {
                            deviceAdminControl {
//                                askForPhoneStateModifyPermission {
                                askForPhoneStatePermission {
                                    askForLocationEnable {
                                        viewModel.isPermissionGranted.value = true
                                        startService(
                                            Intent(
                                                this,
                                                ForegroundService::class.java
                                            ).apply { setAction("start") })
                                        setVolume(30)
                                        setBrightness(100)
                                    }
                                }
                            }
//                            }
                        }
                    }
                }
            }
        }
    }

    fun askForPhoneStatePermission(granted: (() -> Unit)? = null) {
        requestPermission(arrayListOf(Manifest.permission.READ_PHONE_STATE),
            object : PermissionCallBack {
                override fun permissionGranted() {
                    granted?.invoke()
                }

                override fun permissionDenied() {
                    confirmationDialog(
                        title = getString(R.string.permission_label),
                        msg = getString(R.string.allow_phone_permission)
                    )
                }

                override fun onPermissionDisabled() {
                    confirmationDialog(title = getString(R.string.permission_label),
                        msg = getString(R.string.allow_phone_permission),
                        btnPositiveClick = {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            permissionLauncher.launch(intent)
                        })
                }

            })
    }

    fun askForPhoneStateModifyPermission(granted: (() -> Unit)? = null) {
        requestPermission(arrayListOf(Manifest.permission.MODIFY_PHONE_STATE),
            object : PermissionCallBack {
                override fun permissionGranted() {
                    granted?.invoke()
                }

                override fun permissionDenied() {
                    confirmationDialog(
                        title = getString(R.string.permission_label),
                        msg = getString(R.string.allow_phone_permission)
                    )
                }

                override fun onPermissionDisabled() {
                    confirmationDialog(title = getString(R.string.permission_label),
                        msg = getString(R.string.allow_phone_permission),
                        btnPositiveClick = {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            permissionLauncher.launch(intent)
                        })
                }

            })
    }

    private fun askForSettingPermission(granted: (() -> Unit)? = null) {
        if (Settings.System.canWrite(this)) {
            granted?.invoke()
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            permissionLauncher.launch(intent)
        }
    }

    private fun askForLocationEnable(granted: (() -> Unit)? = null) {
        val locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            granted?.invoke()
        } else {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            intent.data = Uri.parse("package:$packageName")
            permissionLauncher.launch(intent)
        }
    }

    fun askForBatteryRestrictionRemove(granted: (() -> Unit)? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val isRestricted = pm.isIgnoringBatteryOptimizations(packageName)
            if (!isRestricted) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                permissionLauncher.launch(intent)
            } else {
                granted?.invoke()
            }
        } else {
            granted?.invoke()
        }
    }

    fun askForAccessibilityPermission(granted: (() -> Unit)? = null) {
        if (!isAccessibilityServiceEnabled()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//            intent.data = Uri.parse("package:$packageName")
            permissionLauncher.launch(intent)
        } else {
            granted?.invoke()
        }
    }

    fun askForFileManage(granted: (() -> Unit)? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                granted?.invoke()
            } else {
                //request for the permission
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                permissionLauncher.launch(intent)
            }
        } else {
            val permissions = arrayListOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            requestPermission(permissions, object : PermissionCallBack {
                override fun permissionGranted() {
                    granted?.invoke()
                }

                override fun permissionDenied() {
                    confirmationDialog(title = getString(R.string.permission_label),
                        msg = getString(R.string.allow_storage_permission),
                        btnPositiveClick = {

                        })
                }

                override fun onPermissionDisabled() {
                    confirmationDialog(title = getString(R.string.permission_label),
                        msg = getString(R.string.allow_storage_permission),
                        btnPositiveClick = {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            permissionLauncher.launch(intent)
                        })
                }
            })
        }

    }

    private fun deviceAdminControl(granted: (() -> Unit)? = null) {
        val devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = LegacyUtils.getAdminComponentName(this)
        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Enable device admin to remotely shut down or restart the device."
            )
            permissionLauncher.launch(intent)
        } else {
            granted?.invoke()
        }
    }

    private fun askForOverLayPermission(granted: (() -> Unit)? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            intent.data = Uri.parse("package:$packageName")
            permissionLauncher.launch(intent)
        } else {
            granted?.invoke()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.forEach { fragment ->
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, ForegroundService::class.java).apply { setAction("stop") })
    }


    fun exitKioskMode(
        onCancel: ((dialog: Dialog, input: String, view: View) -> Unit)? = null,
        noKiosk: ((dialog: Dialog?) -> Unit)? = null
    ) {
        if ((mDialog?.isShowing != true) and prefs.getIsKiosk()) {
            mDialog = simpleInputDialog(
                getString(R.string.enter_a_pin_to_exit_kiosk_mode), onOK = { dialog, pin, view ->
                    view?.hideSoftKeyboard()
                    if (pin.isEmpty()) {
                        toastMessage(getString(R.string.please_enter_pin))
                    } else if (!viewModel.network.isConnected()) {
                        if (pin.equals(prefs.getExitKioskCode())) {
                            HyperLog.d(TAG, "Exit Kiosk Mode")
                            dialog.dismiss()
//                                finish()
                            findNavController(binding.navHostFragment.id).navigate(R.id.action_exit_kiosk)
                        }
                    } else {
                        val izonAppVersion = try {
                            packageManager?.getPackageInfo(
                                izonPackageName,
                                0
                            )?.versionName
                                ?: "0.0.0"
                        } catch (e: PackageManager.NameNotFoundException) {
                            "0.0.0"
                        }
                        val exitKioskRequest = ExitKioskRequest(
                            deviceid = getAndroidId(),
                            deviceiccid = getIccId().first,
                            clubcode = prefs.getFacilityCode(),
                            dcappversion = BuildConfig.VERSION_NAME,
                            izongolfappversion = izonAppVersion,
                            exitcode = pin,
                            clientdatetime = todayDate(withFormat = API_RETURN_UPDATE_FORMAT)
                        )
                        validateKioskCodeV2(
                            viewModel.viewModelScope,
                            viewModel.repository,
                            exitKioskRequest,
                            onSuccess = {
//                                LegacyUtils.lockDownApp(
//                                    this,
//                                    false,
//                                    finishActivity = false
//                                )
                                HyperLog.d(TAG, "Exit Kiosk Mode")
                                dialog.dismiss()
//                                finish()
                                findNavController(binding.navHostFragment.id).navigate(R.id.action_exit_kiosk)
                            },
                            onFail = {
                                toastMessage(getString(R.string.please_enter_correct_pin))
                                simpleAlertOverLay(it.toString())
                            })
                    }
                }, onCancel = { dialog, pin, view ->
                    onCancel?.invoke(dialog, pin, view)
                })
        } else {
            noKiosk?.invoke(mDialog)
        }
    }

}