package com.example.runningapp.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.runningapp.database.entity.Workout

@Dao
interface WorkoutDao{
    @Insert
    fun insert(sessionData: Workout)

    @Query("SELECT * FROM workout_data ORDER BY date DESC")
    fun getAllWorkoutDetails(): LiveData<List<Workout>>

    @Delete
    fun deleteWorkout(workout: Workout)
}

