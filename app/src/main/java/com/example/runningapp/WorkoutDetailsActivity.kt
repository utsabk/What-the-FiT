package com.example.runningapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.runningapp.database.entity.Workout
import com.example.runningapp.models.RouteSection
import com.example.runningapp.ui.activity.RunningTrackerActivity
import com.example.runningapp.ui.history.WorkoutViewModel
import com.example.runningapp.ui.history.WorkoutViewModelFactory
import com.example.runningapp.utils.PREFERENCE_WEIGHT
import com.example.runningapp.utils.USER_DATA_PREFERENCE_FILE_KEY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_workout_details.*
import kotlinx.android.synthetic.main.app_bar_workoutdetails.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.roundToInt

class WorkoutDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var sharedPref: SharedPreferences? = null

    private lateinit var currentDate: Date
    private var measuredTimeInMillis = 0L
    private lateinit var routeSections: List<RouteSection>
    private var calories = 0
    private var recommendedWaterIntake = 0
    private var launchedFromHistory = true
    private var statusText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_details)
        val toolbar: Toolbar = findViewById(R.id.workoutDetails_toolbar)
        setSupportActionBar(toolbar)

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

        sharedPref = getSharedPreferences(USER_DATA_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val weight = sharedPref!!.getInt(PREFERENCE_WEIGHT, 65)
        recommendedWaterIntake = (12F / 3_720_000F * weight * measuredTimeInMillis).roundToInt()

        summary_measured_time.text =
            if (measuredTimeInMillis >= 3600000)
                String.format(
                    "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(measuredTimeInMillis),
                    TimeUnit.MILLISECONDS.toMinutes(measuredTimeInMillis) % TimeUnit.HOURS.toMinutes(
                        1
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(measuredTimeInMillis) % TimeUnit.MINUTES.toSeconds(
                        1
                    )
                )
            else
                String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(measuredTimeInMillis) % TimeUnit.HOURS.toMinutes(
                        1
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(measuredTimeInMillis) % TimeUnit.MINUTES.toSeconds(
                        1
                    )
                )

        val distance = routeSections.sumByDouble { section -> section.distance.toDouble() }

        val distanceInKm = distance.roundToInt().toDouble() / 1000

        val pace = measuredTimeInMillis / distance / 60

        summary_measured_distance.text = distanceInKm.toString().plus(" km")
        summary_measured_speed.text =
            (((distance * 360000.0) / measuredTimeInMillis.toDouble()).roundToInt() / 100.0).toString()
                .plus(" km/h")
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

        menu_close.setOnClickListener {
            //super.onBackPressed()
            finish()
        }
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
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    routeSections.last().end.toLatLng(),
                    14f
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (launchedFromHistory) return
        val viewModel = WorkoutViewModelFactory(application).create(WorkoutViewModel::class.java)
        val workout =
            Workout(
                currentDate,
                measuredTimeInMillis,
                routeSections,
                status.text.toString(),
                calories,
                recommendedWaterIntake
            )
        viewModel.insert(workout)
    }
}
