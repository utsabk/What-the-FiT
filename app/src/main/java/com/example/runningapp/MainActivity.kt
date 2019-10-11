package com.example.runningapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningapp.services.RunningTrackerService
import com.example.runningapp.ui.activity.RunningTrackerActivity.Companion.REQUEST_CHECK_SETTINGS
import com.example.runningapp.ui.help.HelpActivity
import com.example.runningapp.ui.history.HistoryFragment
import com.example.runningapp.ui.home.HomeFragment
import com.example.runningapp.ui.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlin.system.exitProcess


class MainActivity  : AppCompatActivity() {

    lateinit var homeFragment: HomeFragment
    lateinit var historyFragment: HistoryFragment
    lateinit var profileFragment: ProfileFragment
    private lateinit var currentDestination:NavDestination

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
       /* AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_YES)*/


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




        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        //default fragment is home fragment



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
            R.id.settings_app -> {
                appSettings()
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

    private fun appSettings() {
//        startActivity(Intent(this@MainActivity, EditProfileActivity::class.java))
        startActivity(Intent(this@MainActivity, Settings::class.java))

    }


    override fun onBackPressed() {
        if (currentDestination.id == R.id.navigation_home) {
            exitApp()
        } else {
            super.onBackPressed()
        }
    }

}
