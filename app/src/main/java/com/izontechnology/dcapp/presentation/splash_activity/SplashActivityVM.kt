package com.izontechnology.dcapp.presentation.splash_activity

import android.content.ContentResolver
import android.content.Context
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import com.izontechnology.dcapp.base.BaseViewModel
import com.izontechnology.dcapp.domain.repository.deviceInfo.DeviceInfoRepository
import com.izontechnology.dcapp.utils.HUNDRED
import com.izontechnology.dcapp.utils.TENTHOUSAND
import com.izontechnology.dcapp.utils.THARTY_THOUSAND
import com.izontechnology.dcapp.utils.WidgetsViewModel
import com.izontechnology.dcapp.utils.wifi.listener.OnWifiConnectListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashActivityVM @Inject constructor() : BaseViewModel() {
   }