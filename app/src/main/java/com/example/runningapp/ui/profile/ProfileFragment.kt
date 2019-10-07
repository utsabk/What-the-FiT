package com.example.runningapp.ui.profile

import android.content.Context
import android.content.SharedPreferences
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
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.profile_achievements.*
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment() {

    private var sharedPref: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("Tag", "Inside onCreateView")
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Tag", "Inside onViewCreated")


        val viewModel =
            WorkoutViewModelFactory(activity!!.application).create(WorkoutViewModel::class.java)
        viewModel.workoutList.observe(this, Observer { workoutList ->
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
                    (Math.round(workoutList.maxBy {
                            workout -> workout.routeSections.sumByDouble{
                            section -> section.distance.toDouble() } }!!.routeSections.sumByDouble{
                            section -> section.distance.toDouble() } * 100.0) / 100).toString()
                        .plus("m")
            }

            achievements_best_calories.text =
                workoutList.maxBy { workout -> workout.caloriesBurnt }!!.caloriesBurnt.toString()
                    .plus("kcal")

            val longestTime = workoutList.maxBy { workout -> workout.timeInMillis }!!.timeInMillis
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
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d("Tag", "Inside onActivityCreated")

    }

    override fun onStart() {
        super.onStart()
        Log.d("Tag", "Inside onStart")
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


    override fun onResume() {
        super.onResume()
        Log.d("Tag", "Inside onResume")

    }

    override fun onPause() {
        super.onPause()
        Log.d("Tag", "Inside onPause")

    }

    override fun onStop() {
        super.onStop()
        Log.d("Tag", "Inside onStop")


    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Tag", "Inside onDestroyView")

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("Tag", "Inside onDestroy")

    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        sharedPref = activity?.getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE)
//
//        val firstname = sharedPref!!.getString(getString(R.string.preference_first_name),"John")
//        val lastname = sharedPref!!.getString(getString(R.string.preference_last_name),"Doe")
//        profile_user_name.text = firstname.plus(" ").plus(lastname)
//      //  profile_user_height.text = (sharedPref!!.getInt(getString(R.string.preference_height),70)).toString()
//        profile_user_weight.text = (sharedPref!!.getInt(getString(R.string.preference_weight),165)).toString()
//
//        when(sharedPref!!.getInt(getString(R.string.preference_gender),0)){
//            0 -> {
//                profile_gender.text = getString(R.string.male)
//            }
//            1 ->{profile_gender.text = getString(R.string.female)}
//            else -> return
//        }
//    }
}