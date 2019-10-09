package com.example.runningapp.ui.editprofile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.example.runningapp.R
import com.example.runningapp.utils.PrefUtils
import com.example.runningapp.utils.Theme
import kotlinx.android.synthetic.main.activity_editprofile.*

class EditProfileActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var sharedPreferenceUtil: PrefUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)
        setSupportActionBar(settings_toolbar)
        supportActionBar?.let {
            it.title = "Settings"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        theme_button.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // The switch is enabled/checked
                AppCompatDelegate.MODE_NIGHT_YES
                // Change the app background color
            } else {
                // The switch is disabled
                AppCompatDelegate.MODE_NIGHT_NO

            }
        }

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, p3: Long) {
        val selectedTheme: String = parent?.getItemAtPosition(position).toString()
        Theme.setThemes(selectedTheme)
        sharedPreferenceUtil.saveTheme(selectedTheme)
    }
}