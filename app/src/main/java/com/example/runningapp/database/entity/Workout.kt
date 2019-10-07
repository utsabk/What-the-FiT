package com.example.runningapp.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runningapp.models.RouteSection
import java.util.*


@Entity(tableName = "workout_data")
data class Workout(
    @PrimaryKey
    val date: Date,
    val timeInMillis: Long,
    val routeSections: List<RouteSection>, // Using DataConverter, we can use our custom types in queries
    var status: String,
    val caloriesBurnt: Int,
    val recommendedWaterIntake: Int
)
