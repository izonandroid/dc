package com.izontechnology.dcapp.utils.log;

import android.text.TextUtils;

import java.util.List;

class DeviceLogList {
    private DeviceLogDataSource mDeviceLogDataSource;

    DeviceLogList(DeviceLogDataSource mDeviceLogDataSource) {
        this.mDeviceLogDataSource = mDeviceLogDataSource;
    }

    void addDeviceLog(String deviceLog) {
        if (TextUtils.isEmpty(deviceLog)) {
            return;
        }

        this.mDeviceLogDataSource.addDeviceLog(deviceLog);
    }

    void clearSavedDeviceLogs() {
        this.mDeviceLogDataSource.deleteAllDeviceLogs();
    }

    List<DeviceLogModel> getDeviceLogs(int batch) {
        return this.mDeviceLogDataSource.getDeviceLogs(batch);
    }

    void clearDeviceLogs(List<DeviceLogModel> pushedDeviceLogs) {
        if (pushedDeviceLogs == null || pushedDeviceLogs.isEmpty())
            return;

        this.mDeviceLogDataSource.deleteDeviceLog(pushedDeviceLogs);
    }

    long count() {
        return this.mDeviceLogDataSource.getDeviceLogCount();
    }

    int getDeviceLogBatchCount(){
        return this.mDeviceLogDataSource.getDeviceLogBatchCount();
    }

    void clearOldLogs(int expiryTimeInSeconds) {
        mDeviceLogDataSource.clearOldLogs(expiryTimeInSeconds);
    }
}