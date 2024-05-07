package com.izontechnology.dcapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat

class GPSTracker(private val mContext: Context) : LocationListener {
    var isGPSEnabled = false
    var isNetworkEnabled = false
    var canGetLocation = false
    var mLocation: Location? = null
    var mLatitude = 0.0
    var mLongitude = 0.0
    protected var locationManager: LocationManager? = null
    private val m_Location: Location?

    init {
        m_Location = getLocation()
        println("location Latitude:" + m_Location?.latitude)
        println("location Longitude:" + m_Location?.longitude)
        println("getLocation():" + getLocation())
    }

    fun getLocation(): Location? {
        try {
            locationManager = mContext
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
            isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                canGetLocation = true
                if (ActivityCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (isGPSEnabled) {
                        if (mLocation == null) {
                            locationManager?.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                            )
                            Log.d("GPS", "GPS Enabled")
                            if (locationManager != null) {
                                mLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                if (mLocation != null) {
                                    mLatitude = mLocation?.latitude?:0.0
                                    mLongitude = mLocation?.longitude?:0.0
                                }
                            }
                        }
                    }
                    if (isNetworkEnabled) {
                        locationManager?.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                        )
                        Log.d("Network", "Network Enabled")
                        if (locationManager != null) {
                            mLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            if (mLocation != null) {
                                mLatitude = mLocation?.latitude?:0.0
                                mLongitude = mLocation?.longitude?:0.0
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mLocation
    }

    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager?.removeUpdates(this@GPSTracker)
        }
    }

    fun getLatitude(): Double {
        if (mLocation != null) {
            mLatitude = mLocation?.latitude?:0.0
        }
        return mLatitude
    }

    fun getLongitude(): Double {
        if (mLocation != null) {
            mLongitude = mLocation?.longitude?:0.0
        }
        return mLongitude
    }

    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    override fun onLocationChanged(arg0: Location) {
        mLocation = arg0
    }

    override fun onProviderDisabled(arg0: String) {
        // TODO Auto-generated method stub
    }

    override fun onProviderEnabled(arg0: String) {
        // TODO Auto-generated method stub
    }

    override fun onStatusChanged(arg0: String, arg1: Int, arg2: Bundle) {
        // TODO Auto-generated method stub
    }

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1 // 1 minute
                ).toLong()
    }
}