package com.example.runningapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.runningapp.listener.StepListener

class HomeViewModel() : ViewModel(),StepListener {
    private var numSteps: Int = 0

    override fun step(timeNs: Long) {
        numSteps++

    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
}