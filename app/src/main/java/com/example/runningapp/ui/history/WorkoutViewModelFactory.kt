package com.example.runningapp.ui.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WorkoutViewModelFactory (private val application: Application): ViewModelProvider.NewInstanceFactory(){

    //Creates a new instance of the WorkoutViewModel class
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WorkoutViewModel(application) as T
    }
}