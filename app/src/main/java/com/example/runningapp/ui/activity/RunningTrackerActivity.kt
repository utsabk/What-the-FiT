package com.example.runningapp.ui.activity


import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.runningapp.AppPermissions
import com.example.runningapp.LOCATION_REQUEST_CODE
import com.example.runningapp.R
import com.example.runningapp.WorkoutDetailsActivity
import com.example.runningapp.models.RouteSection
import com.example.runningapp.services.RunningTrackerService
import com.example.runningapp.ui.home.HomeFragment
import com.example.runningapp.ui.profile.ProfileFragment
import com.example.runningapp.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.layout_timer.*
import kotlin.math.roundToInt


class RunningTrackerActivity : AppCompatActivity(),
    OnMapReadyCallback,
    OnMyLocationClickListener,
    OnMyLocationButtonClickListener,
    AdapterView.OnItemSelectedListener {


    val TAG = "Tag"
    private lateinit var mMap: GoogleMap
    private lateinit var spinner: Spinner
    private lateinit var trafficCheckbox: CheckBox
    private lateinit var buildingsCheckbox: CheckBox
    private lateinit var indoorCheckbox: CheckBox

    private var sharedPref:SharedPreferences? = null


    companion object {
        const val REQUEST_CHECK_SETTINGS = 2

        private var timerState = TimerState.Idle

        private var measuredTimeInMillis = 0L
        private var distanceInMeters = 0.0
        private var measuredCalories = 0

        private var routeSections:List<RouteSection> = ArrayList()

        const val BROADCAST_ACTION_LOCATION =
            "com.example.runningapp.ui.activity.runningTrackerActivity.broadcastreceiverlocation"
        const val BROADCAST_ACTION_TIME =
            "com.example.runningapp.ui.activity.runningTrackerActivity.broadcastreceivertime"
        const val BROADCAST_ACTION_STOP_TIMER =
            "com.example.runningapp.ui.activity.runningTrackerActivity.broadcastreceiverstoptimer"
        const val TIME_DATA_KEY =
            "com.example.runningapp.ui.activity.runningTrackerActivity.timedatakey"
        const val ROUTE_SECTIONS_DATA_KEY =
            "com.example.runningapp.ui.activity.runningTrackerActivity.routesectionsdatakey"
        const val CALORIES_DATA_KEY =
            "com.example.runningapp.ui.activity.runningTrackerActivity.caloriesdatakey"
        const val MODE_DATA_KEY =
            "com.example.runningapp.ui.activity.runningTrackerActivity.modedatakey"
        const val STATUS_DATA_KEY =
            "com.example.runningapp.ui.activity.runningTrackerActivity.status"


    }

   // Receives and handles broadcast intents sent by Service class via sendBroadcast(Intent)
    private val runningTrackerBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                BROADCAST_ACTION_TIME -> {
                    measuredTimeInMillis =
                        intent.getLongExtra(RunningTrackerService.MILLIS_DATA_KEY, 0L)
                    if (measuredTimeInMillis == 0L) Log.w(
                        "broadcastreceiver",
                        "service returned time 0"
                    )
                    time.base = SystemClock.elapsedRealtime() - measuredTimeInMillis
                }
                BROADCAST_ACTION_LOCATION -> {
                    routeSections =
                        intent.getParcelableArrayListExtra(RunningTrackerService.ROUTE_SECTIONS_DATA_KEY)
                    context?.let {
                        fillMap()
                        distanceInMeters =
                            routeSections.sumByDouble { section -> section.distance.toDouble() }
                        tracked_distance.text = "%.2f".format(distanceInMeters / 1000)

                        val speed = speedCalc(distanceInMeters, measuredTimeInMillis.toDouble())
                        tracked_speed.text = String.format("%.2f", speed)


                        val weight =  sharedPref!!.getInt(PREFERENCE_WEIGHT, 65)
                        measuredCalories = (0.001033416853125 * weight.toDouble() * distanceInMeters).toInt()
                        calories_burned.text = measuredCalories.toString()
                    }
                }
                BROADCAST_ACTION_STOP_TIMER -> {
                    stopTimer()
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        sharedPref = getSharedPreferences(RUNNING_TRACKER_PREFERENCE_FILE_KEY,Context.MODE_PRIVATE)

        timerState = TimerState.values()[sharedPref!!.getInt(TIMER_STATE, 0)]


        when (timerState) {
            TimerState.Idle -> {
                // return the time since the system was booted, and include deep sleep
                time.base = SystemClock.elapsedRealtime()
            }
            TimerState.Running -> {
                time.base = SystemClock.elapsedRealtime() - measuredTimeInMillis
                tracked_distance.text = "%.2f".format(distanceInMeters / 1000)
                calories_burned.text = measuredCalories.toString()
                fillMap()

                go_fab_idle.hide()
                pause_fab_running.show()
            }
            TimerState.Paused -> {
                time.base = SystemClock.elapsedRealtime() - measuredTimeInMillis
                tracked_distance.text = "%.2f".format(distanceInMeters / 1000)
                fillMap()
                go_fab_idle.hide()
                fabs_paused.visibility = View.VISIBLE
            }
        }



        go_fab_idle.setOnClickListener {
            AppPermissions(this, this).checkLocationPermission(mMap)

            go_fab_idle.hide()
            pause_fab_running.show()
            timerState = TimerState.Running

            RunningTrackerService.stepCalcPaused = false

            val intentFilter = IntentFilter()
            intentFilter.addAction(BROADCAST_ACTION_LOCATION)
            intentFilter.addAction(BROADCAST_ACTION_TIME)
            intentFilter.addAction(BROADCAST_ACTION_STOP_TIMER)
            intentFilter.addAction(ProfileFragment.BROADCAST_ACTION_STEPS)
            this.registerReceiver(runningTrackerBroadcastReceiver, intentFilter)

            // Start the service from here
            ContextCompat.startForegroundService(this, Intent(this, RunningTrackerService::class.java))
        }
        pause_fab_running.setOnClickListener {
            RunningTrackerService.pauseService = true

            pause_fab_running.hide()
            fabs_paused.visibility = View.VISIBLE
            timerState = TimerState.Paused
        }

        go_fab_paused.setOnClickListener {
            RunningTrackerService.pauseService = false

            fabs_paused.visibility = View.GONE

            pause_fab_running.show()
            timerState = TimerState.Running
        }

        stop_fab_paused.setOnLongClickListener {
            stopTimer()
            return@setOnLongClickListener true
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(BROADCAST_ACTION_LOCATION)
        intentFilter.addAction(BROADCAST_ACTION_TIME)
        this.registerReceiver(runningTrackerBroadcastReceiver, intentFilter)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        try {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        } catch (ex: Resources.NotFoundException) {
            Log.e("Google Map", "Resources\$NotFoundException")
        }


        spinner = findViewById<Spinner>(R.id.layers_spinner).apply {
            adapter = ArrayAdapter.createFromResource(
                this@RunningTrackerActivity,
                R.array.layers_array,
                android.R.layout.simple_spinner_item
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            onItemSelectedListener = this@RunningTrackerActivity
        }

        buildingsCheckbox = findViewById(R.id.buildings)
        indoorCheckbox = findViewById(R.id.indoor)
        trafficCheckbox = findViewById(R.id.traffic)


    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        updateMapType()

        // Must deal with the location checkbox separately as must check that
        // location permission have been granted before enabling the 'My Location' layer.
        AppPermissions(this, this).checkLocationPermission(mMap)


        // check the state of all checkboxes and update the map accordingly
        with(mMap) {
            isTrafficEnabled = trafficCheckbox.isChecked
            isBuildingsEnabled = buildingsCheckbox.isChecked
            isIndoorEnabled = indoorCheckbox.isChecked
        }


        // attach a listener to each checkbox
        trafficCheckbox.setOnClickListener {
            mMap.isTrafficEnabled = trafficCheckbox.isChecked
        }

        buildingsCheckbox.setOnClickListener {
            mMap.isBuildingsEnabled = buildingsCheckbox.isChecked
        }

        indoorCheckbox.setOnClickListener {
            mMap.isIndoorEnabled = indoorCheckbox.isChecked
        }

        mMap.uiSettings.isZoomControlsEnabled = true


        mMap.setOnMyLocationButtonClickListener(this)

        mMap.setOnMyLocationClickListener(this)

    }

    private fun updateMapType() {
        // This can also be called by the Android framework in onCreate() at which
        // point map may not be ready yet.
        if (!::mMap.isInitialized) return

        //Change the type of the map depending on the currently selected item in the spinner
        mMap.mapType = when (spinner.selectedItem) {
            getString(R.string.normal) -> GoogleMap.MAP_TYPE_NORMAL
            getString(R.string.hybrid) -> GoogleMap.MAP_TYPE_HYBRID
            getString(R.string.satellite) -> GoogleMap.MAP_TYPE_SATELLITE
            getString(R.string.terrain) -> GoogleMap.MAP_TYPE_TERRAIN
            getString(R.string.none_map) -> GoogleMap.MAP_TYPE_NONE
            else -> {
                mMap.mapType // do not change map type
                Log.e(TAG, "Error tool_bar_menu layer with name ${spinner.selectedItem}")
            }
        }
    }


    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    /* Called as part of the AdapterView.OnItemSelectedListener */
    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        updateMapType()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        //Do Nothing
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    // This function is triggered when user chooses an option from permission dialogue
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user")
                    mMap.isMyLocationEnabled = false
                    finish()
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                    mMap.isMyLocationEnabled = true
                }
            }
        }

    }


    // start the update request if it has a RESULT_OK result for a REQUEST_CHECK_SETTINGS request
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                go_fab_idle.hide()
                pause_fab_running.show()
                timerState = TimerState.Running
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, RunningTrackerService::class.java)
                )
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.ic_user_location)
            )
        )
        mMap.addMarker(markerOptions)
    }


    private fun fillMap() {
        try {
            mMap.clear()
        } catch (ex: UninitializedPropertyAccessException) {
            Log.w("Google map", "fillMap() invoked with uninitialized googleMap")
            return
        }
        // Iterate through all the routeSections
        for (i in  routeSections.indices) {
            mMap.addPolyline(
                PolylineOptions().add(
                    routeSections[i].beginning.toLatLng(),
                    routeSections[i].end.toLatLng()
                ).color(ContextCompat.getColor(this, R.color.fab_color_stop)).width(5f)
            )
        }
        if (routeSections.isNotEmpty()) {
            // Marker at beginning of the Journey
            mMap.addMarker(MarkerOptions().position(routeSections[0].beginning.toLatLng()))

           //Marker at the last of the Journey
            placeMarkerOnMap(routeSections.last().end.toLatLng())
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    routeSections.last().end.toLatLng(),
                    18f
                )
            )
        }
    }


    private fun stopTimer() {
        timerState = TimerState.Idle

        // Stop the service from running
        this.stopService(Intent(this, RunningTrackerService::class.java))
        RunningTrackerService.stepCalcPaused = true
        this.unregisterReceiver(runningTrackerBroadcastReceiver)
        @Suppress("UNCHECKED_CAST")
        val localRouteSections = routeSections as ArrayList<Parcelable>
        routeSections = ArrayList()
        mMap.clear()

        // return the time since the system was booted, and include deep sleep
        time.base = SystemClock.elapsedRealtime()
        tracked_speed.text = getString(R.string.zero_colon)
        tracked_distance.text = getString(R.string.zero_dot)
        calories_burned.text = getString(R.string.zero)

        fabs_paused.visibility = View.GONE
        pause_fab_running.hide()
        go_fab_idle.show()

        //Save steps to SharePreferences
        val sharedPreference = PrefUtils(this)
        sharedPreference.saveInt(PREFERENCE_STEPS, ProfileFragment.totalSteps)

        if (localRouteSections.size < 1) return
        val intent = Intent(this, WorkoutDetailsActivity::class.java)
            .putExtra(TIME_DATA_KEY, measuredTimeInMillis)
            .putParcelableArrayListExtra(ROUTE_SECTIONS_DATA_KEY, localRouteSections)
            .putExtra(CALORIES_DATA_KEY, measuredCalories)
            .putExtra(MODE_DATA_KEY, false)
        startActivity(intent)



    }

    private fun speedCalc(distanceMeter: Double, timeMilliSec: Double): Double {
        return((distanceMeter * 360000.0) / timeMilliSec.roundToInt())/100
       // ((distanceMeter * 360000.0) / timeMilliSec.toDouble()).roundToInt() / 100.0

    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            this.unregisterReceiver(runningTrackerBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
        }
        with(sharedPref!!.edit()) {
            putInt(TIMER_STATE, timerState.ordinal)
            commit()
        }
    }


    enum class TimerState { Idle, Running, Paused }



}
