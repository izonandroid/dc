package com.izontechnology.dcapp.base.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import javax.inject.Inject

class Network @Inject constructor(private val context: Context) : NetworkConnectivity{

    override fun getNetworkInfo(): NetworkCapabilities? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.getNetworkCapabilities(cm.activeNetwork)
    }

    override fun isConnected(): Boolean {
        val info = getNetworkInfo()
        return info != null
    }
}


interface NetworkConnectivity {
    fun getNetworkInfo(): NetworkCapabilities?
    fun isConnected(): Boolean
}