package com.izontechnology.dcapp.utils.log;

import com.google.gson.annotations.Expose;

public class DeviceLogModel {

    @Expose(serialize = false, deserialize = false)
    private int id;

    private String deviceLog;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceLog() {
        return deviceLog;
    }

    public void setDeviceLog(String deviceLog) {
        this.deviceLog = deviceLog;
    }

    public DeviceLogModel(String deviceLog) {
        this.deviceLog = deviceLog;
    }

    public DeviceLogModel(int id, String deviceLog) {
        this.id = id;
        this.deviceLog = deviceLog;
    }
}