package com.example.finflow.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    private const val STORAGE_PERMISSION_CODE = 101

    fun checkStoragePermissions(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val writePermission = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (!writePermission) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    fun handlePermissionResult(requestCode: Int, grantResults: IntArray): Boolean {
        return when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            }
            else -> false
        }
    }
}