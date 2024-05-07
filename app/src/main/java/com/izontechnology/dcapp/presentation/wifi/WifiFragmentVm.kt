package com.izontechnology.dcapp.presentation.wifi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.BaseApplication
import com.izontechnology.dcapp.base.BaseViewModel
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.data.common.SimNetwork
import com.izontechnology.dcapp.data.request.SimStatusRequest
import com.izontechnology.dcapp.data.request.UserRequest
import com.izontechnology.dcapp.data.response.SimStatusResponse
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import com.izontechnology.dcapp.utils.HUNDRED
import com.izontechnology.dcapp.utils.TENTHOUSAND
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WifiFragmentVm @Inject constructor(private val repository: DeviceInfoRepository) :
    BaseViewModel() {
    @Inject
    lateinit var prefs: SharedPrefs
    var handlerSimScan: Handler? = null
    var simScanRunnable: Runnable? = null

    @Inject
    lateinit var telephonyManager: TelephonyManager

    @Inject
    lateinit var subscriptionManager: SubscriptionManager

    val simNetworkList = MutableLiveData<ArrayList<SimNetwork?>>()


    var simStatusResponse = MutableLiveData<Resource<SimStatusResponse>>()
    fun getToken(onSuccess: () -> Unit, onFail: (status: String?, message: String?) -> Unit) {
        val user = UserRequest("tqVsR7KHqfyqfiMpKLXJ", "dcappuser")
        viewModelScope.launch {
            repository.getToken(user).collect {
                when (it) {
                    is Resource.Success -> {
                        it.data?.token?.let { it1 -> prefs.saveToken("bearer $it1") }
                        onSuccess.invoke()
                    }

                    is Resource.Error -> {
                        onFail.invoke(it.status, it.message)
                    }

                    is Resource.APIException -> {
                        onFail.invoke(it.status.toString(), it.message)
                    }

                    else -> {}
                }
            }
        }
    }

    fun getSimStatus(simStatusRequest: SimStatusRequest) {
        viewModelScope.launch {
            repository.getSimStatus(simStatusRequest).collect {
                simStatusResponse.value = it
            }
        }
    }

    fun stopSimScan() {
        simScanRunnable?.let { handlerSimScan?.removeCallbacks(it) }
        handlerSimScan?.removeCallbacks {}
        handlerSimScan = null
    }

    fun startSimScan(forceScan: Boolean = false) {
        if (forceScan) {
            stopSimScan()
        }
        if (handlerSimScan == null) {
            handlerSimScan = Handler(Looper.getMainLooper())
            var delay = HUNDRED // 1000 milliseconds == 1 second
            handlerSimScan?.postDelayed(object : Runnable {
                override fun run() {
                    getScanSimResults {
                        handlerSimScan?.postDelayed(this, delay.toLong())
                    }
                    delay = TENTHOUSAND
                }
            }.apply {
                simScanRunnable = this
            }, delay.toLong()
            )
        }
    }

    fun getScanSimResults(scanComplete: (() -> Unit)? = null) {
        try {
            val networks = ArrayList<SimNetwork?>()
            if (ActivityCompat.checkSelfPermission(
                    BaseApplication.INSTANCE,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                subscriptionManager.activeSubscriptionInfoList.forEach { subscriptionInfo ->
                    val simNetwork = SimNetwork()
                    simNetwork.provider = subscriptionInfo.displayName.toString()
                    simNetwork.simSlot = subscriptionInfo.simSlotIndex
                    if ((subscriptionInfo.carrierName?.contains(
                            "no service",
                            true
                        )== true) or (subscriptionInfo.carrierName.isNullOrEmpty())
                    ) {
                        simNetwork.isRegistered = false
                        simNetwork.icon = R.drawable.ic_sim_not_available
                    } else {
                        Log.d("telephonyManager", (telephonyManager.allCellInfo.size.toString()))
                        var cellInfo = telephonyManager.allCellInfo.firstOrNull { cellInfo ->
                            when (cellInfo) {
                                is CellInfoWcdma -> cellInfo.cellIdentity.mnc.equals(
                                    subscriptionInfo.mnc
                                )

                                is CellInfoGsm -> cellInfo.cellIdentity.mnc.equals(subscriptionInfo.mnc)
                                is CellInfoLte -> cellInfo.cellIdentity.mnc.equals(subscriptionInfo.mnc)
                                else -> false
                            }
                        }
                        if (subscriptionManager.activeSubscriptionInfoList.size == 1) {
                            if (cellInfo == null) {
                                cellInfo =
                                    telephonyManager.allCellInfo.firstOrNull { it.isRegistered }
                            }
                        }
                        if (cellInfo != null) {
                            simNetwork.isRegistered = cellInfo.isRegistered
                            if (cellInfo.isRegistered) {
                                when (cellInfo) {
                                    is CellInfoWcdma -> {
                                        val cellSignalStrengthWcdma = cellInfo.cellSignalStrength
                                        simNetwork.strength = cellSignalStrengthWcdma.dbm
                                        simNetwork.networkType = "3G"
                                        simNetwork.icon = getSimIcon(cellSignalStrengthWcdma.level)
                                    }

                                    is CellInfoGsm -> {
                                        val cellSignalStrengthGsm = cellInfo.cellSignalStrength
                                        simNetwork.strength = cellSignalStrengthGsm.dbm
                                        simNetwork.networkType = "2G"
                                        simNetwork.icon = getSimIcon(cellSignalStrengthGsm.level)
                                    }

                                    is CellInfoLte -> {
                                        val cellSignalStrengthLte = cellInfo.getCellSignalStrength()
                                        simNetwork.strength = cellSignalStrengthLte.dbm
                                        simNetwork.networkType = "4G"
                                        simNetwork.icon = getSimIcon(cellSignalStrengthLte.level)
                                    }

                                    is CellInfoCdma -> {
                                        val cellSignalStrengthCdma = cellInfo.cellSignalStrength
                                        simNetwork.strength = cellSignalStrengthCdma.dbm
                                        simNetwork.networkType = "G"
                                        simNetwork.icon = getSimIcon(cellSignalStrengthCdma.level)
                                    }
                                }
                            }
                        }
                    }
                    networks.add(simNetwork)
                }
                simNetworkList.value = networks
            }
            scanComplete?.invoke()
        }catch (e:Exception){
            scanComplete?.invoke()
            e.printStackTrace()
        }
    }

    private fun getSimIcon(level: Int): Int {
        when (level) {
            0 -> {
                return R.drawable.ic_sim_signal_0
            }

            1 -> {
                return R.drawable.ic_sim_signal_1
            }

            2 -> {
                return R.drawable.ic_sim_signal_2
            }

            3 -> {
                return R.drawable.ic_sim_signal_3
            }

            4 -> {
                return R.drawable.ic_sim_signal_4
            }

            else -> return R.drawable.ic_sim_not_available
        }
    }
}