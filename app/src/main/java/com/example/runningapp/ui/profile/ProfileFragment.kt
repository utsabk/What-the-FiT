package com.example.runningapp.ui.profile

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.ui.history.WorkoutViewModel
import com.example.runningapp.ui.history.WorkoutViewModelFactory
import com.example.runningapp.ui.home.HomeFragment.Companion.totalDistance
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.profile_achievements.*
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment() {

    private var sharedPref: SharedPreferences? = null
    private var stackedChart: BarChart? = null
    internal var colorClassArray = intArrayOf(Color.rgb(0,128,255), Color.rgb(255,171,64), Color.rgb(0,150,136))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("Tag", "Inside onCreateView")
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        stackedChart = root.findViewById(R.id.stacked_bar)
        val barDataSet = BarDataSet(dataValues1(), "Bar Set")
        barDataSet.setColors(*colorClassArray)
        val barData = BarData(barDataSet)

        stackedChart!!.setData(barData)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Tag", "Inside onViewCreated")

        val viewModel =
            WorkoutViewModelFactory(activity!!.application).create(WorkoutViewModel::class.java)
        viewModel.workoutList.observe(this, Observer { workoutList ->
            try {
                workoutList?.let {
                    achievements_trainings_count.text = workoutList.size.toString()
                    achievements_distance_sum.text =
                        Math.round(workoutList.sumByDouble { workout -> workout.routeSections.sumByDouble { section -> section.distance.toDouble() } } / 1000)
                            .toString()

                    achievements_time_sum.text =
                        (workoutList.sumBy { workout -> workout.timeInMillis.toInt() } / 36000000).toString()
                    achievements_calories_sum.text =
                        workoutList.sumBy { workout -> workout.caloriesBurnt }.toString()

                    achievements_best_distance.text =
                        (Math.round(workoutList.maxBy { workout ->
                            workout.routeSections.sumByDouble { section -> section.distance.toDouble() }
                        }!!.routeSections.sumByDouble { section -> section.distance.toDouble() } * 100.0) / 100).toString()
                            .plus("m")
                }

                achievements_best_calories.text =
                    workoutList.maxBy { workout -> workout.caloriesBurnt }!!.caloriesBurnt.toString()
                        .plus("kcal")

                val longestTime =
                    workoutList.maxBy { workout -> workout.timeInMillis }!!.timeInMillis
                achievements_longest_time.text = if (longestTime >= 3600000)
                    String.format(
                        "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(longestTime),
                        TimeUnit.MILLISECONDS.toMinutes(longestTime) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(longestTime) % TimeUnit.MINUTES.toSeconds(1)
                    )
                else
                    String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(longestTime) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(longestTime) % TimeUnit.MINUTES.toSeconds(1)
                    )
            } catch (ex: KotlinNullPointerException) {
                Log.d("Tag", "Null point exception")
            }
        })
    }

    private fun dataValues1(): ArrayList<BarEntry> {
        val dataVals = ArrayList<BarEntry>()
        dataVals.add(BarEntry(1f, floatArrayOf(8f, 3f, 2f)))
        dataVals.add(BarEntry(3f, floatArrayOf(0f, 1f, 5.3f)))
        dataVals.add(BarEntry(2f, floatArrayOf(2f, 3f, 8f)))
        dataVals.add(BarEntry(4f, floatArrayOf(2f, 6f, 8f)))
        dataVals.add(BarEntry(5f, floatArrayOf(1f, 5f, 5f)))
        return dataVals
    }



    override fun onResume() {
        super.onResume()
        Log.d("Tag", "Inside onResume")
        sharedPref =
            activity?.getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE)

        val firstname = sharedPref!!.getString(getString(R.string.preference_first_name), "John")
        val lastname = sharedPref!!.getString(getString(R.string.preference_last_name), "Doe")
        profile_user_name.text = firstname.plus(" ").plus(lastname)
        profile_user_height.text =
            (sharedPref!!.getInt(getString(R.string.preference_height), 70)).toString()
        profile_user_weight.text =
            (sharedPref!!.getInt(getString(R.string.preference_weight), 165)).toString()

        when (sharedPref!!.getInt(getString(R.string.preference_gender), 0)) {
            0 -> {
                profile_gender.text = getString(R.string.male)
            }
            1 -> {
                profile_gender.text = getString(R.string.female)
            }
            else -> return
        }

    }

}