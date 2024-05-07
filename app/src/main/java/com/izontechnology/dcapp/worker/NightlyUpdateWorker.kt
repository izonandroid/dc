package com.izontechnology.dcapp.worker

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.izontechnology.dcapp.BuildConfig
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.data.response.DownloadInfoResponse
import com.izontechnology.dcapp.data.response.MediaDownloadResponse
import com.izontechnology.dcapp.data.response.Network
import com.izontechnology.dcapp.data.response.NetworkType
import com.izontechnology.dcapp.data.response.OTAResponse
import com.izontechnology.dcapp.data.response.VersionResponse
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import com.izontechnology.dcapp.downloadmanagerplus.FileItem
import com.izontechnology.dcapp.downloadmanagerplus.classes.Downloader
import com.izontechnology.dcapp.downloadmanagerplus.enums.DownloadReason
import com.izontechnology.dcapp.downloadmanagerplus.enums.DownloadStatus
import com.izontechnology.dcapp.downloadmanagerplus.enums.Errors
import com.izontechnology.dcapp.downloadmanagerplus.interfaces.ActionListener
import com.izontechnology.dcapp.downloadmanagerplus.interfaces.DownloadListener
import com.izontechnology.dcapp.downloadmanagerplus.model.DownloadItem
import com.izontechnology.dcapp.downloadmanagerplus.utils.Utils
import com.izontechnology.dcapp.utils.DOWNLOAD_DIR_PATH
import com.izontechnology.dcapp.utils.DeviceLogs
import com.izontechnology.dcapp.utils.FIRMWARE_JSON_PATH
import com.izontechnology.dcapp.utils.LegacyUtils
import com.izontechnology.dcapp.utils.TILE_JSON_PATH
import com.izontechnology.dcapp.utils.UnZipUtil
import com.izontechnology.dcapp.utils.executeAsyncTask
import com.izontechnology.dcapp.utils.getConnectedNetworkType
import com.izontechnology.dcapp.utils.getDownloadAllowNetwork
import com.izontechnology.dcapp.utils.getDownloadInfoJob
import com.izontechnology.dcapp.utils.getMediaInfoJob
import com.izontechnology.dcapp.utils.getOTAUpdate
import com.izontechnology.dcapp.utils.getUsedMemorySize
import com.izontechnology.dcapp.utils.getVersionJobDetails
import com.izontechnology.dcapp.utils.installApplication
import com.izontechnology.dcapp.utils.isDownloadSkip
import com.izontechnology.dcapp.utils.isFileDownloaded
import com.izontechnology.dcapp.utils.izonPackageName
import com.izontechnology.dcapp.utils.loadJSONFromFile
import com.izontechnology.dcapp.utils.log.HyperLog
import com.izontechnology.dcapp.utils.saveJSONFile
import com.izontechnology.dcapp.utils.scheduleWork
import com.izontechnology.dcapp.utils.wifi.WiFiManager
import com.izontechnology.dcapp.utils.wifi.listener.OnWifiConnectListener
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.URL
import javax.inject.Inject

@HiltWorker
class NightlyUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: DeviceInfoRepository,
    private val prefs: SharedPrefs
) : CoroutineWorker(context, params) {
    var isDownloadRunning = false
    var downloadList = ArrayList<FileItem>()

    val izonAppVersion = try {
        context.packageManager?.getPackageInfo(izonPackageName, 0)?.versionName ?: "0.0.0"
    } catch (e: PackageManager.NameNotFoundException) {
        "0.0.0"
    }
    val dcAppVersion = BuildConfig.VERSION_NAME

    @Inject
    lateinit var wifiManager: WiFiManager
    override suspend fun doWork(): Result {
        context.cacheDir.deleteRecursively()
        callAPIsAndStartDownload()
//        if (context.getConnectedNetworkType().equals("wi-fi", true)) {
//            callAPIsAndStartDownload()
//        }
//        else {
//            wifiManager.openWiFi(context)
//            wifiManager.setOnWifiConnectListener(object : OnWifiConnectListener {
//                override fun onWiFiConnectLog(log: String?) {
//                    DeviceLogs.e("wifi log", "wifi log onWiFiConnectLog ${log}")
//                }
//
//                override fun onWiFiConnectSuccess(SSID: String?) {
//                    Thread.sleep(500)
//                    GlobalScope.launch {
//                        callAPIsAndStartDownload()
//                    }
//                }
//
//                override fun onWiFiConnectFailure(SSID: String?) {
//                    Thread.sleep(1000)
//
//                }
//
//                override fun onWiFiDisconnectSuccess(SSID: String?) {
//                    Thread.sleep(1000)
//                    DeviceLogs.e("wifi log", "wifi log onWiFiDisconnectSuccess ${SSID}")
//                }
//
//            })
//        }

        // Indicate success or failure of the task
        return Result.success()
    }

    private suspend fun callAPIsAndStartDownload() {
        // Perform nightly update tasks here
        DeviceLogs.e("NightlyUpdateWorker", "Nightly update triggered!")
        HyperLog.d("NightlyUpdateWorker", "Nightly update triggered!")
        getVersionJobDetails(GlobalScope, repository, context, onSuccess = {
            DeviceLogs.e("NightlyUpdateWorker", "Nightly update API call")
            HyperLog.d("NightlyUpdateWorker", "Nightly update API call")
            if (it?.process == true) {
                downloadAppVersion(it?.appversions)
            }
        }, onFail = {})
        // Simulate some work by delaying for a few seconds
        delay(500)
        callDownloadInfoJob()
        delay(500)
        getMediaInfoJob(GlobalScope, repository, context, onSuccess = {
            if (it?.process == true) {
                startMediaDownload(it?.coursemedia, it?.coursemedia?.size)
            }
        }, onFail = {})
        delay(500)
        getOTAUpdate(GlobalScope, repository, onSuccess = {
            startFirmwareDownload(it)
        }, onFail = {})

        delay(3600000)
        scheduleWork(
            if (prefs.getNightUpdateHour() == -1) 1 else prefs.getNightUpdateHour(),
            if (prefs.getNightUpdateMinute() == -1) 1 else prefs.getNightUpdateMinute()
        )
    }

    private fun installAndUpdateButton(
        file: File,
        packageName: String,
        version: String?
    ) {
        if (packageName.equals(context.packageName)) {
            DeviceLogs.e("nightly", "install and launch")
            installApplication(
                context,
                file,
                packageName,
                version
            )
            LegacyUtils.reboot(context)
        } else {
            installApplication(
                context,
                file,
                packageName,
                version
            )
        }
    }

    private fun callDownloadInfoJob() {
        getDownloadInfoJob(GlobalScope, repository, context, onSuccess = {
            if (it?.process == true) {
                downloadZipFile(it?.tiles)
            }
        }, onFail = {})
    }

    private fun startFirmwareDownload(otaResponse: OTAResponse?) {
        val currentItem = FileItem()
        currentItem.token = Utils.getFileName(otaResponse?.fileurl)
        currentItem.uri = otaResponse?.fileurl ?: ""
        currentItem.packageName = ""
        currentItem.networkType = NetworkType(
            priority = "both", wifinetwork = Network(download = true)
        )
        currentItem.fileSize = -1
        currentItem.destinationSubDir = "/Firmware/"
        currentItem.onComplete = { totalBytes, mDownloadItem, downloadUri ->
            val json = JsonObject()
            json.addProperty("devicemodel", otaResponse?.devicemodel)
            json.addProperty("version", otaResponse?.version)
            saveJSONFile(Gson().toJson(json), FIRMWARE_JSON_PATH)
        }
        downloadList.add(currentItem)
        startSequenceDownload()
//        startDownload(currentItem)

        val zipItem = FileItem()
        zipItem.token = Utils.getFileName(otaResponse?.filezip)
        zipItem.uri = otaResponse?.filezip ?: ""
        zipItem.packageName = ""
        zipItem.networkType = NetworkType(
            priority = "both", wifinetwork = Network(download = true)
        )
        zipItem.fileSize = -1
        zipItem.destinationSubDir = "/Firmware/"
        zipItem.onComplete = { totalBytes, mDownloadItem, downloadUri ->
            val json = JsonObject()
            json.addProperty("devicemodel", otaResponse?.devicemodel)
            json.addProperty("version", otaResponse?.version)
            saveJSONFile(Gson().toJson(json), FIRMWARE_JSON_PATH)
        }
        downloadList.add(zipItem)
        startSequenceDownload()
//        startDownload(zipItem)
    }

    private fun startSequenceDownload() {
        if ((!isDownloadRunning) and (downloadList.isNotEmpty())) {
            isDownloadRunning = true
            val currentItem = downloadList.get(0)
            if (currentItem.listener == null) {
                currentItem.listener = getDownloadListener(
                    currentItem,
                    onComplete = { totalBytes, mDownloadItem, downloadUri ->
                        isDownloadRunning = false
                        downloadList.remove(currentItem)
                        startSequenceDownload()
                    },
                    onPause = { percent, reason, totalBytes, downloadedBytes, mDownloadItem ->
                        Downloader.getInstance(context)
                            .deleteFile(currentItem.token, object : ActionListener {
                                override fun onSuccess() {
                                    downLoadFile(currentItem)
                                }

                                override fun onFailure(error: Errors?) {
                                    downLoadFile(currentItem)
                                }
                            })
                    },
                    onFail = { percent, reason, totalBytes, downloadedBytes, mDownloadItem ->
                        isDownloadRunning = false
                        downloadList.remove(currentItem)
                        startSequenceDownload()
                    },
                    onCancel = { _, _, _ ->
                        isDownloadRunning = false
                        downloadList.remove(currentItem)
                        startSequenceDownload()
                    },
                )
            }
            startDownload(currentItem, onAlreadyDownloaded = {
                currentItem.onAlreadyDownloaded?.invoke()
                isDownloadRunning = false
                downloadList.remove(currentItem)
                startSequenceDownload()
            })
        }
        if (downloadList.isEmpty()) {
//            if (prefs.getIsWifiOff()){
            wifiManager.closeWiFi(context)
//            }
        }
    }

    private fun startMediaDownload(data: MediaDownloadResponse?, size: Int?) {
        data?.forEach { mediaItem ->
            DeviceLogs.e(
                "NightlyUpdateWorker", "media Download start ${data?.size} ${mediaItem.filename}"
            )
            HyperLog.d(
                "NightlyUpdateWorker", "media Download start ${data?.size} ${mediaItem.filename}"
            )
            val currentItem = FileItem()
            currentItem.token = mediaItem.filename ?: ""
            currentItem.uri = mediaItem.url ?: ""
            currentItem.packageName = ""
            currentItem.networkType = mediaItem?.networktype
            currentItem.fileSize = mediaItem?.size ?: -1
            currentItem.destinationSubDir = "/CONTENT/"
            currentItem.forceDownload = false
            currentItem.mediaItem = mediaItem

            downloadList.add(currentItem)
            startSequenceDownload()
        }
    }

    private fun downloadZipFile(data: DownloadInfoResponse?) {
        val savedData = try {
            Gson().fromJson(loadJSONFromFile(TILE_JSON_PATH), DownloadInfoResponse::class.java)
        } catch (e: Exception) {
            prefs.getTilesData()
        }
        if (savedData?.clubid?.equals(data?.clubid) != true) {
            GlobalScope.executeAsyncTask(onPreExecute = {}, doInBackground = {
                try {
                    File(DOWNLOAD_DIR_PATH + "/OVERLAY/").deleteRecursively()
                } catch (e: Exception) {
                    println("Error during unzip: ${e.message}")
                }
                "Result" // send data to "onPostExecute"
            }, onPostExecute = {
                startZipDownload(data)
            })
        } else if (!savedData.tilesversion.equals(data?.tilesversion)) {
            startZipDownload(data, true)
        } else {
            startZipDownload(data)
        }
    }

    private fun startZipDownload(data: DownloadInfoResponse?, forceDownload: Boolean = false) {
        val currentItem = FileItem()
        currentItem.token = "id1252"
        currentItem.uri = data?.tileszip ?: ""
        currentItem.packageName = ""
        currentItem.networkType = data?.networktype
        currentItem.fileSize = data?.filesize ?: -1
        currentItem.forceDownload = forceDownload
        currentItem.destinationSubDir = "/OVERLAY/"
        currentItem.onRunning = { percent, _, _, _, _ ->
            prefs.setZipExtracted(false)
            DeviceLogs.e("NightlyUpdateWorker", "NightlyUpdateWorker download per ${percent}")
            HyperLog.d("NightlyUpdateWorker", "NightlyUpdateWorker download per ${percent}")
        }
        currentItem.onComplete = { totalBytes, mDownloadItem, downloadUri ->
            if (downloadUri.isNullOrEmpty()) {
                mDownloadItem?.filePath?.let {
                    unzipFile(
                        it, File(mDownloadItem?.filePath).parent
                    )
                }
            } else {
                Uri.parse(downloadUri).path?.let { unzipFile(it, File(it).parent) }
            }
            saveJSONFile(Gson().toJson(data), TILE_JSON_PATH)
            data?.let { prefs.saveTilesData(it) }
        }
        currentItem.onAlreadyDownloaded = {
            val path = DOWNLOAD_DIR_PATH + "/OVERLAY/" + Utils.getFileName(currentItem.getUri())
            try {
                unzipFile(
                    path, File(path).parent
                )
                saveJSONFile(Gson().toJson(data), TILE_JSON_PATH)
                data?.let { prefs.saveTilesData(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        downloadList.add(currentItem)
        startSequenceDownload()
//        startDownload(currentItem)
    }

    fun unzipFile(zipFilePath: String, outputDir: String) {
//        if (!prefs.getZipExtracted())
            GlobalScope.executeAsyncTask(onPreExecute = {},
                doInBackground = {
                    val ram = context.getUsedMemorySize()
                    try {
                        HyperLog.ze("Unzip","Unzip Start Device Total RAM ${ram.first} MB, Free ram ${ram.second} MB, Available Ram ${ram.third} MB")
                        UnZipUtil().unzip(zipFilePath, outputDir) {}
                        HyperLog.ze("Unzip","Unzip completed Device Total RAM ${ram.first} MB, Free ram ${ram.second} MB, Available Ram ${ram.third} MB")
                        println("Unzip completed successfully.")
                    } catch (e: Exception) {
                        HyperLog.ze("Unzip","Unzip error Device Total RAM ${ram.first} MB, Free ram ${ram.second} MB, Available Ram ${ram.third} MB")
                        DeviceLogs.e("unzip", "Error during unzip")
                        println("Error during unzip: ${e.message}")
                        return@executeAsyncTask "unzip Error"
                    }
                    "Result" // send data to "onPostExecute"
                },
                onPostExecute = {
                    if (it.equals("unzip Error", ignoreCase = true)) {
                        DeviceLogs.e("unzip", "Error during unzip1")
                        try {
                            File(zipFilePath).deleteRecursively()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        prefs.setZipExtracted(false)
                        callDownloadInfoJob()
                    } else {
                        prefs.setZipExtracted(true)
                    }
                })
    }

    fun downloadAppVersion(data: VersionResponse?) {
        if (!data?.izonapp?.version.equals(izonAppVersion)) {
            val currentItem = FileItem()
            currentItem.token = data?.izonapp?.app + data?.izonapp?.version
            currentItem.uri = data?.izonapp?.downloadlink ?: ""
            currentItem.packageName = izonPackageName
            currentItem.version = data?.izonapp?.version
            currentItem.networkType = data?.izonapp?.networktype
            currentItem.destinationSubDir = "/Version/"
            downloadList.add(currentItem)
            currentItem.onRunning = { _, _, _, _, _ ->
                DeviceLogs.e("nightlyVersion", "appdownload")

            }
            currentItem.onComplete = { _, _, _ ->
                DeviceLogs.e("nightly", "download complete")
                val filePath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/Version/" + Utils.getFileName(
                        currentItem.getUri()
                    )
                installAndUpdateButton(
                    File(filePath),
                    currentItem.packageName,
                    currentItem.version
                )
            }
            startSequenceDownload()
//            startDownload(currentItem)
        }
        if (!data?.dcapp?.version.equals(dcAppVersion)) {
            val currentItem = FileItem()
            currentItem.token = data?.dcapp?.app + data?.dcapp?.version
            currentItem.uri = data?.dcapp?.downloadlink ?: ""
            currentItem.packageName = context.packageName
            currentItem.version = data?.dcapp?.version
            currentItem.networkType = data?.dcapp?.networktype
            currentItem.destinationSubDir = "/Version/"
            downloadList.add(currentItem)
            currentItem.onRunning = { _, _, _, _, _ ->
                DeviceLogs.e("nightlyVersion", "appdownload")

            }
            currentItem.onComplete = { _, _, _ ->
                DeviceLogs.e("nightly", "download complete")
                val filePath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/Version/" + Utils.getFileName(
                        currentItem.getUri()
                    )
                installAndUpdateButton(
                    File(filePath),
                    currentItem.packageName,
                    currentItem.version
                )
            }
            startSequenceDownload()
//            startDownload(currentItem)
        }
    }

    private fun startDownload(
        currentItem: FileItem, onAlreadyDownloaded: (() -> Unit)? = null
    ) {
        if (currentItem.networkType?.isDownloadSkip(context) == true) {
//            callNextScreen()
            onAlreadyDownloaded?.invoke()
        } else {
            if (currentItem.forceDownload) {
                downLoadFile(currentItem)
            } else if (currentItem.fileSize == -1) {
                GlobalScope.executeAsyncTask(onPreExecute = {}, doInBackground = {
                    try {
                        val fileLength = URL(currentItem.uri).openConnection().contentLength
                        currentItem.fileSize = fileLength
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, onPostExecute = {
                    checkAndDownload(currentItem, onAlreadyDownloaded)
                })
            } else {
                checkAndDownload(currentItem, onAlreadyDownloaded)
            }
        }
    }

    fun checkAndDownload(item: FileItem, onAlreadyDownloaded: (() -> Unit)? = null) {
        if (!context.isFileDownloaded(
                item, DOWNLOAD_DIR_PATH + item.destinationSubDir + Utils.getFileName(item.getUri())
            )
        ) {
            try {
                val downloaderItem = Downloader.getDownloadItem(context, item.token)
                if (downloaderItem.uri.equals(item.uri)) {
                    downLoadFile(item)
                } else {
                    Downloader.getInstance(context).deleteFile(item.token, object : ActionListener {
                        override fun onSuccess() {
                            downLoadFile(item)
                        }

                        override fun onFailure(error: Errors?) {
                            downLoadFile(item)
                        }
                    })
                }
            } catch (e: Exception) {
                downLoadFile(item)
            }
        } else {
            onAlreadyDownloaded?.invoke()
        }
    }

    private fun downLoadFile(item: FileItem) {
        var downloader = getDownloader(item, item.listener)
        try {
            downloader?.deleteFile(item?.token, object : ActionListener {
                override fun onSuccess() {
                    downloader?.start()
                }

                override fun onFailure(error: Errors?) {
                    downloader?.start()
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //Showing progress for running downloads.
        showProgress(item, item.listener)
    }

    private fun getDownloader(item: FileItem, listener: DownloadListener?): Downloader? {
        val request: Downloader =
            Downloader.getInstance(context).setListener(listener).setUrl(item.getUri())
                .setToken(item.getToken())
                .setKeptAllDownload(false) //if true: canceled download token keep in database
                .setAllowedOverRoaming(true).setVisibleInDownloadsUi(true)
                .setScanningByMediaScanner(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                .setAllowedNetworkTypes(
                    item.networkType.getDownloadAllowNetwork()
                        ?: DownloadManager.Request.NETWORK_WIFI
                )
//             .setCustomDestinationDir(Storage.DIRECTORY_DOWNLOADS, Utils.getFileName(item.getUri()))//TargetApi 28 and lower
                .setDestinationDir(
                    item.destinationDir, item.destinationSubDir + Utils.getFileName(item.getUri())
                ).setNotificationTitle("Downloading")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(true) //Api 16 and higher
        }
        return request
    }

    private fun showProgress(item: FileItem, listener: DownloadListener?) {
        getDownloader(item, listener)?.showProgress()
    }

    private fun getDownloadListener(
        item: FileItem,
        onComplete: ((totalBytes: Int, mDownloadItem: DownloadItem?, downloadUri: String?) -> Unit)? = null,
        onPause: ((percent: Int, reason: DownloadReason, totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?) -> Unit)? = null,
        onPending: ((percent: Int, totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?) -> Unit)? = null,
        onFail: ((percent: Int, reason: DownloadReason, totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?) -> Unit)? = null,
        onCancel: ((totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?) -> Unit)? = null,
        onRunning: ((percent: Int, totalBytes: Int, downloadedBytes: Int, downloadSpeed: Float, mDownloadItem: DownloadItem?) -> Unit)? = null
    ): DownloadListener {
        return object : DownloadListener {
            var lastStatus = DownloadStatus.NONE
            var lastPercent = 0
            override fun onComplete(
                totalBytes: Int, mDownloadItem: DownloadItem?, downloadUri: String?
            ) {
                if (lastStatus !== DownloadStatus.SUCCESSFUL) {
                    DeviceLogs.e("NightlyUpdateWorker", "Download Success ${item.uri}")
                    HyperLog.d("NightlyUpdateWorker", "Download Success ${item.uri}")
                    item.downloadStatus = DownloadStatus.SUCCESSFUL
                    onComplete?.invoke(totalBytes, mDownloadItem, downloadUri)
                    item.onComplete?.invoke(totalBytes, mDownloadItem, downloadUri)
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
                    DeviceLogs.e("NightlyUpdateWorker", "Download Pause ${item.uri}")
                    HyperLog.d("NightlyUpdateWorker", "Download Pause ${item.uri}")
                    item.downloadStatus = DownloadStatus.PAUSED
                    onPause?.invoke(percent, reason, totalBytes, downloadedBytes, mDownloadItem)
                    item.onPause?.invoke(
                        percent, reason, totalBytes, downloadedBytes, mDownloadItem
                    )
                }
                lastStatus = DownloadStatus.PAUSED
            }

            override fun onPending(
                percent: Int, totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?
            ) {
                if (lastStatus !== DownloadStatus.PENDING) {
                    DeviceLogs.e("NightlyUpdateWorker", "Download Pending ${item.uri}")
                    HyperLog.d("NightlyUpdateWorker", "Download Pending ${item.uri}")
                    item.downloadStatus = DownloadStatus.PENDING
                    onPending?.invoke(percent, totalBytes, downloadedBytes, mDownloadItem)
                    item.onPending?.invoke(percent, totalBytes, downloadedBytes, mDownloadItem)
                }
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
                    DeviceLogs.e("NightlyUpdateWorker", "Download Fail ${item.uri}")
                    HyperLog.d("NightlyUpdateWorker", "Download Fail ${item.uri}")
                    item.downloadStatus = DownloadStatus.FAILED
                    onFail?.invoke(percent, reason, totalBytes, downloadedBytes, mDownloadItem)
                    item.onFail?.invoke(percent, reason, totalBytes, downloadedBytes, mDownloadItem)
                }
                lastStatus = DownloadStatus.FAILED
            }

            override fun onCancel(
                totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?
            ) {
                if (lastStatus !== DownloadStatus.CANCELED) {
                    DeviceLogs.e("NightlyUpdateWorker", "Download Cancel ${item.uri}")
                    HyperLog.d("NightlyUpdateWorker", "Download Cancel ${item.uri}")
                    item.downloadStatus = DownloadStatus.CANCELED
                    onCancel?.invoke(totalBytes, downloadedBytes, mDownloadItem)
                    item.onCancel?.invoke(totalBytes, downloadedBytes, mDownloadItem)
                }
                lastStatus = DownloadStatus.CANCELED
            }

            override fun onRunning(
                percent: Int,
                totalBytes: Int,
                downloadedBytes: Int,
                downloadSpeed: Float,
                mDownloadItem: DownloadItem?
            ) {
                if (lastStatus !== DownloadStatus.RUNNING) {
                }
                if (percent > lastPercent) {
                    lastPercent = percent
                    DeviceLogs.e(
                        "NightlyUpdateWorker", "Download Running ${item.uri} per ${lastPercent}"
                    )
                    HyperLog.d(
                        "NightlyUpdateWorker", "Download Running ${item.uri} per ${lastPercent}"
                    )
                }
                lastStatus = DownloadStatus.RUNNING
                item.downloadStatus = DownloadStatus.RUNNING
                onRunning?.invoke(
                    percent, totalBytes, downloadedBytes, downloadSpeed, mDownloadItem
                )
                item.onRunning?.invoke(
                    percent, totalBytes, downloadedBytes, downloadSpeed, mDownloadItem
                )
            }
        }
    }
}