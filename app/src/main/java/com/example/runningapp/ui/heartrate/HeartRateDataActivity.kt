package com.example.runningapp.ui.heartrate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.runningapp.MainActivity
import com.example.runningapp.R
import kotlinx.android.synthetic.main.activity_heart_rate_data.*

class HeartRateDataActivity :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate_data)

        myGraph.addSeries(HearRate.list)
        val avgHeartRate = heartRateValues.sumBy { heartrate -> heartrate.toInt() }


       heart_rate_value.text = (avgHeartRate/ (heartRateValues.size) ).toString().plus(" BPM")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }
}