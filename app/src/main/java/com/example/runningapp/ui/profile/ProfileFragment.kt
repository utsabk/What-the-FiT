package com.example.runningapp.ui.profile

import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.services.RunningTrackerService
import com.example.runningapp.ui.history.WorkoutViewModel
import com.example.runningapp.ui.history.WorkoutViewModelFactory
import com.example.runningapp.utils.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.profile_achievements.*
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment() {

    private var sharedPref: SharedPreferences? = null


    companion object {

        const val BROADCAST_ACTION_STEPS =
            "com.example.runningapp.ui.home.homefragment.broadcastreceiversteps"

        private var calcSteps: Int = 0

        var totalSteps: Int = 0

    }

    private val stepsBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action

            when (action) {
                BROADCAST_ACTION_STEPS -> {

                    calcSteps = intent.getIntExtra(RunningTrackerService.STEPS_DATA_KEY, 0)

                    val sharedPreference = PrefUtils(context!!)
                    totalSteps = sharedPreference.getValueInt("Steps") + calcSteps

                    steps_taken_so_far.text = totalSteps.toString()
                    goal_steps.text = "/".plus("1000 steps")
                    progressBar_steps.progress = totalSteps

                }
            }

        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("Tag", "Inside onCreateView")
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Tag", "Inside onViewCreated")
        sharedPref =
            activity?.getSharedPreferences(USER_DATA_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

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

    override fun onResume() {
        super.onResume()
        Log.d("Tag", "Inside onResume")
        sharedPref =
            activity?.getSharedPreferences(USER_DATA_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)


        val bitmap = BitmapFactory.decodeFile(
            FileUtil.getOrCreateProfileImageFile(
                context!!,
                "image/jpeg"
            ).path
        )
        if (bitmap != null) {
            val scaleBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
            profile_user_image.setImageBitmap(scaleBitmap)
        } else profile_user_image.setImageBitmap(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.ic_add_a_photo_themed_24dp
            )
        )

        val firstname = sharedPref!!.getString(PREFERENCE_FIRST_NAME, "John")
        val lastname = sharedPref!!.getString(PREFERENCE_LAST_NAME, "Doe")
        profile_user_name.text = firstname.plus(" ").plus(lastname)
        profile_user_height.text =
            (sharedPref!!.getInt(PREFERENCE_HEIGHT, 165)).toString()
        profile_user_weight.text =
            (sharedPref!!.getInt(PREFERENCE_WEIGHT, 70)).toString()

        when (sharedPref!!.getInt(PREFERENCE_GENDER, 0)) {
            0 -> {
                profile_gender.text = getString(R.string.male)
            }
            1 -> {
                profile_gender.text = getString(R.string.female)
            }
            else -> return
        }


        // Progressive bar
        if (!RunningTrackerService.pauseService) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BROADCAST_ACTION_STEPS)
            activity!!.registerReceiver(stepsBroadcastReceiver, intentFilter)
        }

        val sharedPreference = PrefUtils(context!!)
        if (RunningTrackerService.stepCalcPaused) {
            totalSteps = sharedPreference.getValueInt(PREFERENCE_STEPS)

        } else {
            totalSteps = sharedPreference.getValueInt(PREFERENCE_STEPS) + calcSteps
        }
        steps_taken_so_far.text = totalSteps.toString()
        goal_steps.text = "/".plus("1000 steps")
        progressBar_steps.progress = totalSteps

    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            activity!!.unregisterReceiver(stepsBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
        }
    }


}