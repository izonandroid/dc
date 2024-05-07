package com.izontechnology.dcapp.utils

import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import com.google.gson.JsonObject
import com.izontechnology.dcapp.BuildConfig
import com.izontechnology.dcapp.base.BaseApplication
import com.izontechnology.dcapp.base.common.Network
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.data.request.ExitKioskRequest
import com.izontechnology.dcapp.data.request.SimStatusRequest
import com.izontechnology.dcapp.data.request.UserRequest
import com.izontechnology.dcapp.data.request.VersionRequest
import com.izontechnology.dcapp.data.response.AvailableNetworkResponse
import com.izontechnology.dcapp.data.response.DeviceSettingResponse
import com.izontechnology.dcapp.data.response.DownloadInfoJobResponse
import com.izontechnology.dcapp.data.response.MediaJobResponse
import com.izontechnology.dcapp.data.response.OTAResponse
import com.izontechnology.dcapp.data.response.VersionJobResponse
import com.izontechnology.dcapp.data.response.VolumeResponse
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody

fun getToken(
    viewModelScope: CoroutineScope,
    repository: DeviceInfoRepository,
    onSuccess: () -> Unit,
    onFail: () -> Unit
) {
    val network = Network(BaseApplication.INSTANCE)
    if (network.isConnected()) {
        val prefs = SharedPrefs(BaseApplication.INSTANCE)
        val user = UserRequest("tqVsR7KHqfyqfiMpKLXJ", "dcappuser")
        viewModelScope.launch {
            repository.getToken(user).collect {
                if (it is Resource.Success) {
                    it.data?.token?.let { it1 -> prefs.saveToken("bearer $it1") }
                    callSimStatusAPI(viewModelScope, repository, {
                        onSuccess.invoke()
                    }, { onFail.invoke() })
                } else {
                    onFail.invoke()
                }
            }
        }
    }
}

private fun callSimStatusAPI(
    viewModelScope: CoroutineScope,
    repository: DeviceInfoRepository,
    onSuccess: () -> Unit,
    onFail: () -> Unit
) {
    val tm = BaseApplication.INSTANCE.getSystemService(TelephonyManager::class.java)
    val simStatusRequest = SimStatusRequest(
        appversion = BuildConfig.VERSION_NAME,
        uniqueId = BaseApplication.INSTANCE.getIccId().first,
        operatorCode = tm.simOperator,
        operatorName = tm.simOperatorName,
        deviceserialno= getDeviceSerial(),
        simStatus = getSimStatus(tm.simState),
        uniqueiderror = BaseApplication.INSTANCE.getIccId().second,
        deviceUniqueId = BaseApplication.INSTANCE.getAndroidId(),
        devicemanufacturer = getDeviceName(),
        deviceos = getDeviceOS(),
        devicemodel = getDeviceModel(),
        deviceimei = BaseApplication.INSTANCE.getImei(),
    )
    val prefs = SharedPrefs(BaseApplication.INSTANCE)
    viewModelScope.launch {
        repository.getSimStatus(simStatusRequest).collect {
            if (it is Resource.Success) {
                it.data?.simheader?.let { prefs.setSimHeader(it) }
                onSuccess.invoke()
            } else {
                onFail.invoke()
            }
        }
    }
}

fun getOTAUpdate(
    viewModelScope: CoroutineScope,
    repository: DeviceInfoRepository,
    onSuccess: (reponse: OTAResponse?) -> Unit,
    onFail: () -> Unit
) {
    val network = Network(BaseApplication.INSTANCE)
    if (network.isConnected()) {
        viewModelScope.launch {
            repository.getOTAUpdate(getDeviceModel()).collect {
                if (it is Resource.Success) {
                    onSuccess.invoke(it.data)
                } else {
                    onFail.invoke()
                }
            }
        }
    }
}

fun validateKioskCode(
    viewModelScope: CoroutineScope,
    repository: DeviceInfoRepository,
    code: String,
    onSuccess: () -> Unit,
    onFail: (message: String?) -> Unit
) {
    val network = Network(BaseApplication.INSTANCE)
    if (network.isConnected()) {
        viewModelScope.launch {
            repository.validateKioskCode(code).collect {
                if (it is Resource.Success) {
                    if (it.data?.message.equals(SUCCESS, true)) {
                        onSuccess.invoke()
                    } else {
                        onFail.invoke(it.data?.error?.message)
                    }
                } else if (it is Resource.Error) {
                    onFail.invoke(it.message)
                } else if (it is Resource.APIException) {
                    onFail.invoke(it.message)
                }
            }
        }
    }
}

fun validateKioskCodeV2(
    viewModelScope: CoroutineScope,
    repository: DeviceInfoRepository,
    exitKioskRequest: ExitKioskRequest,
    onSuccess: () -> Unit,
    onFail: (message: String?) -> Unit
) {
    val network = Network(BaseApplication.INSTANCE)
    if (network.isConnected()) {
        viewModelScope.launch {
            repository.validateKioskCodeV2(exitKioskRequest = exitKioskRequest).collect {
                if (it is Resource.Success) {
                    if (it.data?.message.equals(SUCCESS, true)) {
                        onSuccess.invoke()
                    } else {
                        onFail.invoke(it.data?.error?.message)
                    }
                } else if (it is Resource.Error) {
                    onFail.invoke(it.message)
                } else if (it is Resource.APIException) {
                    onFail.invoke(it.message)
                }
            }
        }
    }
}

fun sendDeviceInfo(
    viewModelScope: CoroutineScope,
    deviceInfo: JsonObject,
    repository: DeviceInfoRepository,
    onSuccess: (DeviceSettingResponse?) -> Unit,
    onFail: (message: String?) -> Unit
) {
    val network = Network(BaseApplication.INSTANCE)
    if (network.isConnected()) {
        viewModelScope.launch {
            repository.sendDeviceInfo(deviceInfo).collect {
                if (it is Resource.Success) {
                    onSuccess.invoke(it.data)
                } else if (it is Resource.Error) {
                    onFail.invoke(it.message)
                } else if (it is Resource.APIException) {
                    onFail.invoke(it.message)
                }
            }
        }
    }
}

fun sendWifiToggle(
    viewModelScope: CoroutineScope,
    isWifi: Boolean?,
    repository: DeviceInfoRepository,
    onSuccess: ((ResponseBody?) -> Unit)? = null,
    onFail: ((message: String?) -> Unit)? = null
) {
    val network = Network(BaseApplication.INSTANCE)
    if (network.isConnected()) {
        viewModelScope.launch {
            repository.sendWifiToggle(deviceImei = BaseApplication.INSTANCE.getImei(),
                deviceSerialNo = getDeviceSerial(),
                clientDateTime = todayDate(withFormat = API_RETURN_UPDATE_FORMAT),
                isWifi = isWifi
            ).collect {
                if (it is Resource.Success) {
                    onSuccess?.invoke(it.data)
                } else if (it is Resource.Error) {
                    onFail?.invoke(it.message)
                } else if (it is Resource.APIException) {
                    onFail?.invoke(it.message)
                }
            }
        }
    }
}


fun uploadLogFile(
    viewModelScope: CoroutineScope,
    fileUpload: MultipartBody.Part?,
    deviceid: RequestBody?,
    deviceimei: RequestBody?,
    filename: RequestBody?,
    code: RequestBody?,
    clientTs: RequestBody?,
    repository: DeviceInfoRepository,
    onResponse: ((response: Resource<ResponseBody>) -> Unit)? = null,
    onSuccess: ((ResponseBody?) -> Unit)? = null,
    onFail: ((message: String?) -> Unit)? = null
) {
    val network = Network(BaseApplication.INSTANCE)
    if (network.isConnected()) {
        viewModelScope.launch {
            repository.uploadFile(fileUpload, deviceid, deviceimei, filename, code,clientTs).collect {
                onResponse?.invoke(it)
                if (it is Resource.Success) {
                    onSuccess?.invoke(it.data)
                } else if (it is Resource.Error) {
                    onFail?.invoke(it.message)
                } else if (it is Resource.APIException) {
                    onFail?.invoke(it.message)
                }
            }
        }
    }
}

fun getVolume(
    viewModelScope: CoroutineScope,
    repository: DeviceInfoRepository,
    onSuccess: (response: VolumeResponse?) -> Unit,
    onFail: () -> Unit
) {
    val network = Network(BaseApplication.INSTANCE)
    if (network.isConnected()) {
        viewModelScope.launch {
            repository.getVolume().collect {
                if (it is Resource.Success) {
                    onSuccess.invoke(it.data)
                } else {
                    onFail.invoke()
                }
            }
        }
    }
}

fun getAvailableNetwork(
    viewModelScope: CoroutineScope,
    repository: DeviceInfoRepository,
    code: String,
    onSuccess: (response: AvailableNetworkResponse?) -> Unit,
    onFail: () -> Unit
) {
    val network = Network(BaseApplication.INSTANCE)
    if (network.isConnected()) {
        viewModelScope.launch {
            repository.getAvailableNetwork(code).collect {
                if (it is Resource.Success) {
                    onSuccess.invoke(it.data)
                } else {
                    if ((it is Resource.APIException) or (it is Resource.Error))
                        onFail.invoke()
                }
            }
        }
    }
}

fun getVersionJobDetails(
    viewModelScope: CoroutineScope,
    repository: DeviceInfoRepository,
    context: Context,
    onResponse: ((response: Resource<VersionJobResponse>) -> Unit)? = null,
    onSuccess: ((response: VersionJobResponse?) -> Unit)? = null,
    onFail: (() -> Unit)? = null
) {
    val network = Network(BaseApplication.INSTANCE)
    val izonAppVersion = try {
        context.packageManager?.getPackageInfo(izonPackageName, 0)?.versionName
            ?: "0.0.0"
    } catch (e: PackageManager.NameNotFoundException) {
        "0.0.0"
    }
    val versionRequest = VersionRequest(
        deviceid = context.getAndroidId(),
        dcappversion = BuildConfig.VERSION_NAME,
        izonappversion = izonAppVersion
    )
    if (network.isConnected()) {
        viewModelScope.launch {
            repository.getVersionsJob(versionRequest).collect {
                onResponse?.invoke(it)
                if (it is Resource.Success) {
                    onSuccess?.invoke(it.data)
                } else {
                    if ((it is Resource.APIException) or (it is Resource.Error))
                        onFail?.invoke()
                }
            }
        }
    }
}

fun getDownloadInfoJob(
    viewModelScope: CoroutineScope,
    repository: DeviceInfoRepository,
    context: Context,
    onResponse: ((response: Resource<DownloadInfoJobResponse>) -> Unit)? = null,
    onSuccess: ((response: DownloadInfoJobResponse?) -> Unit)? = null,
    onFail: (() -> Unit)? = null
) {
    val prefs = SharedPrefs(context)
    val network = Network(BaseApplication.INSTANCE)
    if (network.isConnected()) {
        viewModelScope.launch {
            repository.getDownloadJobUrl(prefs.getFacilityCode()).collect {
                onResponse?.invoke(it)
                if (it is Resource.Success) {
                    onSuccess?.invoke(it.data)
                } else {
                    if ((it is Resource.APIException) or (it is Resource.Error))
                        onFail?.invoke()
                }
            }
        }
    }
}

fun getMediaInfoJob(
    viewModelScope: CoroutineScope,
    repository: DeviceInfoRepository,
    context: Context,
    onResponse: ((response: Resource<MediaJobResponse>) -> Unit)? = null,
    onSuccess: ((response: MediaJobResponse?) -> Unit)? = null,
    onFail: (() -> Unit)? = null
) {
    val prefs = SharedPrefs(context)
    val network = Network(BaseApplication.INSTANCE)
    if (network.isConnected()) {
        viewModelScope.launch {
            repository.getMediaJobUrls(prefs.getFacilityCode()).collect {
                onResponse?.invoke(it)
                if (it is Resource.Success) {
                    onSuccess?.invoke(it.data)
                } else {
                    if ((it is Resource.APIException) or (it is Resource.Error))
                        onFail?.invoke()
                }
            }
        }
    }
}

