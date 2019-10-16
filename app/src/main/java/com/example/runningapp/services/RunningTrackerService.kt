package com.example.runningapp.services

/* A component that can perform long-running operations in the background,
 and it doesn't provide a user interface*/

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.runningapp.App.Companion.CHANNEL_ID
import com.example.runningapp.MainActivity
import com.example.runningapp.R
import com.example.runningapp.listener.StepListener
import com.example.runningapp.models.RouteSection
import com.example.runningapp.ui.activity.RunningTrackerActivity
import com.example.runningapp.ui.home.HomeFragment
import com.example.runningapp.utils.StepDetector
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class RunningTrackerService : Service(), SensorEventListener, StepListener {
    private lateinit var context: Context

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    private var locations: MutableList<RouteSection> = ArrayList()


    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private var locationUpdateState = false
    private var currentTimeInMillis = 0L


    private var simpleStepDetector: StepDetector? = null
    private var sensorManager: SensorManager? = null
    private var numSteps: Int = 0

    private var lastSteps: Int = 0


    companion object {
        private var timer = Timer()
        var pauseService = false

        var stepCalcPaused = false

        // We use it on Notification start, and to cancel it.
        const val NOTIFICATION_ID = 1

        const val MILLIS_DATA_KEY = "com.example.runningapp.trackactivityservice.millisdatakey"
        const val ROUTE_SECTIONS_DATA_KEY =
            "com.example.runningapp.trackactivityservice.routesectionsdatakey"
        const val RESOLUTION_DATA_KEY =
            "com.example.runningapp.trackactivityservice.resolutiondatakey"

        const val STEPS_DATA_KEY = "com.example.runningapp.services.stepcounterservice.stepsdatakey"

    }


    override fun onBind(p0: Intent?): IBinder? {

        return null
    }

    // Called by the system when the service is first created.
    override fun onCreate() {
        super.onCreate()
        context = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        simpleStepDetector = StepDetector()
        simpleStepDetector!!.registerListener(this)
        sensorManager!!.registerListener(
            this,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST
        )


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
                if (lastLocation != null && numSteps > lastSteps) {
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
                    lastLocation = locationResult.lastLocation
                }
                // lastSteps is only updated to numSteps once they're added them to list(locations)
                // For that reason lastSteps and numSteps aren't same
                lastSteps = numSteps

                // When no steps are taken, last location is current location
                if (numSteps == 0){
                    lastLocation = locationResult.lastLocation
                }

                val intent = Intent()
                intent.action = RunningTrackerActivity.BROADCAST_ACTION_LOCATION
                intent.putParcelableArrayListExtra(
                    ROUTE_SECTIONS_DATA_KEY,
                    locations as ArrayList<out Parcelable>
                )
                //all receivers matching this Intent will receive the broadcast
                sendBroadcast(intent)

            }

        }
        startService()
    }

    // Called by the system to notify a Service that it is no longer used and is being removed.
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


        val notificationIntent = Intent(this, RunningTrackerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(currentTimeInMillis.toString())
            .setSmallIcon(R.drawable.ic_play_arrow)
            .setContentIntent(pendingIntent)
            .build()

        // To request that your service run in the foreground
        startForeground(NOTIFICATION_ID, notification)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // used for services that should only remain running while processing any commands sent to them
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

                val notificationIntent = Intent(context, RunningTrackerActivity::class.java)
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


                // Set the info for the views that show in the notification panel.
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


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.d("Tag", "onAccuracyChanged called")

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector!!.updateAccelerometer(
                event.timestamp,
                event.values[0],
                event.values[1],
                event.values[2]
            )
        }
    }

    override fun step(timeNs: Long) {
        if (!stepCalcPaused) {

            numSteps++

            val intent = Intent()
            intent.action = HomeFragment.BROADCAST_ACTION_STEPS
            intent.putExtra(STEPS_DATA_KEY, numSteps)
            sendBroadcast(intent)
        }




    }
}