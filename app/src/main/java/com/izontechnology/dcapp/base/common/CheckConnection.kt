package com.izontechnology.dcapp.base.common

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import androidx.lifecycle.LiveData

class CheckConnection(private val cm: ConnectivityManager) : LiveData<Boolean>() {

    constructor(application: Application) : this(
        application.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
    )

    private val networkCallback =object : ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: android.net.Network) {
            super.onAvailable(network)
            postValue(true)
        }

        override fun onLost(network: android.net.Network) {
            super.onLost(network)
            postValue(false)
        }
    }

    override fun onActive() {
        super.onActive()
        val request= NetworkRequest.Builder()
        cm.registerNetworkCallback(request.build(),networkCallback)
    }

    override fun onInactive() {
        super.onInactive()
        cm.unregisterNetworkCallback(networkCallback)
    }

}