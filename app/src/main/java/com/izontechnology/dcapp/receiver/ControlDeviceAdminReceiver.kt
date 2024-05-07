package com.izontechnology.dcapp.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.UserManager
import android.provider.Settings
import android.widget.Toast
import com.izontechnology.dcapp.presentation.host_activity.HostActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ControlDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        showToast(context, "Device admin privileges enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        showToast(context, "Device admin privileges disabled")
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}