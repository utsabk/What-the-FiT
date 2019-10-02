package com.example.runningapp.services

/* An application's facility that tells the system about something
it wants to be doing in the background (even when the user is not directly
interacting with the application)*/

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.runningapp.App.Companion.CHANNEL_ID
import com.example.runningapp.MainActivity
import com.example.runningapp.R
import com.example.runningapp.models.RouteSection
import com.example.runningapp.ui.activity.RunningTrackerActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class RunningTrackerService : Service() {

    private lateinit var context: Context

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    private var locations: MutableList<RouteSection> = ArrayList()


    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private var locationUpdateState = false
    private var currentTimeInMillis = 0L


    companion object {
        private var timer = Timer()
        var pauseService = false

        const val NOTIFICATION_ID = 1
        const val MILLIS_DATA_KEY = "com.example.runningapp.trackactivityservice.millisdatakey"
        const val ROUTE_SECTIONS_DATA_KEY =
            "com.example.runningapp.trackactivityservice.routesectionsdatakey"
        const val RESOLUTION_DATA_KEY =
            "com.example.runningapp.trackactivityservice.resolutiondatakey"
    }


    override fun onBind(p0: Intent?): IBinder? {

        return null
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // update lastLocation with the new location
        //  And update the map with the new location coordinates
        locationCallback = object : LocationCallback() {

            // Called when device location information is available
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                if (locationResult == null) return
                if (pauseService) {
                    lastLocation = null
                    return
                }

                if (lastLocation != null) {
                    locations.add(
                        RouteSection(
                            com.example.runningapp.models.Location(
                                lastLocation!!.latitude,
                                lastLocation!!.longitude
                            ),
                            com.example.runningapp.models.Location(
                                locationResult.lastLocation.latitude,
                                locationResult.lastLocation.longitude
                            )
                        )
                    )
                }

                Log.d("Tag", "$locations")

                lastLocation = locationResult.lastLocation

                val intent = Intent()
                intent.action = RunningTrackerActivity.BROADCAST_ACTION_LOCATION
                intent.putParcelableArrayListExtra(
                    ROUTE_SECTIONS_DATA_KEY,
                    locations as ArrayList<out Parcelable>
                )
                sendBroadcast(intent)

            }

        }
        startService()
    }


    override fun onDestroy() {
        super.onDestroy()

        pauseService = false
        currentTimeInMillis = 0
        timer.cancel()
        timer = Timer()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Toast.makeText(
            this,
            "Timer service stopped. Locations: ".plus(locations.size),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun startService() {
        timer.scheduleAtFixedRate(UpdateTimeTask(), 10, 10)
        timer.scheduleAtFixedRate(UpdateNotificationTask(), 1000, 1000)
        timer.scheduleAtFixedRate(NotifyTimer(), 1000, 500)
        Toast.makeText(this, "Timer service started", Toast.LENGTH_SHORT).show()


        createLocationRequest()


        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(currentTimeInMillis.toString())
            .setSmallIcon(R.drawable.ic_play_arrow)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


        // check the state of the user’s location settings
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        // create a settings client and a task to check location settings
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        // On Success ready to initiate a location request
        task.addOnSuccessListener {
            locationUpdateState = true
            startUpdatingLocation()
        }


        //On Failure show a dialog box to user for asking user’s location settings turned on
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {


                val pendingIntent: PendingIntent = exception.resolution
                client.applicationContext.startActivity(
                    Intent(
                        client.applicationContext,
                        MainActivity::class.java
                    ).putExtra(
                        RESOLUTION_DATA_KEY,
                        pendingIntent
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                val intent = Intent()
                intent.action = RunningTrackerActivity.BROADCAST_ACTION_STOP_TIMER
                sendBroadcast(intent)
            }
        }
    }

    private fun startUpdatingLocation() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private inner class UpdateTimeTask : TimerTask() {
        override fun run() {
            if (!pauseService) currentTimeInMillis += 10
        }
    }

    private inner class NotifyTimer : TimerTask() {
        override fun run() {
            val intent = Intent()
            intent.action = RunningTrackerActivity.BROADCAST_ACTION_TIME
            intent.putExtra(MILLIS_DATA_KEY, currentTimeInMillis)
            sendBroadcast(intent)
        }
    }

    private inner class UpdateNotificationTask : TimerTask() {
        override fun run() {
            if (!pauseService) {

                val notificationIntent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
                val timeFormatted =
                    if (currentTimeInMillis >= 3600000)
                        String.format(
                            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(currentTimeInMillis),
                            TimeUnit.MILLISECONDS.toMinutes(currentTimeInMillis) % TimeUnit.HOURS.toMinutes(
                                1
                            ),
                            TimeUnit.MILLISECONDS.toSeconds(currentTimeInMillis) % TimeUnit.MINUTES.toSeconds(
                                1
                            )
                        )
                    else
                        String.format(
                            "%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(currentTimeInMillis) % TimeUnit.HOURS.toMinutes(
                                1
                            ),
                            TimeUnit.MILLISECONDS.toSeconds(currentTimeInMillis) % TimeUnit.MINUTES.toSeconds(
                                1
                            )
                        )

                val distance =
                    (locations.sumByDouble { section -> section.distance.toDouble() } * 100.0).roundToInt() / 100.0


                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(timeFormatted.plus(" ").plus(distance.toString()))
                    .setSmallIcon(R.drawable.ic_play_arrow)
                    .setContentIntent(pendingIntent)
                    .build()

                val mNotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.notify(NOTIFICATION_ID, notification)
            }

        }


    }
}