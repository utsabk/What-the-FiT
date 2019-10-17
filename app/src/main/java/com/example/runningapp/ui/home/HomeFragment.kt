package com.example.runningapp.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.runningapp.R
import com.example.runningapp.ui.history.WorkoutViewModel
import com.example.runningapp.ui.history.WorkoutViewModelFactory
import com.example.runningapp.utils.PREFERENCE_DAY_OF_MONTH
import com.example.runningapp.utils.PREFERENCE_DISTANCE
import com.example.runningapp.utils.PREFERENCE_TIME_UNIT
import com.example.runningapp.utils.SETTING_PREFERENCE_FILE_KEY
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class HomeFragment : Fragment(),CoroutineScope {
    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        // Two different ways to navigate from one fragment to another
        root.cardView_workout.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_navigation_home_to_navigation_activity, null)
        )


        root.cardView_heart_rate.setOnClickListener { view ->
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
                activity?.getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            val timeUnit = sharedPref!!.getLong(PREFERENCE_TIME_UNIT, 0L)
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
                val distanceGoal = sharedPref.getFloat(PREFERENCE_DISTANCE, 0F)

                val savedDayOfMonth = sharedPref.getInt(PREFERENCE_DAY_OF_MONTH,1)
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

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()

    }

}