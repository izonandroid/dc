package com.izontechnology.dcapp.presentation.wifi

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.izontechnology.dcapp.BuildConfig
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.BaseApplication
import com.izontechnology.dcapp.base.common.CheckConnection
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.base.view.BaseFragment
import com.izontechnology.dcapp.data.common.ScannedWifiItem
import com.izontechnology.dcapp.data.common.SimNetwork
import com.izontechnology.dcapp.data.common.WifiHeader
import com.izontechnology.dcapp.data.request.SimStatusRequest
import com.izontechnology.dcapp.data.response.SimStatusResponse
import com.izontechnology.dcapp.databinding.DialogWifiPasswordInputBinding
import com.izontechnology.dcapp.databinding.FragmentWifiBinding
import com.izontechnology.dcapp.databinding.ItemWifiBinding
import com.izontechnology.dcapp.databinding.ItemWifiHeaderBinding
import com.izontechnology.dcapp.presentation.host_activity.HostActivityVM
import com.izontechnology.dcapp.utils.DeviceLogs
import com.izontechnology.dcapp.utils.GPS_RESOLUTION_CODE
import com.izontechnology.dcapp.utils.LegacyUtils
import com.izontechnology.dcapp.utils.MultiViewRecyclerAdapter
import com.izontechnology.dcapp.utils.PermissionCallBack
import com.izontechnology.dcapp.utils.RecyclerViewLayoutManager
import com.izontechnology.dcapp.utils.RecyclerViewLinearLayout
import com.izontechnology.dcapp.utils.WEP
import com.izontechnology.dcapp.utils.WPA
import com.izontechnology.dcapp.utils.WPA2
import com.izontechnology.dcapp.utils.WPA_EAP
import com.izontechnology.dcapp.utils.WidgetsViewModel
import com.izontechnology.dcapp.utils.confirmationDialog
import com.izontechnology.dcapp.utils.getAndroidId
import com.izontechnology.dcapp.utils.getConnectedNetworkType
import com.izontechnology.dcapp.utils.getDeviceModel
import com.izontechnology.dcapp.utils.getDeviceName
import com.izontechnology.dcapp.utils.getDeviceOS
import com.izontechnology.dcapp.utils.getDeviceSerial
import com.izontechnology.dcapp.utils.getIccId
import com.izontechnology.dcapp.utils.getImei
import com.izontechnology.dcapp.utils.getSimStatus
import com.izontechnology.dcapp.utils.installApplication
import com.izontechnology.dcapp.utils.isPackageInstalled
import com.izontechnology.dcapp.utils.izonPackageName
import com.izontechnology.dcapp.utils.log.HyperLog
import com.izontechnology.dcapp.utils.savedWifiList
import com.izontechnology.dcapp.utils.setUpMultiViewRecyclerAdapter
import com.izontechnology.dcapp.utils.setUpRecyclerView_Binding
import com.izontechnology.dcapp.utils.wifi.getDrawableFromRSSI
import com.izontechnology.dcapp.utils.wifi.isConnectedToWifiOf
import com.izontechnology.dcapp.utils.wifi.isProtectedNetwork
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.http.HTTP_FORBIDDEN
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class WifiFragment : BaseFragment<FragmentWifiBinding, WifiFragmentVm>() {
    @Inject
    lateinit var prefs: SharedPrefs
    var isNextScreenCalled = false

    private val mainVM: HostActivityVM by activityViewModels()
    var wifiAdapter: MultiViewRecyclerAdapter<WidgetsViewModel>? = null
    private val checkConnection by lazy { CheckConnection(BaseApplication.INSTANCE) }


    override fun observeViewModel() {
        setupWifiList(mainVM.wifiNetworksList)
        mainVM.isPermissionGranted.observe(this) mainobser@{
            if (it) {
                checkConnection.observe(this) {
                    if (prefs.getFacilityCode().isNotEmpty()) {
                        /*if (prefs.getFacilityCode().isNotEmpty()) {
                        callNextScreen()
                    } else*/ if (prefs.getPreviousWifiSSID().isNotEmpty()) {
                            if (requireContext().isConnectedToWifiOf(prefs.getPreviousWifiSSID())) {
                                callNextScreen()
                            }
                        } else if (viewModel.network.isConnected()) {
                            callNextScreen()
                        }
                    }
                }
            }
        }
        enableDisableContBtn(false)
        loadingDialog(true)
        if (!prefs.getIsFirstLaunch()) {
            prefs.setIsWifiOn(true)
        }
        prefs.setIsFirstLaunch(true)
        if (prefs.getIsWifiOn()) {
            activity?.let { mainVM.wifiManager.openWiFi(it) }
        }
        mainVM.wifiNetworkList.observe(this) {
            if (mainVM.isWifiConnecting.value == false) {
//                enableDisableContBtn(false)
                DeviceLogs.e("wifi scan", "wifi scan setWiFiList update")
                setWiFiList(it)
            } else {
//                enableDisableContBtn(true)
            }
        }
        viewModel.simNetworkList.observe(this) {
            setSimNetwork(it)
        }
        mainVM.isWifiConnected.observe(this) {
            if (it and (!isNextScreenCalled)) {
                callNextScreen()
                isNextScreenCalled = true
            }
        }
        mainVM.isWifiScanning.observe(this) {
            if (mainVM.isWifiEnabled.value == true) {
                binding.apply {
                    if (it) {
//                        llScanning.visibility = View.VISIBLE
                        if (((wifiAdapter?.itemCount ?: 0) > 0)) {
                            rvWifiList.visibility = View.VISIBLE
                        } else {
                            rvWifiList.visibility = View.GONE
                        }
                    } else {
                        imgRefreshList.clearAnimation()
//                        llScanning.visibility = View.GONE
//                        rvWifiList.visibility = View.GONE
                    }
                }
            }
        }
        mainVM.wifiManager.setOnWifiEnabledListener { enabled ->
            if (!enabled) {
                mainVM.isWifiEnabled.value = false
                mainVM.isWifiConnected.value = false
            } else {
                mainVM.isWifiEnabled.value = true
//                mainVM.startWifiScan()
                requestForWifiList()
            }
        }
        mainVM.isWifiEnabled.observe(this) {
            if (it) {
//                requestForWifiList()
                binding.rvWifiList.visibility = View.VISIBLE
                binding.llWifiOff.visibility = View.GONE
                binding.llContinueRefresh.visibility = View.VISIBLE
            } else {
                mainVM.isWifiScanning.value = false
                mainVM.wifiNetworkList.value?.clear()
                notifyAndScrollToStart()
                binding.rvWifiList.visibility = View.GONE
                binding.llWifiOff.visibility = View.VISIBLE
                loadingDialog(false)
                binding.llContinueRefresh.visibility = View.GONE
                mainVM.handlerWifiScan?.removeCallbacksAndMessages(null)
            }
        }

        viewModel.simStatusResponse.observe(this) {
            handleSimStatus(it)
        }
    }

    private fun setSimNetwork(simNetworks: ArrayList<SimNetwork?>) {
        val adapter =
            binding.rvSimList.setUpRecyclerView_Binding<SimNetwork, ItemWifiBinding>(
                R.layout.item_wifi,
                simNetworks,
                RecyclerViewLayoutManager.LINEAR,
                RecyclerViewLinearLayout.VERTICAL
            ) {
                contentBinder { simNetwork, binder, i ->
                    binder.imgWifiSignal.visibility = View.GONE
                    binder.txtWifiName.text = simNetwork.provider
//                    binder.imgWifiSignal.setImageResource(simNetwork.icon)
                    var connection = if (requireContext().getConnectedNetworkType().equals("sim")) {
                        getString(R.string.connected)
                    } else {
                        getString(R.string.not_connected)
                    }
                    if (simNetwork.isRegistered == true) {
                        binder.txtWifiStatus.text = "${connection} | ${simNetwork.networkType}"
                    } else {
                        binder.txtWifiStatus.text = "${connection}"
                    }
                    binder.txtWifiStatus.visibility = View.VISIBLE
                    if ((!requireContext().getConnectedNetworkType().equals("sim"))) {
                        binder.btnCellular.visibility = View.VISIBLE
                    } else {
                        binder.btnCellular.visibility = View.VISIBLE
                    }
                    binder.btnCellular.setOnClickListener {
                        loadingDialog(true)
//                        mainVM.wifiManager.disconnectCurrentWifi()
                        try {
                            if(mainVM.wifiManager.getSSIDOfConnectedWifi()?.isNotEmpty() == true) {
                                mainVM.wifiManager.getSSIDOfConnectedWifi()
                                    ?.let { it1 -> mainVM.wifiManager.deleteConfig(it1) }
                            }else{
                                mainVM.wifiManager.disconnectCurrentWifi()
                            }
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.READ_PHONE_STATE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (!viewModel.telephonyManager.isDataEnabled) {
                                    viewModel.telephonyManager.setDataEnabled(true)
                                }
                                var subActual: Int = -1
                                for (m in viewModel.subscriptionManager.javaClass.declaredMethods) {
                                    if (m.name == "getDefaultDataSubscriptionId") {
                                        try {
                                            subActual =
                                                m.invoke(viewModel.subscriptionManager) as Int
                                        } catch (e: java.lang.Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                try {
                                    var method =
                                        viewModel.telephonyManager.javaClass.getDeclaredMethod("enableDataConnectivity")
                                    method.isAccessible = true
                                    method.invoke(viewModel.telephonyManager)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                for (m in viewModel.subscriptionManager.javaClass.declaredMethods) {
                                    if (m.name == "setDefaultDataSubId") {
                                        try {
                                            if (subActual != simNetwork.simSlot) {
                                                m.invoke(
                                                    viewModel.subscriptionManager,
                                                    simNetwork.simSlot
                                                )
                                            }
//                                            m.invoke(viewModel.subscriptionManager, 1)
                                        } catch (e: java.lang.Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                if ((!requireContext().getConnectedNetworkType().equals("sim"))) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                                        val panelIntent =
//                                            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
//                                        startActivity(panelIntent)
//                                        startActivity(Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS))
                                    }
                                }
                            }
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            activity?.runOnUiThread {
                                loadingDialog(false)
                                callNextScreen()
                            }
                        }, 10000)
                    }
                }
            }

        binding.rvSimList.adapter = adapter
        loadingDialog(false)
        enableDisableContBtn(viewModel.network.isConnected())
    }

    override fun initViewBinding() {
        var pm = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        viewModel.startSimScan()
        binding.txtAppVersion.text = "${getString(R.string.version)}: ${BuildConfig.VERSION_NAME}"
        doubleTap()
        binding.llRefreshList.setOnClickListener {
            loadingDialog(true)
            val rotate = RotateAnimation(
                0f, 359f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            rotate.setDuration(300);
            rotate.repeatCount = Animation.INFINITE
            binding.imgRefreshList.startAnimation(rotate)
            DeviceLogs.e("wifi scan", "wifi scan 4")
            mainVM.startWifiScan(true)
        }
        binding.btnWifiOn.setOnClickListener {
            try {
                // read the airplane mode setting
                val isEnabled = Settings.System.getInt(
                    requireActivity().getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0
                ) == 1

                // toggle airplane mode
                Settings.System.putInt(
                    requireActivity().getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, if (isEnabled) 0 else 1
                )
                Settings.Global.putInt(
                    requireContext().contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON, if (isEnabled) 1 else 0
                )

//                // Post an intent to reload
//                val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED)
//                intent.putExtra("state", !isEnabled)
//                requireActivity().sendBroadcast(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            activity?.let { mainVM.wifiManager.openWiFi(it) }
        }
        binding.confirmButton.setOnClickListener {
            callNextScreen()
        }
        if (mainVM.isWifiEnabled.value == true) {
            requestForWifiList()
        }
    }

    private fun callNextScreen() {
        //            val intent = Intent(WifiManager.ACTION_PICK_WIFI_NETWORK)
//            startActivityForResult(intent, 100)
        if (viewModel.network.isConnected()) {
            HyperLog.d(
                WifiFragment::class.java.name,
                "Wifi connected ssid:${viewModel.wifiManager.getSSIDOfConnectedWifi()}"
            )
            callTokenAndSimAPI()
        } /*else if (!mainVM.wifiManager.getSSIDOfConnectedWifi().isNullOrEmpty()) {
            callTokenAndSimAPI()
        } */ else {
            toastMessage("Please connect to wifi network")
        }
    }

    private fun doubleTap() {
        val gDetector = GestureDetector(requireContext(), object : SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                val dpm =
                    context?.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

                dpm.clearDeviceOwnerApp(context?.packageName)
                return true
            }
        })

        // Set it to the view
        binding.txtAppVersion.setOnTouchListener { v, event -> gDetector.onTouchEvent(event) }
    }

    /**
     * Handles the response of adding a new user address.
     *
     * @param state The resource state containing the response data.
     */
    private fun handleSimStatus(state: Resource<SimStatusResponse>) {
        when (state) {
            is Resource.Success -> {
                loadingDialog(false)
                state.data?.simheader?.let { prefs.setSimHeader(it) }
                //                installOrLaunch()
                findNavController().navigate(R.id.action_version)
            }

            is Resource.Loading -> {
                loadingDialog(true)
            }

            is Resource.Error -> {
                isNextScreenCalled = false
                loadingDialog(false)
                if (state.status?.equals(HTTP_FORBIDDEN.toString()) == true) {
                    loadingDialog(true)
                    viewModel.getToken(onSuccess = {
                        loadingDialog(false)
                        callSimStatusAPI()
                    }, onFail = { state, message ->
                        loadingDialog(false)
                    })
                }
            }

            is Resource.APIException -> {
                isNextScreenCalled = false
                loadingDialog(false)
            }

            is Resource.Idle -> {
                loadingDialog(false)
            }
        }
    }

    private fun callTokenAndSimAPI() {
        loadingDialog(true)
        viewModel.getToken(onSuccess = {
            loadingDialog(false)
            callSimStatusAPI()
        }, onFail = { state, message ->
            loadingDialog(false)
            isNextScreenCalled = false
//            requireContext().simpleAlert(message.toString())
        })

    }

    private fun callSimStatusAPI() {
        loadingDialog(true)
        val tm = requireContext().getSystemService(TelephonyManager::class.java)
        askForPhoneStatePermission {
            val simStatusRequest = SimStatusRequest(
                appversion = BuildConfig.VERSION_NAME,
                uniqueId = requireContext().getIccId().first,
                operatorCode = tm.simOperator,
                operatorName = tm.simOperatorName,
                deviceserialno = getDeviceSerial(),
                simStatus = getSimStatus(tm.simState),
                uniqueiderror = requireContext().getIccId().second,
                deviceUniqueId = requireContext().getAndroidId(),
                devicemanufacturer = getDeviceName(),
                deviceos = getDeviceOS(),
                devicemodel = getDeviceModel(),
                deviceimei = requireContext().getImei(),
            )
            viewModel.getSimStatus(simStatusRequest)
        }
    }


    fun askForPhoneStatePermission(granted: (() -> Unit)? = null) {
        requestPermission(arrayListOf(Manifest.permission.READ_PHONE_STATE),
            object : PermissionCallBack {
                override fun permissionGranted() {
                    granted?.invoke()
                }

                override fun permissionDenied() {
                    requireContext().confirmationDialog(
                        title = getString(R.string.permission_label),
                        msg = getString(R.string.allow_phone_permission)
                    )
                }

                override fun onPermissionDisabled() {
                    requireContext().confirmationDialog(title = getString(R.string.permission_label),
                        msg = getString(R.string.allow_phone_permission),
                        btnPositiveClick = {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", izonPackageName, null)
                            intent.data = uri
                        })
                }

            })
    }

    private fun installOrLaunch() {
        if (requireContext().isPackageInstalled(izonPackageName)) {
            if (LegacyUtils.isDeviceOwner(requireContext())) {
                prefs.setIsKiosk(true)
                LegacyUtils.lockDownApp(requireContext(), true, whitelistPackage = izonPackageName)
            } else {
                toastMessage("Please make this application device owner")
            }
            val launchIntent =
                requireContext().packageManager.getLaunchIntentForPackage(izonPackageName)
            launchIntent?.let { startActivity(it) }
        } else {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                "OVERLAY/IzonGolf-012.apk"
            )
            if (file.exists()) {
                installApplication(requireContext(), file, izonPackageName, "")
            } else {
                toastMessage("IzonGolf application not found")
            }
        }
    }

    fun enableDisableContBtn(enable: Boolean = false) {
        binding.confirmButton?.isEnabled = enable
        if (enable) {
            binding.confirmButton?.setBackgroundResource(R.drawable.rec_space_gray_cr_8)
        } else {
            binding.confirmButton?.setBackgroundResource(R.drawable.rec_gray_cr_8)
        }
    }

    fun notifyAndScrollToStart() {
        try {
            wifiAdapter?.notifyDataSetChanged()
            binding?.rvWifiList?.smoothScrollToPosition(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestForWifiList() {
        val permissions = arrayListOf(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        requestPermission(permissions, object : PermissionCallBack {
            override fun permissionGranted() {
                if (!(mainVM.isWifiScanning.value == true)) {
//                    mainVM.startWifiScan()
                    gpsCheck()
                } else {
                    toastMessage("Scan is already running")
                }
            }

            override fun permissionDenied() {
                requireContext().confirmationDialog(title = getString(R.string.permission_label),
                    msg = getString(R.string.allow_location_permission),
                    btnPositiveClick = {
                        requestForWifiList()
                    })
            }

            override fun onPermissionDisabled() {
                requireContext().confirmationDialog(title = getString(R.string.permission_label),
                    msg = getString(R.string.allow_location_permission),
                    btnPositiveClick = {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", requireContext().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    })
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_RESOLUTION_CODE) {
                DeviceLogs.e("wifi scan", "wifi scan 5")
                mainVM.startWifiScan()
            }
        }
    }

    fun gpsCheck() {
        val locationManager =
            requireContext().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            DeviceLogs.e("wifi scan", "wifi scan 6")
            mainVM.startWifiScan()
            return
        }
        val locationRequest =
            LocationRequest.Builder(LocationRequest.PRIORITY_HIGH_ACCURACY, 10000).build()
        val locationSettingsRequestBuilder = LocationSettingsRequest.Builder()
        locationSettingsRequestBuilder.addLocationRequest(locationRequest);
        locationSettingsRequestBuilder.setAlwaysShow(true);
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());
        task.addOnSuccessListener {
            DeviceLogs.e("wifi scan", "wifi scan 7")
            mainVM.startWifiScan()
        }.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(requireActivity(), GPS_RESOLUTION_CODE)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }

    private fun setupWifiList(wifiNetworksList: ArrayList<WidgetsViewModel>) {
        wifiAdapter =
            binding.rvWifiList.setUpMultiViewRecyclerAdapter(wifiNetworksList) { item: WidgetsViewModel, binder: ViewDataBinding, position: Int ->
                if (item is ScannedWifiItem) {
                    if (binder is ItemWifiBinding) {
                        if (item.isConnected) {
                            binder.txtWifiStatus.visibility = View.VISIBLE
                            binder.btnConnect.visibility = View.GONE
                            binder.txtWifiStatus.text = getString(R.string.connected)
                        }
                        if (item.isConnecting) {
                            binder.txtWifiStatus.visibility = View.VISIBLE
                            binder.btnConnect.visibility = View.GONE
                            binder.txtWifiStatus.text = getString(R.string.connecting)
                        }
                        if (!item.isConnected and !item.isConnecting) {
                            binder.txtWifiStatus.visibility = View.GONE
                            binder.btnConnect.visibility = View.VISIBLE
                        }

                        binder.txtWifiName.text = item.wifiResult?.SSID
                        binder.imgWifiSignal.setImageResource(item.wifiResult.getDrawableFromRSSI())
                        binder.btnConnect.setOnClickListener {
//                            requireContext().confirmationDialog(
//                                title = getString(R.string.wifi_connect),
//                                msg = getString(
//                                    R.string.do_you_want_to_connect_wifi,
//                                    item.wifiResult?.SSID
//                                ),
//                                btnPositiveClick = {
//                                    val settingsIntent = Intent(Settings.Panel.ACTION_WIFI)
//                                    startActivity(settingsIntent)
//                                })
                            if ((!item.isConnected) and (!isProtectedNetwork(item.wifiResult?.capabilities))) {
                                connectToWifi(item, "")
                            } else if ((!item.isConnected) and (isProtectedNetwork(item.wifiResult?.capabilities))) {
                                showBottomSheetDialog(item)
                            }
                        }
                    }
                }
                if (item is WifiHeader) {
                    if (binder is ItemWifiHeaderBinding) {
                        binder.txtTitle.text = item.title
                    }
                }

            }
        binding.rvWifiList.adapter = wifiAdapter
        if(mainVM.wifiNetworkList.value?.isNotEmpty() == true){
            mainVM.wifiNetworkList.value?.let { setWiFiList(it) }
        }
    }

    private fun setWiFiList(wifiScanResult: ArrayList<ScanResult?>) {
        if (wifiScanResult.isNotEmpty()) {
            Log.i("startScan", "Found ${wifiScanResult.size} wifi-networks")
            mainVM.apply {
                wifiNetworksList.clear()
//                val currentHeaderItem = WifiHeader(getString(R.string.current_network))
                val availableHeaderItem = WifiHeader(getString(R.string.available_networks))
                wifiScanResult.forEach { result ->
                    if (result?.SSID?.isNotEmpty() == true) {
                        if (!wifiNetworksList.contains(availableHeaderItem)) {
                            wifiNetworksList.add(0, availableHeaderItem)
                        }
                        if (requireContext().isConnectedToWifiOf(result?.SSID)) {
                            prefs.setPreviousWifiSSID(result?.SSID ?: "")
//                            if (!wifiNetworksList.contains(currentHeaderItem)) {
//                                wifiNetworksList.add(0, currentHeaderItem)
                            wifiNetworksList.add(
                                1,
                                ScannedWifiItem(
                                    result,
                                    isConnected = true,
                                    isConnecting = result.SSID.equals(mainVM.connectingWifiSSID)
                                )
                            )
//                            }
                        } else {
                            if (!(viewModel.network.getNetworkInfo()
                                    ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true)
                            ) {
                                if (mainVM.isWifiConnecting.value != true) {
                                    val availableWifi =
                                        savedWifiList.firstOrNull { it.SSID.equals(result.SSID) }
                                    if (availableWifi != null) {
                                        connectToWifi(
                                            ScannedWifiItem(
                                                result,
                                                isConnecting = result.SSID.equals(mainVM.connectingWifiSSID)
                                            ), availableWifi.password
                                        )
                                    }
                                }
                            }
                            wifiNetworksList.add(
                                ScannedWifiItem(
                                    result,
                                    isConnecting = result.SSID.equals(mainVM.connectingWifiSSID)
                                )
                            )
                        }
                    }
                }
//                enableDisableContBtn(!mainVM.wifiManager.getSSIDOfConnectedWifi().isNullOrEmpty())
                notifyAndScrollToStart()
            }
        } else {
            Log.i("startScan", "No wifi-networks found")
        }
        loadingDialog(false)
    }

    fun showBottomSheetDialog(item: ScannedWifiItem) {
//        mainVM.stopWifiScan()
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val dialogBinding = DialogWifiPasswordInputBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.wifiName.text = item.wifiResult?.SSID
        dialogBinding.connectButton.setOnClickListener {
            if (dialogBinding.password.text?.trim().isNullOrEmpty()) {
                toastMessage(getString(R.string.please_enter_wifi_password))
            } else {
                connectToWifi(item, dialogBinding.password.text?.trim().toString())
            }
            dialog.dismiss()
        }
//        dialog.setOnDismissListener {
//            mainVM.isWifiConnecting.value = false
//        }

//        dialogBinding.hidePwd.setOnClickListener {
//            dialogBinding.hidePwd.visibility = View.GONE
//            dialogBinding.showPwd.visibility = View.VISIBLE
//            dialogBinding.password.setInputType(InputType.TYPE_CLASS_TEXT)
//
//        }
//        dialogBinding.showPwd.setOnClickListener {
//            dialogBinding.hidePwd.visibility = View.VISIBLE
//            dialogBinding.showPwd.visibility = View.GONE
//            dialogBinding.password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
//        }
        // Set the dialog behavior to a bottom sheet
        val bottomSheetBehavior = dialog.behavior
        bottomSheetBehavior.isFitToContents = true
        bottomSheetBehavior.isHideable = true

        dialog.show()
    }

    private fun connectToWifi(item: ScannedWifiItem, password: String) {
        mainVM.isWifiConnecting.value = true
        loadingDialog(true, getString(R.string.connecting_label))
        mainVM.connectingWifiSSID = item.wifiResult?.SSID.toString()
        item.isConnecting = true
        wifiAdapter?.notifyDataSetChanged()
        if ((item.wifiResult?.capabilities?.contains(WPA) == true) or (item.wifiResult?.capabilities?.contains(
                WPA2
            ) == true) or (item.wifiResult?.capabilities?.contains(WPA_EAP) == true)
        ) {
            viewModel.wifiManager.connectWPA2Network(
                item.wifiResult?.SSID ?: "",
                password,
                false
            )
        } else if ((item.wifiResult?.capabilities?.contains(WEP) == true)) {
            viewModel.wifiManager.connectWEPNetwork(
                item.wifiResult?.SSID ?: "",
                password,
                false
            )
        } else {
            viewModel.wifiManager.connectOpenNetwork(item.wifiResult?.SSID ?: "")
        }
    }

    override fun onResume() {
        super.onResume()
//        enableDisableContBtn(!mainVM.wifiManager.getSSIDOfConnectedWifi().isNullOrEmpty())
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopSimScan()
    }
}