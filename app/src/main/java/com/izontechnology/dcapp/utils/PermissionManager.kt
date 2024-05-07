package com.izontechnology.dcapp.utils

import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(
    private val activity: AppCompatActivity
) {
    private var permissionLauncher: ActivityResultLauncher<String>? = null

    init {
        initializePermissionLauncher()
    }

    private fun initializePermissionLauncher() {
        permissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted
                onPermissionResult(true)
            } else {
                // Permission denied
                onPermissionResult(false)
            }
        }

    }

    fun permissionStatus(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(permission: String, rationale: String) {
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            onPermissionResult(true)
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showPermissionRationale(permission,rationale) {
                    // Request the permission using the launcher
                    permissionLauncher?.launch(permission)
                }
            }else{
                permissionLauncher?.launch(permission)
            }
        }
    }

    private fun onPermissionResult(isGranted: Boolean) {
        // Handle the result of the permission request here
    }

    private fun showPermissionRationale(permission: String,rationaleMessage: String, callback: (isGranted: Boolean) -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage(rationaleMessage)
            .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                // Request the permission
                permissionLauncher?.launch(permission)
            }
            .setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
                // Permission denied, notify the callback
                callback(false)
            }
            .setOnCancelListener {
                // Permission denied, notify the callback
                callback(false)
            }
            .show()
    }

}
