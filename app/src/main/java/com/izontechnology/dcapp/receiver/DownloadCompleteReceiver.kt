package com.izontechnology.dcapp.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.izontechnology.dcapp.base.common.SharedPrefs
import com.izontechnology.dcapp.utils.DeviceLogs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DownloadCompleteReceiver : BroadcastReceiver() {
    @Inject
    lateinit var pref:SharedPrefs
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.getAction() != null) {
            when (intent.getAction()) {
                DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                    DeviceLogs.e("download complete","hdsfhghrkgkshg ${pref.getDownloadId()}")
                    if (intent.hasExtra(DownloadManager.EXTRA_DOWNLOAD_ID)) {
                        DeviceLogs.e("download complete","hdsfhghrkgkshg ${intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1)}")
                        if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1) == pref.getDownloadId()){
                            pref.setDownloadId(-1)
                            DeviceLogs.e("download complete","hdsfhghrkgkshg ${pref.getDownloadId()}")
                        }
                    }
                }
            }
        }
    }
}