package com.example.runningapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.runningapp.utils.PrefUtils
import com.example.runningapp.utils.SETTING_PREFERENCE_FILE_KEY
import com.example.runningapp.utils.Theme
import com.example.runningapp.utils.USER_DATA_PREFERENCE_FILE_KEY
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.setting_resolution.*


class SettingsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var sharedPreferenceUtil: PrefUtils


    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(settings_toolbar)
        supportActionBar?.let {
            it.title = "SettingsActivity"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        val themeSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.theme_settings, android.R.layout.simple_spinner_item)
        themeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        theme_spinner.adapter = themeSpinnerAdapter
        theme_spinner.onItemSelectedListener = this
        sharedPreferenceUtil = PrefUtils(this)
        theme_spinner.setSelection(themeSpinnerAdapter.getPosition(sharedPreferenceUtil.getSavedTheme()))


        val sharedPreferences =
            getSharedPreferences(SETTING_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        resolution_save_button.setOnClickListener {
            val distance = resolution_distance.text.toString().toFloat()
            val distanceUnit = resolution_distance_unit.selectedItemId
            val timeUnit = resolution_time_unit.selectedItemId

            with(sharedPreferences!!.edit()) {
                putFloat(com.example.runningapp.utils.PREFERENCE_DISTANCE, if (distanceUnit == 0L) distance * 1000 else distance)
                putLong(com.example.runningapp.utils.PREFERENCE_TIME_UNIT, timeUnit)
                putInt(com.example.runningapp.utils.PREFERENCE_DAY_OF_MONTH, java.util.Calendar.DAY_OF_MONTH)
                apply()

            }

            resolution_distance.text.clear()

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
        Theme.setTheme(selectedTheme)
        sharedPreferenceUtil.saveTheme(selectedTheme)
    }

}
