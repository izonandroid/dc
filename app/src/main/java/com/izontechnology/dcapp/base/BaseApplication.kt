package com.izontechnology.dcapp.base

import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.services.ForegroundService
import com.izontechnology.dcapp.utils.DOWNLOAD_DIR_PATH
import com.izontechnology.dcapp.utils.DeviceLogs
import com.izontechnology.dcapp.utils.log.HyperLog
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import javax.inject.Inject


@HiltAndroidApp
class BaseApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        lateinit var INSTANCE: BaseApplication
        var isPaused = false
        var activity: Activity? = null
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        HyperLog.initialize(this)
        try {
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = SharedPrefs(this).getDownloadId()
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status != DownloadManager.STATUS_RUNNING) {
                    SharedPrefs(this).setDownloadId(-1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        startService(Intent(this, ForegroundService::class.java).apply { setAction("start") })
    }

    val destinationFolder: File
        get() {
            val parent = DOWNLOAD_DIR_PATH
            val destinationFolder = File(parent, "MyApp")
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs()
                destinationFolder.mkdir()
            }
            return destinationFolder
        }

    override fun onTerminate() {
        DeviceLogs.e("onterminate call", "onterminate call")
        super.onTerminate()
        stopService(Intent(this, ForegroundService::class.java).apply { setAction("stop") })
    }

    fun pauseDownload() {
        isPaused = true
    }

    fun resumeDownload() {
        isPaused = false
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}