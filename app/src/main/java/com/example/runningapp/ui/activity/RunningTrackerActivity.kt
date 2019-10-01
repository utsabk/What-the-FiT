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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.runningapp.AppPermissions
import com.example.runningapp.LOCATION_REQUEST_CODE
import com.example.runningapp.R
import com.example.runningapp.models.RouteSection
import com.example.runningapp.services.RunningTrackerService
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
import kotlinx.android.synthetic.main.timer_distance.*
import kotlin.collections.ArrayList


class RunningTrackerActivity : AppCompatActivity(),
    OnMapReadyCallback,
    OnMyLocationClickListener,
    OnMyLocationButtonClickListener,
    AdapterView.OnItemSelectedListener {


    val TAG = "Tag"
    private lateinit var mMap: GoogleMap
    private lateinit var spinner: Spinner
    private lateinit var trafficCheckbox: CheckBox
    private lateinit var myLocationCheckbox: CheckBox
    private lateinit var buildingsCheckbox: CheckBox
    private lateinit var indoorCheckbox: CheckBox


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val REQUEST_CHECK_SETTINGS = 2


        private var timerState = TimerState.Idle

        private var measuredTimeInMillis = 0L
        private var distanceInMeters = 0.0
        private var measuredCalories = 0

        private var routeSections = arrayListOf<RouteSection>()

        const val BROADCAST_ACTION_LOCATION =
            "com.example.runningapp.ui.activity.runningTrackerActivity.broadcastreceiverlocation"
        const val BROADCAST_ACTION_TIME =
            "com.example.runningapp.ui.activity.runningTrackerActivity.broadcastreceivertime"
        const val BROADCAST_ACTION_STOP_TIMER =
            "com.example.runningapp.ui.activity.runningTrackerActivity.broadcastreceiverstoptimer"
    }


    private val runningTrackerBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                BROADCAST_ACTION_TIME -> {
                    measuredTimeInMillis =
                        intent.getLongExtra(RunningTrackerService.MILLIS_DATA_KEY, 0L)
                    Log.d("Tag","TimeINMills:---$measuredTimeInMillis")
                    if (measuredTimeInMillis == 0L) Log.w("broadcastreceiver", "service returned time 0")
                    time.base = SystemClock.elapsedRealtime() - measuredTimeInMillis
                }
                BROADCAST_ACTION_LOCATION -> {
                    if (this@RunningTrackerActivity == null) return
                    routeSections =
                        intent.getParcelableArrayListExtra(RunningTrackerService.ROUTE_SECTIONS_DATA_KEY)
                    context?.let {
                        fillMap()
                        distanceInMeters =
                            routeSections.sumByDouble { section -> section.distance.toDouble() }

                        Log.d("Tag","DistanceInmeters:---$distanceInMeters")

                        tracked_distance.text = "%.2f".format(distanceInMeters/1000)

                        val speed = speedCalc(distanceInMeters , measuredTimeInMillis)
                        tracked_speed.text = String.format("%.2f",speed)

//                        if (sharedPref == null) return
//                        val weight = sharedPref!!.getInt(getString(R.string.preference_weight), 62)
//                        measuredCalories = (0.001033416853125 * weight.toDouble() * distanceInMeters).toInt()
//                        metrics_calories.text = measuredCalories.toString()
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

        when (timerState) {
            TimerState.Idle -> {
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
            AppPermissions(this, this, mMap)

            go_fab_idle.hide()
            pause_fab_running.show()
            timerState = TimerState.Running

            val intentFilter = IntentFilter()
            intentFilter.addAction(BROADCAST_ACTION_LOCATION)
            intentFilter.addAction(BROADCAST_ACTION_TIME)
            intentFilter.addAction(BROADCAST_ACTION_STOP_TIMER)
            this.registerReceiver(runningTrackerBroadcastReceiver, intentFilter)
            ContextCompat.startForegroundService(
                this,
                Intent(this, RunningTrackerService::class.java)
            )
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

        myLocationCheckbox = findViewById(R.id.my_location)
        buildingsCheckbox = findViewById(R.id.buildings)
        indoorCheckbox = findViewById(R.id.indoor)
        trafficCheckbox = findViewById(R.id.traffic)


    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        updateMapType()

        // check the state of all checkboxes and update the map accordingly
        with(mMap) {
            isTrafficEnabled = trafficCheckbox.isChecked
            isBuildingsEnabled = buildingsCheckbox.isChecked
            isIndoorEnabled = indoorCheckbox.isChecked
        }

        // Must deal with the location checkbox separately as must check that
        // location permission have been granted before enabling the 'My Location' layer.
        if (myLocationCheckbox.isChecked) {
            AppPermissions(this, this, mMap).checkPermission()
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

        // if this box is checked, must check for permission before enabling the My Location layer
        myLocationCheckbox.setOnClickListener {
            if (!myLocationCheckbox.isChecked) {
                mMap.isMyLocationEnabled = false
            } else {
                AppPermissions(this, this, mMap).checkPermission()
            }
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
                Log.e(TAG, "Error setting layer with name ${spinner.selectedItem}")
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

    //  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12.0f))

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user")
                    mMap.isMyLocationEnabled = false
                    myLocationCheckbox.isChecked = false

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


    private fun setUpMap() {
        if (this == null) return
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
    }

    private fun fillMap() {
        try {
            mMap.clear()
        } catch (ex: UninitializedPropertyAccessException) {
            Log.w("Google map", "fillMap() invoked with uninitialized googleMap")
            return
        }
        for (i in 0 until routeSections.size) {
            mMap.addPolyline(
                PolylineOptions().add(
                    routeSections[i].beginning.toLatLng(),
                    routeSections[i].end.toLatLng()
                ).color(ContextCompat.getColor(this, R.color.fab_color_stop)).width(5f)
            )
        }
        if (routeSections.isNotEmpty()) {
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

        this.stopService(Intent(this, RunningTrackerService::class.java))
        this.unregisterReceiver(runningTrackerBroadcastReceiver)
        @Suppress("UNCHECKED_CAST")
        val localRouteSections = routeSections as ArrayList<Parcelable>
        routeSections = ArrayList()
        mMap.clear()

        time.base = SystemClock.elapsedRealtime()
        tracked_speed.text = getString(R.string.zero_colon)
        tracked_distance.text = getString(R.string.zero_dot)
        calories_burned.text = getString(R.string.zero)

        fabs_paused.visibility = View.GONE
        pause_fab_running.hide()
        go_fab_idle.show()

    }

    private fun speedCalc(distanceMeter: Double, timeMilliSec: Long ):Double{
       return (distanceMeter * 3600)/timeMilliSec
    }

    enum class TimerState { Idle, Running, Paused }


}
