package com.izontechnology.dcapp.utils.wifi.listener;

import android.net.wifi.ScanResult;

import java.util.List;

//This interface is used scan wifi result
public interface OnWifiScanResultsListener {

    void onScanResults(List<ScanResult> scanResults);
}
