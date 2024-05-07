package com.izontechnology.dcapp.domain.repository.deviceInfo

import com.google.gson.JsonObject
import com.izontechnology.dcapp.base.common.Resource
import com.izontechnology.dcapp.base.common.makeApiCallNormal
import com.izontechnology.dcapp.data.remote_service.DeviceInfoService
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.inject.Inject

class DeviceInfoRepositoryImpl @Inject constructor(private val api: DeviceInfoService) :
    DeviceInfoRepository {

    override suspend fun addDevice(deviceInfo: DeviceInfo): Flow<Resource<ResponseBody>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.addDevice(deviceInfo) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun addBattery(batteryInfo: BatteryInfo): Flow<Resource<ResponseBody>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.batteryStatus(batteryInfo) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getBrightness(ipAddress: String,deviceId:String): Flow<Resource<BrightnessResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getBrightness(ipAddress, deviceId) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getDownloadUrl(code: String): Flow<Resource<DownloadInfoResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getDownloadUrl(code) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
    override suspend fun getDownloadJobUrl(code: String): Flow<Resource<DownloadInfoJobResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getDownloadJobUrl(code) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getMediaUrls(code: String): Flow<Resource<MediaDownloadResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getMediaUrls(code) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun uploadFile(fileUpload: MultipartBody.Part?, deviceid: RequestBody?, deviceimei: RequestBody?, filename: RequestBody?,code:RequestBody?, clientTs: RequestBody?): Flow<Resource<ResponseBody>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.uploadFile(fileUpload, deviceid, deviceimei, filename,code, clientTs) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
    override suspend fun getMediaJobUrls(code: String): Flow<Resource<MediaJobResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getMediaJobUrls(code) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getVersions(versionRequest: VersionRequest): Flow<Resource<VersionResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getVersions(versionRequest) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
  override suspend fun getVersionsJob(versionRequest: VersionRequest): Flow<Resource<VersionJobResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getVersionsJob(versionRequest) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun validateKioskCode(code: String): Flow<Resource<ValidCodeResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.validateKioskCode(code) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
    override suspend fun validateKioskCodeV2(exitKioskRequest: ExitKioskRequest): Flow<Resource<ValidCodeResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.validateKioskCodeV2(exitKioskRequest) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getToken(user: UserRequest): Flow<Resource<TokenResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getToken(user) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getOTAUpdate(devicemodel: String): Flow<Resource<OTAResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getOTAUpdate(devicemodel) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun sendDeviceInfo(deviceRequest: JsonObject): Flow<Resource<DeviceSettingResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.sendDeviceInfo(deviceRequest) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
    override suspend fun sendWifiToggle(deviceImei: String?,
                                        deviceSerialNo: String?,
                                        clientDateTime: String?,
                                        isWifi: Boolean? ): Flow<Resource<ResponseBody>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.sendWifiToggle(deviceImei, deviceSerialNo,clientDateTime, isWifi) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
override suspend fun getVolume(): Flow<Resource<VolumeResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getVolume() }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getAvailableNetwork(code: String): Flow<Resource<AvailableNetworkResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getAvailableNetwork(code) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getSimStatus(simStatusRequest: SimStatusRequest): Flow<Resource<SimStatusResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.getSimStatus(simStatusRequest) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun checkValidCode(code: String): Flow<Resource<ValidCodeResponse>> {
        return flow {
            emit(Resource.Loading)
            val result = makeApiCallNormal { api.checkValidCode(code) }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }
}