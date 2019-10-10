package com.example.runningapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.runningapp.DataConverter
import com.example.runningapp.database.dao.WorkoutDao
import com.example.runningapp.database.entity.Workout

@Database(entities = [Workout::class], version = 1, exportSchema = false)

@TypeConverters(DataConverter::class)

abstract class WorkoutRoomDatabase : RoomDatabase() {

    abstract fun workoutDataDao(): WorkoutDao

    companion object {

        @Volatile
        private var INSTANCE: WorkoutRoomDatabase? = null

        operator fun invoke(context: Context): WorkoutRoomDatabase {

            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance

            synchronized(this) {

                // get an instance of the created database
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutRoomDatabase::class.java,
                    "workout.db"
                ).build()

                INSTANCE = instance

                return instance
            }


        }
    }
}