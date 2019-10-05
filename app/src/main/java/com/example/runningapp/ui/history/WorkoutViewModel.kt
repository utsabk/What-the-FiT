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

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main


    // The implementation of methods is completely hidden from the UI.
    // We don't want insert to block the main thread, so we're launching a new
    // coroutine. ViewModels have a coroutine scope based on their lifecycle called
    // viewModelScope which we can use here.
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

    fun insert(workout: Workout) = scope.launch(Dispatchers.IO) {
        repository.insert(workout)
    }

    fun delete(workout: Workout) = scope.launch(Dispatchers.IO){
        repository.delete(workout)
    }

   suspend fun getLastWorkout(startDate: Long):List<Workout>{
        return repository.getLastWorkout(startDate)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}