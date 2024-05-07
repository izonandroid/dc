package com.izontechnology.dcapp.utils.wifi.listener;


//This interface is used for wifi connection successful or not
public interface OnWifiConnectListener {

    void onWiFiConnectLog(String log);

    void onWiFiConnectSuccess(String SSID);

    void onWiFiConnectFailure(String SSID);

    void onWiFiDisconnectSuccess(String SSID);
}
