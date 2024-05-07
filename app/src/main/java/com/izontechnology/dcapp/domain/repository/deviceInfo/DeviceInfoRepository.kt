package com.izontechnology.dcapp.domain.repository.deviceInfo

import com.google.gson.JsonObject
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.data.request.BatteryInfo
import com.izontechnology.dcapp.data.request.DeviceInfo
import com.izontechnology.dcapp.data.request.ExitKioskRequest
import com.izontechnology.dcapp.data.request.SimStatusRequest
import com.izontechnology.dcapp.data.request.UserRequest
import com.izontechnology.dcapp.data.request.VersionRequest
import com.izontechnology.dcapp.data.response.AvailableNetworkResponse
import com.izontechnology.dcapp.data.response.BrightnessResponse
import com.izontechnology.dcapp.data.response.DeviceSettingResponse
import com.izontechnology.dcapp.data.response.DownloadInfoJobResponse
import com.izontechnology.dcapp.data.response.DownloadInfoResponse
import com.izontechnology.dcapp.data.response.MediaDownloadResponse
import com.izontechnology.dcapp.data.response.MediaJobResponse
import com.izontechnology.dcapp.data.response.OTAResponse
import com.izontechnology.dcapp.data.response.SimStatusResponse
import com.izontechnology.dcapp.data.response.TokenResponse
import com.izontechnology.dcapp.data.response.ValidCodeResponse
import com.izontechnology.dcapp.data.response.VersionJobResponse
import com.izontechnology.dcapp.data.response.VersionResponse
import com.izontechnology.dcapp.data.response.VolumeResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody

interface DeviceInfoRepository {
    suspend fun addDevice(deviceInfo: DeviceInfo): Flow<Resource<ResponseBody>>
    suspend fun addBattery(batteryInfo: BatteryInfo): Flow<Resource<ResponseBody>>
    suspend fun getBrightness(
        ipAddress: String, deviceId: String
    ): Flow<Resource<BrightnessResponse>>

    suspend fun getDownloadUrl(code: String): Flow<Resource<DownloadInfoResponse>>
    suspend fun getDownloadJobUrl(code: String): Flow<Resource<DownloadInfoJobResponse>>
    suspend fun getMediaUrls(code: String): Flow<Resource<MediaDownloadResponse>>
    suspend fun uploadFile(
        fileUpload: MultipartBody.Part?,
        deviceid: RequestBody?,
        deviceimei: RequestBody?,
        filename: RequestBody?,
        code: RequestBody?,
        clientTs: RequestBody?
    ): Flow<Resource<ResponseBody>>

    suspend fun getMediaJobUrls(code: String): Flow<Resource<MediaJobResponse>>
    suspend fun getVersions(versionRequest: VersionRequest): Flow<Resource<VersionResponse>>
    suspend fun getVersionsJob(versionRequest: VersionRequest): Flow<Resource<VersionJobResponse>>
    suspend fun validateKioskCode(code: String): Flow<Resource<ValidCodeResponse>>
    suspend fun validateKioskCodeV2(exitKioskRequest: ExitKioskRequest): Flow<Resource<ValidCodeResponse>>
    suspend fun getToken(user: UserRequest): Flow<Resource<TokenResponse>>
    suspend fun getOTAUpdate(devicemodel: String): Flow<Resource<OTAResponse>>
    suspend fun sendDeviceInfo(deviceRequest: JsonObject): Flow<Resource<DeviceSettingResponse>>
    suspend fun sendWifiToggle(
        deviceImei: String?,
        deviceSerialNo: String?,
        clientDateTime: String?,
        isWifi: Boolean?
    ): Flow<Resource<ResponseBody>>

    suspend fun getVolume(): Flow<Resource<VolumeResponse>>
    suspend fun getAvailableNetwork(code: String): Flow<Resource<AvailableNetworkResponse>>
    suspend fun getSimStatus(simStatusRequest: SimStatusRequest): Flow<Resource<SimStatusResponse>>
    suspend fun checkValidCode(code: String): Flow<Resource<ValidCodeResponse>>
}