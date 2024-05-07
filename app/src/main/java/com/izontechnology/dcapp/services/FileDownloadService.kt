package com.izontechnology.dcapp.services

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.os.ResultReceiver
import android.util.Log
import com.izontechnology.dcapp.base.BaseApplication
import com.izontechnology.dcapp.utils.DeviceLogs
import dagger.hilt.android.AndroidEntryPoint
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@AndroidEntryPoint
class FileDownloadService : IntentService("") {
//    private var isPaused: Boolean = false
    private var isCancelled: Boolean = false
    override fun onHandleIntent(intent: Intent?) {
        val bundle = intent?.extras
        if (bundle == null || !bundle.containsKey(DOWNLOADER_RECEIVER)
            || !bundle.containsKey(DOWNLOAD_DETAILS)
        ) {
            return
        }
        val resultReceiver = bundle.getParcelable<ResultReceiver>(DOWNLOADER_RECEIVER)
        val downloadDetails = bundle.getParcelable<DownloadRequest>(DOWNLOAD_DETAILS)
        lateinit var connectivityReceiver: ConnectivityReceiver

        try {
            assert(downloadDetails != null)
            val localPath = downloadDetails?.localFilePath
            val url = URL(downloadDetails?.serverFilePath)
            val urlConnection = url.openConnection()
            var downloadedFile = File(localPath)
            var downloaded = 0L
            if (BaseApplication.isPaused) {
                if (downloadedFile.exists()) {
                    downloaded = downloadedFile.length()
                    urlConnection.setRequestProperty(
                        "Range",
                        "bytes=" + downloadedFile.length() + "-"
                    );
                }
            }
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect()
            val lengthOfFile = urlConnection.contentLength
            Log.d("FileDownloaderService", "Length of file: $lengthOfFile")
            downloadStarted(resultReceiver)
            val input: InputStream = BufferedInputStream(url.openStream())
            var output: OutputStream = FileOutputStream(localPath)
            if (downloaded > 0L){
                output = FileOutputStream(localPath)
            }else{
                output = FileOutputStream(localPath,true)
            }
            val data = ByteArray(1024)
            var total: Long = 0
            var count: Int
            val startTime = System.nanoTime()

            connectivityReceiver = ConnectivityReceiver()
            val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            registerReceiver(connectivityReceiver, intentFilter)

            while (input.read(data).also { count = it } != -1) {
                if (isCancelled) {
                    return
                }

                while (BaseApplication.isPaused) {
                    Thread.sleep(1000)
                }

                if (!BaseApplication.isPaused) {
                    total += count.toLong()
                    val progress = (total * 100 / lengthOfFile).toInt()
                    DeviceLogs.e("download Service", "progress $progress")

                    val elapsedTime = System.nanoTime() - startTime
                    DeviceLogs.e(
                        "download Service",
                        "elapsedTime $elapsedTime startTime $startTime"
                    )
                    val allTimeForDownloading: Long = (elapsedTime * lengthOfFile / total)
                    val remainingTime = ((allTimeForDownloading - elapsedTime) * 1e-9).toLong()
                    DeviceLogs.e("download Service", "remainingTime $remainingTime")

                    sendProgress(progress, remainingTime, resultReceiver)
                    output.write(data, 0, count)
//                Thread.sleep(1000)
                }
            }
            output.flush()
            output.close()
            input.close()
            if (downloadDetails?.isRequiresUnzip == true) {
                var unzipDestination = downloadDetails.unzipAtFilePath
                if (unzipDestination == null) {
                    val file = File(localPath)
                    unzipDestination = file.parentFile.absolutePath
                }
                unzip(localPath, unzipDestination)
            }
            downloadCompleted(resultReceiver)
            if (downloadDetails?.isDeleteZipAfterExtract == true) {
                val file = File(localPath)
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            downloadFailed(resultReceiver)
        }
    }

    fun sendProgress(progress: Int, remainingTimeInSeconds: Long, receiver: ResultReceiver?) {
        val progressBundle = Bundle()
        progressBundle.putInt(DOWNLOAD_PROGRESS, progress)
        progressBundle.putLong(DOWNLOAD_REMAIN_TIME, remainingTimeInSeconds)
        receiver?.send(STATUS_OK, progressBundle)
    }

    fun downloadStarted(resultReceiver: ResultReceiver?) {
        val progressBundle = Bundle()
        progressBundle.putBoolean(DOWNLOAD_STARTED, true)
        resultReceiver?.send(STATUS_OK, progressBundle)
    }

    fun downloadCompleted(resultReceiver: ResultReceiver?) {
        val progressBundle = Bundle()
        progressBundle.putBoolean(DOWNLOAD_COMPLETED, true)
        resultReceiver?.send(STATUS_OK, progressBundle)
    }

    fun downloadFailed(resultReceiver: ResultReceiver?) {
        val progressBundle = Bundle()
        progressBundle.putBoolean(DOWNLOAD_FAILED, true)
        resultReceiver?.send(STATUS_FAILED, progressBundle)
    }

    @Throws(Exception::class)
    private fun unzip(zipFilePath: String?, unzipAtLocation: String?) {
        val archive = File(zipFilePath)
        try {
            val zipfile = ZipFile(archive)
            val e: Enumeration<*> = zipfile.entries()
            while (e.hasMoreElements()) {
                val entry = e.nextElement() as ZipEntry
                unzipEntry(zipfile, entry, unzipAtLocation)
            }
        } catch (e: Exception) {
            Log.e("Unzip zip", "Unzip exception", e)
        }
    }

    @Throws(IOException::class)
    private fun unzipEntry(zipfile: ZipFile, entry: ZipEntry, outputDir: String?) {
        if (entry.isDirectory) {
            createDir(File(outputDir, entry.name))
            return
        }
        val outputFile = File(outputDir, entry.name)
        if (!outputFile.parentFile.exists()) {
            createDir(outputFile.parentFile)
        }
        Log.v("ZIP E", "Extracting: $entry")
        val zin = zipfile.getInputStream(entry)
        val inputStream = BufferedInputStream(zin)
        val outputStream = BufferedOutputStream(FileOutputStream(outputFile))
        try {

            //IOUtils.copy(inputStream, outputStream);
            try {
                var c = inputStream.read()
                while (c != -1) {
                    outputStream.write(c)
                    c = inputStream.read()
                }
            } finally {
                outputStream.close()
            }
        } finally {
            outputStream.close()
            inputStream.close()
        }
    }

    private fun createDir(dir: File) {
        if (dir.exists()) {
            return
        }
        Log.v("ZIP E", "Creating dir " + dir.name)
        if (!dir.mkdirs()) {
            throw RuntimeException("Can not create dir $dir")
        }
    }

    class FileDownloader private constructor(handler: Handler) : ResultReceiver(handler) {
        private var downloadDetails: DownloadRequest? = null
        private var onDownloadStatusListener: OnDownloadStatusListener? = null
        fun download(context: Context) {
            if (isOnline(context)) {
                val intent = Intent(context, FileDownloadService::class.java)
                intent.putExtra(DOWNLOADER_RECEIVER, this)
                intent.putExtra(DOWNLOAD_DETAILS, downloadDetails)
                context.startService(intent)
            }
        }

        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            super.onReceiveResult(resultCode, resultData)
            if (onDownloadStatusListener == null) {
                return
            }
            if (resultCode == STATUS_OK) {
                if (resultData.containsKey(DOWNLOAD_STARTED)
                    && resultData.getBoolean(DOWNLOAD_STARTED)
                ) {
                    onDownloadStatusListener?.onDownloadStarted()
                } else if (resultData.containsKey(DOWNLOAD_COMPLETED)
                    && resultData.getBoolean(DOWNLOAD_COMPLETED)
                ) {
                    onDownloadStatusListener?.onDownloadCompleted()
                } else if (resultData.containsKey(DOWNLOAD_PROGRESS)) {
                    val progress = resultData.getInt(DOWNLOAD_PROGRESS)
                    val remainTime = resultData.getLong(DOWNLOAD_REMAIN_TIME, -1)
                    onDownloadStatusListener?.onDownloadProgress(progress, remainTime)
                }
            } else if (resultCode == STATUS_FAILED) {
                onDownloadStatusListener?.onDownloadFailed()
            }
        }

        fun getDownloadDetails(): DownloadRequest? {
            return downloadDetails
        }

        fun setDownloadDetails(downloadDetails: DownloadRequest?) {
            this.downloadDetails = downloadDetails
        }

        fun getOnDownloadStatusListener(): OnDownloadStatusListener? {
            return onDownloadStatusListener
        }

        fun setOnDownloadStatusListener(onDownloadStatusListener: OnDownloadStatusListener?) {
            this.onDownloadStatusListener = onDownloadStatusListener
        }

        companion object {
            var instance:FileDownloader? = null
            fun getInstance(
                downloadDetails: DownloadRequest?,
                downloadStatusListener: OnDownloadStatusListener?
            ): FileDownloader? {
                if (instance == null) {
                    val handler = Handler(Looper.getMainLooper())
                    val fileDownloader = FileDownloader(handler)
                    fileDownloader.downloadDetails = downloadDetails
                    fileDownloader.onDownloadStatusListener = downloadStatusListener
                    instance = fileDownloader
                    return instance
                }else{
                    return instance
                }
            }
        }
    }
    fun cancelDownload() {
        isCancelled = true
    }

    interface OnDownloadStatusListener {
        fun onDownloadStarted()
        fun onDownloadCompleted()
        fun onDownloadFailed()
        fun onDownloadProgress(progress: Int, remainTime: Long)
    }

    inner class ConnectivityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val cm =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            if (networkInfo == null || !networkInfo.isConnected) {
                BaseApplication.INSTANCE.pauseDownload()
            } else {
                BaseApplication.INSTANCE.resumeDownload()
            }
        }
    }

    class DownloadRequest : Parcelable {
        var tag: String? = null
        var isRequiresUnzip: Boolean = false
        var serverFilePath: String?
        var localFilePath: String?
        var unzipAtFilePath: String? = null
        var isDeleteZipAfterExtract = true

        constructor(serverFilePath: String?, localPath: String?) {
            this.serverFilePath = serverFilePath
            localFilePath = localPath
            isRequiresUnzip = isRequiresUnzip
        }

        protected constructor(`in`: Parcel) {
            isRequiresUnzip = `in`.readByte().toInt() != 0x00
            serverFilePath = `in`.readString()
            localFilePath = `in`.readString()
            unzipAtFilePath = `in`.readString()
            isDeleteZipAfterExtract = `in`.readByte().toInt() != 0x00
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeByte((if (isRequiresUnzip) 0x01 else 0x00).toByte())
            dest.writeString(serverFilePath)
            dest.writeString(localFilePath)
            dest.writeString(unzipAtFilePath)
            dest.writeByte((if (isDeleteZipAfterExtract) 0x01 else 0x00).toByte())
        }

        companion object CREATOR : Parcelable.Creator<DownloadRequest?> {
            override fun createFromParcel(`in`: Parcel): DownloadRequest? {
                return DownloadRequest(`in`)
            }

            override fun newArray(size: Int): Array<DownloadRequest?> {
                return arrayOfNulls(size)
            }
        }

    }

    companion object {
        private const val STATUS_OK = 100
        private const val STATUS_FAILED = 200
        private const val DOWNLOADER_RECEIVER = "downloader_receiver"
        private const val DOWNLOAD_DETAILS = "download_details"
        private const val DOWNLOAD_STARTED = "download_started"
        private const val DOWNLOAD_FAILED = "download_failed"
        private const val DOWNLOAD_COMPLETED = "download_completed"
        private const val DOWNLOAD_PROGRESS = "download_progress"
        private const val DOWNLOAD_REMAIN_TIME = "download_remain_time"
        private fun isOnline(context: Context): Boolean {
            val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            return if (netInfo != null && netInfo.isConnectedOrConnecting
                && cm.activeNetworkInfo?.isAvailable == true
                && cm.activeNetworkInfo?.isConnected == true
            ) {
                true
            } else false
        }
    }
}