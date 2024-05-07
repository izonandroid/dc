package com.izontechnology.dcapp.base

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.izontechnology.dcapp.R
import com.izontechnology.dcapp.base.common.NetworkConnectivity
import com.izontechnology.dcapp.utils.ResourceUtils
import com.izontechnology.dcapp.utils.SingleEvent
import com.izontechnology.dcapp.utils.wifi.WiFiManager
import javax.inject.Inject

open class BaseViewModel : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()
    val loadingMessage = MutableLiveData<String>(ResourceUtils.getString(R.string.loading))

    @Inject
    lateinit var wifiManager: WiFiManager

    @Inject
    lateinit var network: NetworkConnectivity

    val isWifiEnabled = MutableLiveData<Boolean>()

    var savedInstanceState: Bundle? = null

    val showSnackBarPrivate = MutableLiveData<SingleEvent<Any>>()
    val showSnackBar: LiveData<SingleEvent<Any>> get() = showSnackBarPrivate

}