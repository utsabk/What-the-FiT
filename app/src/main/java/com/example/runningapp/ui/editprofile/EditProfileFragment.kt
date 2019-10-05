package com.example.runningapp.ui.editprofile

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.runningapp.R

class EditProfileFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }
}