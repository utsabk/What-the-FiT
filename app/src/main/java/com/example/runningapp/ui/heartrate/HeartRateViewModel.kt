package com.example.runningapp.ui.heartrate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HeartRateViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is HeartRate Fragment"
    }
    val text: LiveData<String> = _text
}