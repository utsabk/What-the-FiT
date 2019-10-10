package com.example.runningapp.ui.heartrate

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.runningapp.R
import kotlinx.android.synthetic.main.activity_heart_rate_data.*

class HeartRateDataActivity :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate_data)

        myGraph.addSeries(HearRate.list)

        Log.d("Tagdef","heartrateValues:--${heartRateValues}")
        Log.d("Tagdef","heartrateValues.size:--${heartRateValues.size}")


        val avgHeartRate = heartRateValues.sumBy { heartrate -> heartrate.toInt() }


       heart_rate_value.text = (avgHeartRate/ (heartRateValues.size) ).toString().plus(" BPM")
    }
}