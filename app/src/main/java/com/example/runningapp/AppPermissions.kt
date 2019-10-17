package com.example.runningapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap

const val LOCATION_REQUEST_CODE = 101

const val BODY_SENSOR_REQUEST_CODE = 100

const val CAMERA_REQUEST_CODE = 99

class AppPermissions(val context: Context?, val activity: Activity) {
    fun checkLocationPermission(map: GoogleMap) {

        val permission =
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= 23 && permission != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Permission to access the location is required for this app to get current location.")
                    .setTitle("Permission required")

                builder.setPositiveButton("OK") { _, _ ->
                    makeLocationRequest()
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                makeLocationRequest()
            }
        } else {
            map.isMyLocationEnabled = true

        }

    }

    fun checkBodySensorPermission() {
        val permission =
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.BODY_SENSORS)
        if (Build.VERSION.SDK_INT >= 23 && permission != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.BODY_SENSORS
                )
            ) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Permission to Body Sensor  is required for this app to measure Heart rate.")
                    .setTitle("Permission required")

                builder.setPositiveButton("OK") { _, _ ->
                    makeBodySensorRequest()
                }
                val dialog = builder.create()
                dialog.show()
            } else {
                makeBodySensorRequest()
            }
        }

    }

    fun checkCameraPermission() {
        val permission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= 23 && permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }

    }


    private fun makeLocationRequest() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_REQUEST_CODE
        )
    }

    private fun makeBodySensorRequest() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.BODY_SENSORS),
            BODY_SENSOR_REQUEST_CODE
        )
    }


}

