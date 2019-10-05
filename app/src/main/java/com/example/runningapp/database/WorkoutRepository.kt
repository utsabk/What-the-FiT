package com.example.runningapp.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.runningapp.database.dao.WorkoutDao
import com.example.runningapp.database.entity.Workout

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class WorkoutRepository(private val workoutDao: WorkoutDao) {


    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    var allWorkouts: LiveData<List<Workout>> = workoutDao.getAllWorkoutDetails()

    @WorkerThread
    // The suspend modifier tells the compiler that this must be called from a
    // coroutine or another suspend function.

    suspend fun insert(workout: Workout){
        workoutDao.insert(workout)
    }

    @WorkerThread
    suspend fun  delete(workout: Workout){
        workoutDao.deleteWorkout(workout)
    }


    suspend fun getLastWorkout(startDate:Long):List<Workout>{
      return  workoutDao.getLastWorkout(startDate)
    }
}