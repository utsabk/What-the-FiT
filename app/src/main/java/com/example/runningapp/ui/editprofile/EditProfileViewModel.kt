package com.example.runningapp.ui.editprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is EditProfile Fragment"
    }
    val text: LiveData<String> = _text
}