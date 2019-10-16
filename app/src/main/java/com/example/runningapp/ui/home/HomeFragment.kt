package com.example.runningapp.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.runningapp.R
import com.example.runningapp.services.RunningTrackerService
import com.example.runningapp.ui.history.WorkoutViewModel
import com.example.runningapp.ui.history.WorkoutViewModelFactory
import com.example.runningapp.utils.PrefUtils
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class HomeFragment : Fragment(),CoroutineScope {
    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

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

                  //  tvSteps.text = TEXT_NUM_STEPS.plus(totalSteps)
                   // progressBar.progress = totalSteps
                    distance = (totalSteps * 0.00076) + totalDistance
                    println(distance)
                    val integerDistance = Math.round(distance * 100)
                    print("this is:")
                    println(integerDistance)
                   // progressBar_outer.progress = integerDistance.toInt()
                   // var display = Math.round(distance * 1000.0) / 1000.0
                   // tvSteps_distance.text = DISTANCE_STEPS.plus(display)
                }
            }

        }

    }


    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        // Two different ways to navigate from one fragment to another
        root.activity_card.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_navigation_home_to_navigation_activity, null)
        )


        root.heart_rate_card.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_navigation_home_to_nav_heartrate)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        job = Job()
        val viewModel = WorkoutViewModelFactory(activity!!.application).create(WorkoutViewModel::class.java)

        launch {
            val sharedPref =
                activity?.getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE)
            val timeUnit = sharedPref!!.getLong(getString(R.string.preference_time_unit), 0L)
            val calendar = Calendar.getInstance()

            if (timeUnit == 0L) {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
            } else {
                calendar.add(Calendar.MONTH, -1)
            }
            val trainings = viewModel.getLastTrainings(calendar.timeInMillis)

            launch(context = Dispatchers.Main){
                val distanceRan =
                    trainings.sumByDouble { training -> training.routeSections.sumByDouble { section -> section.distance.toDouble() } }
                val distanceGoal = sharedPref.getFloat(getString(R.string.preference_distance), 0F)

                val savedDayOfMonth = sharedPref.getInt(getString(R.string.preference_day_of_month),1)
                var todaysDayOfMonth = Calendar.DAY_OF_MONTH
                if(todaysDayOfMonth < savedDayOfMonth){
                    todaysDayOfMonth += 30
                }

                val daysPercentage =
                       if(timeUnit == 0L) ((todaysDayOfMonth - savedDayOfMonth)*100)/7 else ((todaysDayOfMonth - savedDayOfMonth)*100)/30

                Log.d("myTag","daysPercentage:---${daysPercentage}")

                 progressBar_outer.progress = daysPercentage

                if(daysPercentage >= 100)
                { days_gone_so_far.text = getString(R.string.home_page_finished_message)}
                else {days_gone_so_far.text = "(".plus(todaysDayOfMonth - savedDayOfMonth).plus("/").plus(if(timeUnit == 0L) 7 else 30).plus(")days")}

                val distancePercentage =
                    if (Math.round(distanceRan / distanceGoal * 100) > 100) 100 else Math.round(distanceRan / distanceGoal * 100)

                progressBar_inner.progress = distancePercentage.toInt()
                if(distancePercentage >= 100)
                    distance_travelled_so_far.text = getString(R.string.home_page_complete_message)
                else
                    distance_travelled_so_far.text =  "(".plus(Math.round(distanceRan).toString()).plus("/").plus(distanceGoal).plus(")m")

            }

        }
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

       // tvSteps.text = TEXT_NUM_STEPS.plus(totalSteps)
      //  progressBar.progress = totalSteps
        distance = (totalSteps * 0.00076) + totalDistance
        println(distance)
        val integerDistance = Math.round(distance * 100)
        print("this is:")
        println(integerDistance)
       // progressBar_outer.progress = integerDistance.toInt()
       // var display = Math.round(distance * 1000.0) / 1000.0
        // tvSteps_distance.text = DISTANCE_STEPS.plus(display)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        try {
            activity!!.unregisterReceiver(stepsBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
        }

        job.cancel()

    }

}