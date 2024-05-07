package com.izontechnology.dcapp.data.remote_service

import com.google.gson.JsonObject
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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Employee management service interface.
 *
 * This interface defines the methods for managing data in the application.
 */
interface DeviceInfoService {
    @POST("adddevice")
    suspend fun addDevice(@Body deviceInfo: DeviceInfo): Response<ResponseBody>

    @POST("batterystatus")
    suspend fun batteryStatus(@Body batteryInfo: BatteryInfo): Response<ResponseBody>

    @GET("getdevicebrightness/{ipAddress}/{deviceId}")
    suspend fun getBrightness(
        @Path("ipAddress") ipAddress: String = "", @Path("deviceId") deviceId: String = ""
    ): Response<BrightnessResponse>

    @GET("/api/dcapp/tiles/getTiles")
    suspend fun getDownloadUrl(@Query("code") code: String = ""): Response<DownloadInfoResponse>

    @GET("/api/dcapp/jobs/tiles/getTiles")
    suspend fun getDownloadJobUrl(@Query("code") code: String = ""): Response<DownloadInfoJobResponse>

    @GET("/api/dcapp/media/getMedia")
    suspend fun getMediaUrls(@Query("code") code: String = ""): Response<MediaDownloadResponse>

    @Headers("CONNECT_TIMEOUT:300000", "READ_TIMEOUT:300000", "WRITE_TIMEOUT:300000")
    @Multipart
    @POST("/api/dcapp/device/devicelog")
    suspend fun uploadFile(
        @Part fileUpload: MultipartBody.Part?,
        @Part("deviceid") deviceid: RequestBody?,
        @Part("deviceimei") deviceimei: RequestBody?,
        @Part("filename") filename: RequestBody?,
        @Part("code") code: RequestBody?,
        @Part("clientTs") clientTs: RequestBody?,
    ): Response<ResponseBody>

    @GET("/api/dcapp/jobs/media/getMedia")
    suspend fun getMediaJobUrls(@Query("code") code: String = ""): Response<MediaJobResponse>

    @GET("/api/dcapp/course/validateCode")
    suspend fun checkValidCode(@Query("code") code: String = ""): Response<ValidCodeResponse>

    @GET("/api/dcapp/kiosk/validateCode")
    suspend fun validateKioskCode(@Query("code") code: String = ""): Response<ValidCodeResponse>

    @POST("/api/dcapp/kiosk/validateCodev2")
    suspend fun validateKioskCodeV2(@Body exitKioskRequest: ExitKioskRequest): Response<ValidCodeResponse>

    @GET("/api/dcapp/networks/availablenetworks")
    suspend fun getAvailableNetwork(@Query("clubcode") clubcode: String = ""): Response<AvailableNetworkResponse>

    @GET("/api/dcapp/ota/getOta")
    suspend fun getOTAUpdate(@Query("devicemodel") devicemodel: String = ""): Response<OTAResponse>

    @POST("/api/dcapp/getToken")
    suspend fun getToken(@Body user: UserRequest): Response<TokenResponse>

    @POST("/api/dcapp/appversions/getappversions")
    suspend fun getVersions(@Body versionRequest: VersionRequest): Response<VersionResponse>

    @POST("/api/dcapp/jobs/appversions/getappversions")
    suspend fun getVersionsJob(@Body versionRequest: VersionRequest): Response<VersionJobResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/dcapp/device/settings")
    suspend fun sendDeviceInfo(@Body deviceRequest: JsonObject): Response<DeviceSettingResponse>

    @FormUrlEncoded
    @POST("/api/dcapp/networks/wifitoggle")
    suspend fun sendWifiToggle(
        @Field("deviceimei") deviceImei: String?,
        @Field("deviceserialno") deviceSerialNo: String?,
        @Field("clientdatetime") clientDateTime: String?,
        @Field("iswifi") isWifi: Boolean? = false
    ): Response<ResponseBody>

    @GET("/api/dcapp/device/getvolume")
    suspend fun getVolume(): Response<VolumeResponse>

    @POST("/api/dcapp/sim/validatesim")
    suspend fun getSimStatus(@Body simStatusRequest: SimStatusRequest): Response<SimStatusResponse>
}