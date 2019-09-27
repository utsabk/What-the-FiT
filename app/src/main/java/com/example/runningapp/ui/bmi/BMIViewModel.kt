package com.example.runningapp.ui.bmi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BMIViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is BMI Fragment"
    }
    val text: LiveData<String> = _text
}