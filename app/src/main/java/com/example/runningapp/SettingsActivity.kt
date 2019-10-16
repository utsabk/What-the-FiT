package com.example.runningapp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.runningapp.utils.PrefUtils
import com.example.runningapp.utils.Theme
import kotlinx.android.synthetic.main.activity_settings.*


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
