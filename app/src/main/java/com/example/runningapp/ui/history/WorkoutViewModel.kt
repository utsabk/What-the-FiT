package com.example.runningapp.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.runningapp.database.WorkoutRepository
import com.example.runningapp.database.WorkoutRoomDatabase
import com.example.runningapp.database.entity.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


// Class extends AndroidViewModel and requires application as a parameter.
class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    //JOB is a cancellable thing,
    // Where cancellation of a parent leads to immediate cancellation of all its children
    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main


    // A CoroutineScope manages one or more related Coroutines
    // Additionally, it also starts a new Coroutine within that scope.
    // However, it doesn't run the Coroutines
    private val scope = CoroutineScope(coroutineContext)


    // The ViewModel maintains a reference to the repository to get data.
    private val repository: WorkoutRepository

    // LiveData gives us updated Workout when they change.
    internal val workoutList: LiveData<List<Workout>>

    init {
        val workoutDao = WorkoutRoomDatabase.invoke(application).workoutDataDao()
        repository = WorkoutRepository(workoutDao)
        workoutList = repository.allWorkouts
    }

    // LAUNCH starts a new Coroutine and doesn't return the result to the caller.
    // DISPATCHERS.IO is optimized to perform disk or network I/O outside of the main thread
    fun insert(workout: Workout) = scope.launch(Dispatchers.IO) {
        repository.insert(workout)
    }

    fun delete(workout: Workout) = scope.launch(Dispatchers.IO) {
        repository.delete(workout)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}