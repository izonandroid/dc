package com.izontechnology.dcapp.downloadmanagerplus

import com.izontechnology.dcapp.data.response.MediaDownloadResponseItem
import com.izontechnology.dcapp.data.response.NetworkType
import com.izontechnology.dcapp.downloadmanagerplus.enums.DownloadReason
import com.izontechnology.dcapp.downloadmanagerplus.enums.Storage
import com.izontechnology.dcapp.downloadmanagerplus.interfaces.DownloadListener
import com.izontechnology.dcapp.downloadmanagerplus.model.DownloadItem


class FileItem : DownloadItem {
    var reasonMessage: String? = null
    var listener: DownloadListener? = null
    var fileSize = -1
    var fileName: String? = null
    var mediaItem: MediaDownloadResponseItem? = null
    var packageName: String = ""
    var version: String? = ""

    //    var allowNetwork:Int? = DownloadManager.Request.NETWORK_WIFI
    var networkType: NetworkType? = NetworkType()
    var destinationDir: String? = Storage.DIRECTORY_DOWNLOADS
    var destinationSubDir: String? = ""
    var forceDownload = false
    var onComplete: ((totalBytes: Int, mDownloadItem: DownloadItem?, downloadUri: String?) -> Unit)? =
        null
    var onPause: ((percent: Int, reason: DownloadReason, totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?) -> Unit)? =
        null
    var onPending: ((percent: Int, totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?) -> Unit)? =
        null
    var onFail: ((percent: Int, reason: DownloadReason, totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?) -> Unit)? =
        null
    var onCancel: ((totalBytes: Int, downloadedBytes: Int, mDownloadItem: DownloadItem?) -> Unit)? =
        null
    var onRunning: ((percent: Int, totalBytes: Int, downloadedBytes: Int, downloadSpeed: Float, mDownloadItem: DownloadItem?) -> Unit)? =
        null
    var onAlreadyDownloaded: (() -> Unit)? = null

    constructor() : super()
    constructor(downloadItem: DownloadItem?) : super(downloadItem)
}