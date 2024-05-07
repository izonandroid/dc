package com.izontechnology.dcapp.downloadmanagerplus.interfaces;

import android.content.Context;
import android.content.Intent;


public interface DownloadNotificationListener {
    void onCompleted(Context context, Intent intent, long downloadId);

    void onFailed(Context context, Intent intent, long downloadId);

    void onClicked(Context context, Intent intent, long[] downloadIdList);
}
