package com.izontechnology.dcapp.utils.log;

import java.util.List;



interface DeviceLogDataSource {
    long getDeviceLogCount();

    void addDeviceLog(String deviceLog);

    void deleteDeviceLog(List<DeviceLogModel> deviceLogList);

    void deleteAllDeviceLogs();

    List<DeviceLogModel> getDeviceLogs(int batch);

    int getDeviceLogBatchCount();

    void clearOldLogs(int expiryTimeInSeconds);
}