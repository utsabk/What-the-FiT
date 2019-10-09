package com.example.runningapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningapp.listener.StepListener
import com.example.runningapp.services.RunningTrackerService
import com.example.runningapp.ui.activity.RunningTrackerActivity.Companion.REQUEST_CHECK_SETTINGS
import com.example.runningapp.ui.help.HelpActivity
import com.example.runningapp.utils.PrefUtils
import com.example.runningapp.utils.StepDetector
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlin.system.exitProcess
import com.example.runningapp.ui.home.HomeFragment
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.SparseArray
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.example.runningapp.ui.history.HistoryFragment
import com.example.runningapp.ui.home.HomeViewModel
import com.example.runningapp.ui.profile.ProfileFragment


class MainActivity  : AppCompatActivity(), SensorEventListener, StepListener {

    lateinit var homeFragment: HomeFragment
    lateinit var historyFragment: HistoryFragment
    lateinit var profileFragment: ProfileFragment
    private lateinit var currentDestination:NavDestination
    private var simpleStepDetector: StepDetector? = null
    private var sensorManager: SensorManager? = null
    private val TEXT_NUM_STEPS = "Steps: "
    private val DISTANCE_STEPS = " Distance: "
    private var numSteps: Int = 0
    private var distance: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
       /* AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_YES)*/

        if (savedInstanceState != null) {
            numSteps = savedInstanceState.getInt("Steps")
        }

        /*val actionBar = supportActionBar
        actionBar!!.hide()*/

        val userDataSharedPref =
            getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE)
        if (userDataSharedPref.getBoolean(getString(R.string.preference_first_launch), true)) {
            if (userDataSharedPref.getBoolean(getString(R.string.preference_first_launch), true)) {
                with(userDataSharedPref.edit()) {
                    putBoolean(getString(R.string.preference_first_launch), false)
                    apply()
                }
                showDialog()
            }
        }

        val sharedPreference = PrefUtils(this)
        numSteps = sharedPreference.getValueInt("Steps")
        distance = sharedPreference.getValueInt("DISTANCE_WITH_STEPS").toDouble()


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        simpleStepDetector = StepDetector()
        simpleStepDetector!!.registerListener(this)
        sensorManager!!.registerListener(
            this,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST
        )


        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        //default fragment is home fragment


        navView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    homeFragment = HomeFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.main_container, homeFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.navigation_history -> {
                    historyFragment = HistoryFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.main_container, historyFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.navigation_profile -> {
                    profileFragment = ProfileFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.main_container, profileFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
            }
            true
        }

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val navBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_profile, R.id.navigation_history
            )
        )


        navController.addOnDestinationChangedListener { _, destination, _ ->
             currentDestination = destination
            if (destination.id != R.id.navigation_profile) {
                menu_setting.visibility = View.GONE
            } else menu_setting.visibility = View.VISIBLE

        }
        setupActionBarWithNavController(navController, navBarConfiguration)
        navView.setupWithNavController(navController)


        menu_setting.setOnClickListener {
            startActivity(Intent(this, CollectDataActivity::class.java))
        }

        menu_help.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector!!.updateAccelerometer(
                event.timestamp,
                event.values[0],
                event.values[1],
                event.values[2]
            )
        }
    }

    override fun step(timeNs: Long) {
        numSteps++
        tvSteps.text = TEXT_NUM_STEPS.plus(numSteps)
        progressBar.progress = numSteps
        distance = (numSteps * 0.00076)
        println( distance)
        val integerDistance = Math.round(distance * 100 )
        print("this is:")
        println(integerDistance)
        progressBar_outer.progress = integerDistance.toInt()
        var display = Math.round(distance * 1000.0) / 1000.0
        tvSteps_distance.text = DISTANCE_STEPS.plus(display)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        val sharedPreference = PrefUtils(this)
        sharedPreference.saveInt("Steps", numSteps)
        sharedPreference.saveInt("DISTANCE_WITH_STEPS", distance.toInt())

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("Tag", "I'm inside new intent")
        stopService(Intent(this, RunningTrackerService::class.java))
        val pendingIntent: PendingIntent? =
            intent?.getParcelableExtra(RunningTrackerService.RESOLUTION_DATA_KEY)
        pendingIntent?.let {
            startIntentSenderForResult(
                pendingIntent.intentSender,
                REQUEST_CHECK_SETTINGS,
                null,
                0,
                0,
                0,
                null
            )
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tool_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_exit -> {
                exitApp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.launch_title))
        builder.setMessage(getString(R.string.launch_message))
        builder.setPositiveButton(R.string.ok) { _, _ ->
            startActivity(Intent(this, CollectDataActivity::class.java))
        }

        builder.show()
    }

    private fun exitApp() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.confirm_exit)
            .setPositiveButton(R.string.positive_exit) { _, _ -> exitProcess(0) }
            .setNegativeButton(R.string.negative_exit) { _, _ ->
                Toast.makeText(this, "Exit cancelled", Toast.LENGTH_LONG).show()
            }
        builder.create()
        builder.show()
    }


    override fun onBackPressed() {
        if (currentDestination.id == R.id.navigation_home) {
            exitApp()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("Steps", numSteps)
        outState.putDouble("Distance", distance)
    }

}
