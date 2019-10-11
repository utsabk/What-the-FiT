package com.example.runningapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.example.runningapp.utils.PrefUtils



class Settings : AppCompatActivity() {
    private var mySwitch: SwitchCompat? = null
    override fun onCreate(saveInstanceState: Bundle?) {
       if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        } else
            setTheme(R.style.AppTheme)
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_settings)
        mySwitch = findViewById<SwitchCompat>(R.id.theme_button)
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
         mySwitch!!.isChecked = true
        }
        mySwitch!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                //restartApp()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
               // mySwitch!!.setChecked(false)
               // sharedPrf.setNightModeState(false)
                //restartApp()
            }
        }

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Settings"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    fun restartApp() {
        val i = Intent(applicationContext, Settings::class.java)
        startActivity(i)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
