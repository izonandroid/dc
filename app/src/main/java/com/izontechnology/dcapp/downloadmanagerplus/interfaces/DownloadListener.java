package com.izontechnology.dcapp.downloadmanagerplus.interfaces;


import com.izontechnology.dcapp.downloadmanagerplus.enums.DownloadReason;
import com.izontechnology.dcapp.downloadmanagerplus.model.DownloadItem;

/**
 * Created by Siamak on 16/01/2017.
 */

public interface DownloadListener {

    void onComplete(int totalBytes, DownloadItem downloadInfo, String downloadUri);

    void onPause(int percent, DownloadReason reason, int totalBytes, int downloadedBytes, DownloadItem downloadInfo);

    void onPending(int percent, int totalBytes, int downloadedBytes, DownloadItem downloadInfo);

    void onFail(int percent, DownloadReason reason, int totalBytes, int downloadedBytes, DownloadItem downloadInfo);

    void onCancel(int totalBytes, int downloadedBytes, DownloadItem downloadInfo);

    void onRunning(int percent, int totalBytes, int downloadedBytes, float downloadSpeed, DownloadItem downloadInfo);

}
