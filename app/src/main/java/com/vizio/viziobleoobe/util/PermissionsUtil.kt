package com.vizio.viziobleoobe.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat

object PermissionsUtil {
    fun getRequiredPermissions(): Array<String> {
        Log.d("PermissionsUtil", "Getting required permissions")
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            else -> arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }
    fun hasPermissions(context: Context): Boolean {
        Log.d("PermissionsUtil", "Checking permissions")
        return getRequiredPermissions().all { permission ->
            ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    fun requestPermissions(activity: Activity, launcher: ActivityResultLauncher<Array<String>>) {
        Log.d("PermissionsUtil", "Requesting permissions")
        launcher.launch(getRequiredPermissions())
    }
}