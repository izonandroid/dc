package com.izontechnology.dcapp.services

import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.izontechnology.dcapp.BuildConfig
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.common.NetworkConnectivity
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.data.common.toRequestBody
import com.izontechnology.dcapp.data.common.toRequestBodyFile
import com.izontechnology.dcapp.data.request.DeviceRequest
import com.izontechnology.dcapp.data.response.MediaDownloadResponseItem
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import com.izontechnology.dcapp.downloadmanagerplus.FileItem
import com.izontechnology.dcapp.downloadmanagerplus.classes.Downloader
import com.izontechnology.dcapp.downloadmanagerplus.enums.DownloadReason
import com.izontechnology.dcapp.downloadmanagerplus.enums.DownloadStatus
import com.izontechnology.dcapp.downloadmanagerplus.enums.Storage
import com.izontechnology.dcapp.downloadmanagerplus.interfaces.DownloadListener
import com.izontechnology.dcapp.downloadmanagerplus.model.DownloadItem
import com.izontechnology.dcapp.downloadmanagerplus.utils.Utils
import com.izontechnology.dcapp.utils.API_RETURN_DATE_FORMAT
import com.izontechnology.dcapp.utils.API_RETURN_UPDATE_FORMAT
import com.izontechnology.dcapp.utils.DOWNLOAD_DIR_PATH
import com.izontechnology.dcapp.utils.DeviceLogs
import com.izontechnology.dcapp.utils.GPSTracker
import com.izontechnology.dcapp.utils.getAndroidId
import com.izontechnology.dcapp.utils.getBatteryPer
import com.izontechnology.dcapp.utils.getConnectedNetworkType
import com.izontechnology.dcapp.utils.getCurrentBrightness
import com.izontechnology.dcapp.utils.getDeviceBuildDate
import com.izontechnology.dcapp.utils.getDeviceBuildNumber
import com.izontechnology.dcapp.utils.getDeviceModel
import com.izontechnology.dcapp.utils.getDeviceName
import com.izontechnology.dcapp.utils.getDeviceOS
import com.izontechnology.dcapp.utils.getDeviceSerial
import com.izontechnology.dcapp.utils.getDeviceWifi
import com.izontechnology.dcapp.utils.getExtNo
import com.izontechnology.dcapp.utils.getFirmwareVersion
import com.izontechnology.dcapp.utils.getIccId
import com.izontechnology.dcapp.utils.getImei
import com.izontechnology.dcapp.utils.getMaxBrightness
import com.izontechnology.dcapp.utils.getOTAUpdate
import com.izontechnology.dcapp.utils.getVolume
import com.izontechnology.dcapp.utils.isFileDownloaded
import com.izontechnology.dcapp.utils.loadJSONFromFile
import com.izontechnology.dcapp.utils.sendDeviceInfo
import com.izontechnology.dcapp.utils.setScreenTimeOut
import com.izontechnology.dcapp.utils.setVolume
import com.izontechnology.dcapp.utils.todayDate
import com.izontechnology.dcapp.utils.uploadLogFile
import com.izontechnology.dcapp.utils.wifi.getSSIDOfConnectedWifi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import javax.inject.Inject


@AndroidEntryPoint
class ForegroundService : Service() {
    var handlerDeviceStatus: Handler? = null

    @Inject
    lateinit var repository: DeviceInfoRepository

    @Inject
    lateinit var prefs: SharedPrefs

    @Inject
    lateinit var network: NetworkConnectivity

    companion object {
        private const val SERVICE_NOTIFICATION_ID = 1
        private var instance: ForegroundService? = null

        fun getInstance(): ForegroundService? {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getAction().equals("start")) {
            startForeground(SERVICE_NOTIFICATION_ID, createNotification())
            getDeviceStatus()
//            otaUpdateAPICall()
        }
        if (intent?.getAction().equals("stop")) {
            stopForeground(true);
            stopSelfResult(startId);
        }
        // Your background task code goes here

        return START_STICKY
    }

    private fun otaUpdateAPICall() {
        getOTAUpdate(viewModelScope = GlobalScope, repository = repository, onSuccess = {
            downloadFile(
                MediaDownloadResponseItem(
                    url = it?.fileurl.toString(), filename = Utils.getFileName(it?.fileurl)
                )
            )
            downloadFile(
                MediaDownloadResponseItem(
                    url = it?.filezip.toString(), filename = Utils.getFileName(it?.filezip)
                )
            )
//            downloadFile(MediaDownloadResponseItem(url = "https://cp.izongolf.com/tiles/downloads/1001.zip", filename = "firmwareImg"))
//            downloadFile(MediaDownloadResponseItem(url = "https://cp.izongolf.com/tiles/downloads/1007.zip", filename = "firmwareZip"))
        }, onFail = {})
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        instance = null
    }

    fun getDeviceStatus() {
        var minuteCount = 0
        handlerDeviceStatus = Handler(Looper.getMainLooper())
//        var delay = 2000 // 1000 milliseconds == 1 second
        var delay = 1000 // 1000 milliseconds == 1 second
        handlerDeviceStatus?.postDelayed(object : Runnable {
            override fun run() {
                sendDeviceInfoAPI()
                handlerDeviceStatus?.postDelayed(this, delay.toLong())
                delay = 60000
                if (minuteCount % 30 == 0) {
                    setRemoteVolume()
                }
                if (minuteCount % 5 == 0) {
                    pullSystemLog()
                }
                minuteCount++
            }
        }.apply {

        }, delay.toLong()
        )
    }

    private fun pullSystemLog() {
        try {
            val logFileName = "izonlog.txt"
            val path = File("$DOWNLOAD_DIR_PATH/LOGS/")
            val logFile = File(path, logFileName)
            if (!path.exists()) {
                path.mkdirs()
            }
            if (!logFile.exists()) {
                logFile.createNewFile()
            }

            val cmd = "logcat -d " + logFile.absolutePath
            Runtime.getRuntime().exec(cmd)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendDeviceInfoAPI() {
        Log.d("service", "device info api callede")
        val json =
            loadJSONFromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/CartInfo.json")?.let {
                JsonParser.parseString(it)
            }
        val networkStatus = if (network.isConnected()) {
            "connected"
        } else {
            "disconnected"
        }
        var deviceLog = ""
        var accuracyMeter = GPSTracker(this).getLocation()?.accuracy
        var accuracy = "No Signal"
        if ((accuracyMeter ?: 0F) in 1F..100F) {
            accuracy = "High"
        } else {
            accuracy = "Low"
        }
//        HyperLog.getLines(200) {
//            deviceLog = it
        val deviceRequest = DeviceRequest(
            appversion = BuildConfig.VERSION_NAME,
            batterypercentage = getBatteryPer().toString(),
            brightnessvalue = getCurrentBrightness().toString(),
            code = prefs.getFacilityCode(),
            devicemanufacturer = getDeviceName(),
            deviceos = getDeviceOS(),
            deviceuniqueid = getAndroidId(),
            devicevolume = getVolume().toString(),
            devicemodel = getDeviceModel(),
            deviceiccid = getIccId().first,
            deviceimei = getImei(),
            deviceextno = getExtNo(),
            devicebuildnumber = getDeviceBuildNumber(),
            devicefwversion = getFirmwareVersion(),
            devicebuildate = getDeviceBuildDate(),
            networkstatus = networkStatus,
            networkconnectiontype = getConnectedNetworkType(),
            latitude = GPSTracker(this).mLatitude.toString(),
            longitude = GPSTracker(this).mLongitude.toString(),
            maxbrightnessvalue = getMaxBrightness().toString(),
            deviceserialno = getDeviceSerial(),
            devicegpssignalstrength = accuracy,
            devicewifitoggle = getDeviceWifi(),
            clientdatetime = todayDate(withFormat = API_RETURN_UPDATE_FORMAT),
            devicelog = deviceLog,
            wifiname = if (getConnectedNetworkType().equals(
                    "wi-fi", true
                )
            ) getSSIDOfConnectedWifi() else ""
        )
        val data: JsonObject =
            JsonParser.parseString(Gson().toJson(deviceRequest)).getAsJsonObject()
        data.add("izondevice", json)

        sendDeviceInfo(GlobalScope, data, repository, { response ->
            if(response?.iscartroundrunning == true){
                setScreenTimeOut(Integer.MAX_VALUE)
            }else{
                setScreenTimeOut(300000)
//                setScreenTimeOut(Integer.MAX_VALUE)
            }
            if ((response?.izonrounddata?.isroundended == true) and !(response?.izonrounddata?.isfileuploaded == true)) {
                callUploadLogFile()
            }
            response?.izonkiosk?.exitcode?.let { it1 -> prefs.saveExitKioskCode(it1) }
//                if ((response?.iscartroundrunning == true) and (isIzonGolfAppRunning())) {
//                if (LegacyUtils.isDeviceOwner(this)){
//                    WiFiManager(this).closeWiFi(BaseApplication.activity)
//                }else{
//                    WiFiManager(this).openWiFi(BaseApplication.activity)
//                }
//                }
        }) {}
//        }


    }

    private fun callUploadLogFile() {
        var logFileName = todayDate(API_RETURN_DATE_FORMAT) + "_log.txt"
        val path = File("$DOWNLOAD_DIR_PATH/LOGS/")
        if (path.exists()) {
            path.listFiles { dir, name ->
                if (!name.contains("extractionlog.txt")) {
                    logFileName = name
                    val logFile = File(path, logFileName)
                    if (logFile.exists()) {
                        uploadLogFile(viewModelScope = GlobalScope,
                            deviceid = getAndroidId()?.toRequestBody(),
                            deviceimei = getImei()?.toRequestBody(),
                            clientTs = todayDate(withFormat = API_RETURN_DATE_FORMAT).toRequestBody(),
                            filename = logFileName.toRequestBody(),
                            code = prefs.getFacilityCode().toRequestBody(),
                            fileUpload = logFile.toRequestBodyFile("fileUpload"),
                            repository = repository,
                            onSuccess = {
                                try {
//                                    logFile.delete()
                                    val writer: PrintWriter = PrintWriter(logFile)
                                    writer.print("")
                                    writer.close()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            onFail = {})
                    }
                }
                true
            }
        }

    }

    fun setRemoteVolume() {
        getVolume(GlobalScope, repository, {
            setVolume(it?.volume?.times(10) ?: 30)
        }, {})
    }


    private fun createNotification(): Notification {
        val channelId = "ForegroundServiceChannel"
        val channelName = "Foreground Service Channel"

        val builder =
            NotificationCompat.Builder(this, channelId).setContentTitle("Foreground Service")
                .setContentText("Running...").setSmallIcon(R.mipmap.ic_launcher)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return builder.build()
    }

    private fun getDownloader(item: FileItem, listener: DownloadListener?): Downloader? {
        val request: Downloader =
            Downloader.getInstance(applicationContext).setListener(listener).setUrl(item.getUri())
                .setToken(item.getToken())
                .setKeptAllDownload(false) //if true: canceled download token keep in database
                .setAllowedOverRoaming(true).setVisibleInDownloadsUi(true)
                .setScanningByMediaScanner(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
//             .setCustomDestinationDir(Storage.DIRECTORY_DOWNLOADS, Utils.getFileName(item.getUri()))//TargetApi 28 and lower
                .setDestinationDir(
                    Storage.DIRECTORY_DOWNLOADS,
                    "/FirmWare/" + if (item.mediaItem != null) item.mediaItem?.filename else Utils.getFileName(
                        item.getUri()
                    )
                ).setNotificationTitle("Downloading")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(true) //Api 16 and higher
        }
        return request
    }

    private fun downloadFile(
        mediaItem: MediaDownloadResponseItem,
    ) {
        val item = FileItem()
        item.mediaItem = mediaItem
        item.uri = mediaItem.url
        item.fileName = mediaItem.filename
        item.token = mediaItem.filename
        item.fileSize = mediaItem.size ?: 0
        item.listener = getMediaDownloadListener(item)
        if (!this.isFileDownloaded(item, "")) {
            //Showing progress for running downloads.
            var downloader = getDownloader(item, item.listener)
            if (downloader?.getStatus(item.getToken()) === DownloadStatus.RUNNING || downloader?.getStatus(
                    item.getToken()
                ) === DownloadStatus.PAUSED || downloader?.getStatus(item.getToken()) === DownloadStatus.PENDING
            ) {
                if (downloader?.getStatus(item.getToken()) === DownloadStatus.PENDING || (downloader?.downloadedBytes
                        ?: 0) <= 0
                ) {
                    downloader?.cancel(item.getToken())
                } else if (downloader?.getStatus(item.getToken()) === DownloadStatus.PAUSED) {
                    //int status = Downloader.resume(this, item.getToken());
                    downloader?.resume()
                } else {
                    //int status = Downloader.pause(this, item.getToken());
                    downloader?.pause()
                }
            } else if (downloader?.getStatus(item.getToken()) === DownloadStatus.SUCCESSFUL) {
                try {
                    downloader?.deleteFile(item?.token, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                downloader?.start()
                DeviceLogs.e("download", "download successfull")
            } else {
                downloader?.start()
            }
        }
    }

    private fun getMediaDownloadListener(
        item: FileItem,
    ): DownloadListener {
        var lastStatus = DownloadStatus.NONE
        var lastPercent = 0
        return object : DownloadListener {
            override fun onComplete(
                totalBytes: Int, mDownloadItem: DownloadItem?, downloadUri: String?
            ) {
                item.downloadStatus = DownloadStatus.SUCCESSFUL
                if (lastStatus !== DownloadStatus.SUCCESSFUL) {
                    item.listener = null
                }
                lastStatus = DownloadStatus.SUCCESSFUL

            }

            override fun onPause(
                percent: Int,
                reason: DownloadReason,
                totalBytes: Int,
                downloadedBytes: Int,
                mDownloadItem: DownloadItem?
            ) {
                if (lastStatus !== DownloadStatus.PAUSED) {
                    item.downloadStatus = DownloadStatus.PAUSED
                }
                if (reason == DownloadReason.PAUSED_WAITING_FOR_NETWORK) {
//                    txtPauseReason.visibility = View.VISIBLE
//                    txtPauseReason.text = getString(R.string.no_internet_waiting_for_network)
                } else {
//                    txtPauseReason.visibility = View.GONE
//                    downloader?.resume()
                }
//                tvPercent.setText("${getMediaDownloadPer(size, data?.size, percent)}%")
                lastStatus = DownloadStatus.PAUSED
            }

            override fun onPending(
                percent: Int, totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?
            ) {
                if (lastStatus !== DownloadStatus.PENDING) {
                    item.downloadStatus = DownloadStatus.PENDING
                }
//                txtPauseReason.visibility = View.GONE
//                tvPercent.setText("${getMediaDownloadPer(size, data?.size, percent)}%")
                lastStatus = DownloadStatus.PENDING
            }

            override fun onFail(
                percent: Int,
                reason: DownloadReason,
                totalBytes: Int,
                downloadedBytes: Int,
                mDownloadItem: DownloadItem?
            ) {
                //Toast.makeText(NormalActivity.this, "Failed: " + reason, Toast.LENGTH_SHORT).show();
                if (lastStatus !== DownloadStatus.FAILED) {
                    item.downloadStatus = DownloadStatus.FAILED
//                    startMediaDownload(data, size)
                }
//                tvPercent.setText("${getMediaDownloadPer(size, data?.size, percent)}%")
//                txtPauseReason.visibility = View.GONE
                lastStatus = DownloadStatus.FAILED
            }

            override fun onCancel(
                totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?
            ) {
                if (lastStatus !== DownloadStatus.CANCELED) {
                    item.downloadStatus = DownloadStatus.CANCELED
                    try {
//                        startMediaDownload(data, size)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
//                tvPercent.setText("${getMediaDownloadPer(size, data?.size, 0)}%")
//                txtPauseReason.visibility = View.GONE
                lastStatus = DownloadStatus.CANCELED
            }

            override fun onRunning(
                percent: Int,
                totalBytes: Int,
                downloadedBytes: Int,
                downloadSpeed: Float,
                mDownloadItem: DownloadItem?
            ) {
                if (percent > lastPercent) {
                    lastPercent = percent
                }
                if (lastStatus !== DownloadStatus.RUNNING) {
                    item.downloadStatus = DownloadStatus.RUNNING
                }
//                txtPauseReason.visibility = View.GONE
//                tvPercent.setText("${getMediaDownloadPer(size, data?.size, percent)}%")
//                var remainTime = ((totalBytes - downloadedBytes) / (downloadSpeed * 1000)).toLong()
//                if ((remainTime >= 0) and (remainTime <= 86400)) {
//                    txtRemainingTime.text =
//                        "${getString(R.string.remaining)}: ${remainTime.formatTime()}"
//                } else {
//                    binding.txtRemainingTime.text = getString(R.string.remaining_calculating)
//                }
                lastStatus = DownloadStatus.RUNNING
            }
        }
    }
}
