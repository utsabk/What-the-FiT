package com.example.runningapp.ui.heartrate

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.runningapp.AppPermissions
import com.example.runningapp.BODY_SENSOR_REQUEST_CODE
import com.example.runningapp.R
import com.jjoe64.graphview.series.DataPoint
import kotlinx.android.synthetic.main.activity_heartrate.*

var heartRateValues = ArrayList<Float>()

class HeartRateActivity : AppCompatActivity(), SensorEventListener {

    private var fingerState = State.FingerRemoved
    private lateinit var sensorManager: SensorManager

    private var senHeartRate: Sensor? = null

    private var heartRateValue: Float = 0.0F

    var heartrateMeasuredPercentage = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heartrate)

        AppPermissions(this, this).checkBodySensorPermission()

        Log.d("Tagbca", "Inside onCreate")

        after_finger_placed_layout.visibility = View.GONE
        heartrate_progress_bar_ConstraintLayout.visibility = View.GONE


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager


        senHeartRate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {
        Log.d("Tag", "onAccuracyChanged:--${sensor?.name}")

        Log.d("Tag", "onAccuracyChanged p1:--${p1}")
        fingerState = State.Measuring

        heartrateMeasuredPercentage = 0
    }

    override fun onSensorChanged(p0: SensorEvent?) {

        //Log.d("Tag", "SensorEvent:--${p0}")
        //  FingerPlaced = false


        heartRateValue = (p0?.values?.get(0) ?: -1f).toFloat()

        if (p0?.sensor == senHeartRate) {

            when (fingerState) {
                State.FingerRemoved -> {
                    put_finger_layout.visibility = View.VISIBLE
                    after_finger_placed_layout.visibility = View.GONE
                    fingerState = State.FingerPlaced
                    // fingerState = State.FingerPlaced
                }

                State.FingerPlaced -> {
                    put_finger_layout.visibility = View.GONE
                    after_finger_placed_layout.visibility = View.VISIBLE
                    heartrate_anm_heart.startAnimation(
                        AnimationUtils.loadAnimation(
                            this,
                            R.anim.heart_beat
                        )
                    )



                    fingerState = State.FingerRemoved
                }

                State.Measuring -> {
                    put_finger_layout.visibility = View.GONE
                    after_finger_placed_layout.visibility = View.VISIBLE
                    heartrate_anm_heart.startAnimation(
                        AnimationUtils.loadAnimation(
                            this,
                            R.anim.heart_beat
                        )
                    )

                    // Change progress bar state
                    heartrateMeasuredPercentage += 10

                    if (heartrateMeasuredPercentage > 100) {
                        val intent = Intent(
                            this, HeartRateDataActivity

                            ::class.java
                        )
                        startActivity(intent)
                    } else {
                        heartrate_progress_bar.progress = heartrateMeasuredPercentage
                        measured_percentage_text.text =
                            heartrateMeasuredPercentage.toString().plus("%")
                    }


                    // Condition to show progressive bar
                    if(heartrateMeasuredPercentage <= 10){
                        heartrate_progress_bar_ConstraintLayout.visibility = View.GONE
                    }else heartrate_progress_bar_ConstraintLayout.visibility = View.VISIBLE





                    if (heartRateValue > 0.0 && heartRateValue < 120.0) {
                        heartRateValues.add(heartRateValue)
                        HearRate.list.appendData(DataPoint(heartRateValues.size.toDouble(), heartRateValue.toDouble()), false, 100)
                    }
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        senHeartRate?.also {
            sensorManager.registerListener(
                this, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // This function is triggered when user chooses an option from permission dialogue
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            BODY_SENSOR_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("Tag", "Permission has been denied by user")
                    finish()
                } else {
                    Log.i("Tag", "Permission has been granted by user")
                }
            }
        }

    }

    enum class State { FingerPlaced, FingerRemoved, Measuring }
}
