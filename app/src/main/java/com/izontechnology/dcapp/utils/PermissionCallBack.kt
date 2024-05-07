package com.izontechnology.dcapp.utils

interface PermissionCallBack {
    fun permissionGranted()
    fun permissionDenied()
    /**
     * Callback on permission "Never show again" checked and denied
     * */
    fun onPermissionDisabled()
}