package com.example.runningapp.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.runningapp.R
import com.example.runningapp.services.RunningTrackerService
import com.example.runningapp.utils.PrefUtils
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private val TEXT_NUM_STEPS = "Steps: "
    private val DISTANCE_STEPS = " Distance: "

    companion object {

        const val BROADCAST_ACTION_STEPS =
            "com.example.runningapp.ui.home.homefragment.broadcastreceiversteps"

        private var calcSteps: Int = 0

        var totalSteps: Int = 0


        private var distance: Double = 0.0

        var totalDistance: Double = 0.0

    }


    private lateinit var homeViewModel: HomeViewModel

    private val stepsBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action

            when (action) {
                BROADCAST_ACTION_STEPS -> {

                    calcSteps = intent.getIntExtra(RunningTrackerService.STEPS_DATA_KEY, 0)

                    Log.d("Hawa", "calcStep:--$calcSteps")


                    val sharedPreference = PrefUtils(context!!)
                    totalSteps = sharedPreference.getValueInt("Steps") + calcSteps
                    totalDistance = sharedPreference.getValueInt("DISTANCE_WITH_STEPS").toDouble()

                    tvSteps.text = TEXT_NUM_STEPS.plus(totalSteps)
                    progressBar.progress = totalSteps
                    distance = (totalSteps * 0.00076) + totalDistance
                    println(distance)
                    val integerDistance = Math.round(distance * 100)
                    print("this is:")
                    println(integerDistance)
                    progressBar_outer.progress = integerDistance.toInt()
                    var display = Math.round(distance * 1000.0) / 1000.0
                    tvSteps_distance.text = DISTANCE_STEPS.plus(display)
                }
            }

        }

    }


    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        // Two different ways to navigate from one fragment to another
        root.activity_card.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_navigation_home_to_navigation_activity,
                null
            )
        )


        root.heart_rate_card.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_navigation_home_to_nav_heartrate)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Tag", "Time:--${SystemClock.elapsedRealtime()}")
    }


    override fun onResume() {
        super.onResume()

        Log.d("Tag", "Inside on resume")

        if (!RunningTrackerService.pauseService) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BROADCAST_ACTION_STEPS)
            activity!!.registerReceiver(stepsBroadcastReceiver, intentFilter)
        }

        val sharedPreference = PrefUtils(context!!)
        if (RunningTrackerService.stepCalcPaused) {
            totalSteps = sharedPreference.getValueInt("Steps")

        } else {
            totalSteps = sharedPreference.getValueInt("Steps") + calcSteps
        }
        totalDistance = sharedPreference.getValueInt("DISTANCE_WITH_STEPS").toDouble()

        tvSteps.text = TEXT_NUM_STEPS.plus(totalSteps)
        progressBar.progress = totalSteps
        distance = (totalSteps * 0.00076) + totalDistance
        println(distance)
        val integerDistance = Math.round(distance * 100)
        print("this is:")
        println(integerDistance)
        progressBar_outer.progress = integerDistance.toInt()
        var display = Math.round(distance * 1000.0) / 1000.0
        tvSteps_distance.text = DISTANCE_STEPS.plus(display)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Hello", "Inside Ondestroy fragment")

        try {
            activity!!.unregisterReceiver(stepsBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
        }
    }

}