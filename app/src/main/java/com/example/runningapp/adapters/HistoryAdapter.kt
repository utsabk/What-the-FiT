package com.example.runningapp.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.runningapp.R
import com.example.runningapp.WorkoutDetailsActivity
import com.example.runningapp.database.entity.Workout
import com.example.runningapp.ui.activity.RunningTrackerActivity
import com.example.runningapp.ui.history.WorkoutViewModel
import com.example.runningapp.ui.history.WorkoutViewModelFactory
import kotlinx.android.synthetic.main.history_row.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class HistoryAdapter(private val context: Context) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var workouts: List<Workout> = Collections.emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.history_row, parent, false)
        return HistoryViewHolder(view)

    }

    override fun getItemCount(): Int {
        return workouts.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.setData(workouts[position])
    }

    fun collectData(workouts: List<Workout>) {
        this.workouts = workouts
        notifyDataSetChanged()
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun setData(workout: Workout) {
            itemView.history_workout_date.text =
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(workout.date)

            itemView.history_distance.text =
                "%2f KM".format(workout.routeSections.sumByDouble { it.distance.toDouble() } / 1000)

            val formattedTime = String.format(
                "%02d:%02d",
                TimeUnit.MICROSECONDS.toHours(workout.timeInMillis),
                TimeUnit.MILLISECONDS.toMinutes(workout.timeInMillis) % TimeUnit.HOURS.toMinutes(1)
            )

            itemView.history_time.text = formattedTime
            itemView.history_kcal.text = workout.caloriesBurnt.toString().plus("KCAL")

            itemView.setOnClickListener {
                @Suppress("UNCHECKED_CAST") val intent =
                    Intent(context, WorkoutDetailsActivity::class.java)
                        .putExtra(RunningTrackerActivity.TIME_DATA_KEY, workout.timeInMillis)
                        .putParcelableArrayListExtra(
                            RunningTrackerActivity.ROUTE_SECTIONS_DATA_KEY,
                            workout.routeSections as ArrayList<Parcelable>
                        )
                        .putExtra(RunningTrackerActivity.CALORIES_DATA_KEY, workout.caloriesBurnt)
                        .putExtra(RunningTrackerActivity.STATUS_DATA_KEY, workout.status)

                // This flag will make the Activity start of a new task on this history stack.
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }


            itemView.setOnLongClickListener {
                val viewModel = WorkoutViewModelFactory(context.applicationContext as Application).create(WorkoutViewModel::class.java)

                val builder = AlertDialog.Builder(context)
                builder.setMessage(R.string.confirm_delete)
                    .setPositiveButton(R.string.positive_delete){_,_ -> viewModel.delete(workout)}
                    .setNegativeButton(R.string.negative_delete){_,_ ->}
                builder.create()
                builder.show()
                return@setOnLongClickListener true
            }


        }

    }
}
