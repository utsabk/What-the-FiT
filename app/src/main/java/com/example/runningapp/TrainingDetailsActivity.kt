package com.example.runningapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.runningapp.models.RouteSection
import com.example.runningapp.ui.activity.RunningTrackerActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_training_details.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.roundToInt

class TrainingDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var currentDate: Date
    private var measuredTimeInMillis = 0L
    private lateinit var routeSections: List<RouteSection>
    private var calories = 0
    private var recommendedWaterIntake = 0
    private var launchedFromHistory = true
    private var statusText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_details)

        currentDate = Calendar.getInstance().time
        measuredTimeInMillis = intent.getLongExtra(RunningTrackerActivity.TIME_DATA_KEY, 0L)
        routeSections =
            intent.getParcelableArrayListExtra<RouteSection>(RunningTrackerActivity.ROUTE_SECTIONS_DATA_KEY) as List<RouteSection>
        calories = intent.getIntExtra(RunningTrackerActivity.CALORIES_DATA_KEY, 0)
        launchedFromHistory = intent.getBooleanExtra(RunningTrackerActivity.MODE_DATA_KEY, true)

        if (launchedFromHistory) {
            statusText = intent.getStringExtra(RunningTrackerActivity.STATUS_DATA_KEY)
            status.setText(statusText)
            status.isFocusable = false
        }

       val weight = 60 //This is just for test purposes
       recommendedWaterIntake = (12F / 3_720_000F * weight * measuredTimeInMillis).roundToInt()

        summary_measured_time.text =
            if (measuredTimeInMillis >= 3600000)
                String.format(
                    "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(measuredTimeInMillis),
                    TimeUnit.MILLISECONDS.toMinutes(measuredTimeInMillis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(measuredTimeInMillis) % TimeUnit.MINUTES.toSeconds(1)
                )
            else
                String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(measuredTimeInMillis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(measuredTimeInMillis) % TimeUnit.MINUTES.toSeconds(1)
                )

        val distance = routeSections.sumByDouble { section -> section.distance.toDouble() }

        val distanceInKm = distance .roundToInt().toDouble() / 1000

        val pace = measuredTimeInMillis / distance / 60

        summary_measured_distance.text = distanceInKm.toString().plus(" km")
        summary_measured_speed.text =
            (((distance * 360000.0) / measuredTimeInMillis.toDouble()).roundToInt() / 100.0).toString().plus(" km/h")
        summary_measured_pace.text = String.format(
            "%01d:%02d",
            floor(pace).toInt(),
            floor((pace - floor(pace)) * 60).toInt()
        )
        summary_measured_water.text = recommendedWaterIntake.toString().plus(" ml")
        summary_measured_calories.text = calories.toString().plus(" kcal")

        summary_map.onCreate(savedInstanceState)
        summary_map.onResume()
        summary_map.getMapAsync(this)
    }


    override fun onMapReady(_googleMap: GoogleMap) {
        googleMap = _googleMap
        fillMap()
    }

    private fun fillMap() {
        try {
            googleMap.clear()
        } catch (ex: UninitializedPropertyAccessException) {
            Log.w("Google map", "fillMap() invoked with uninitialized googleMap")
            return
        }
        for (i in routeSections.indices) {
            googleMap.addPolyline(
                PolylineOptions().add(
                    routeSections[i].beginning.toLatLng(),
                    routeSections[i].end.toLatLng()
                ).color(ContextCompat.getColor(this, R.color.fab_color_stop)).width(10f)
            )

            // Marker at beginning of the Journey
            googleMap.addMarker(MarkerOptions().position(routeSections.first().beginning.toLatLng()))

            // Marker at the end of the Journey
            googleMap.addMarker(
                MarkerOptions().position(routeSections.last().end.toLatLng())
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            BitmapFactory.decodeResource(resources, R.drawable.ic_user_location)
                        )
                    )

            )
        }
        if (routeSections.isNotEmpty()) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(routeSections.last().end.toLatLng(), 14f))
        }
    }
}
