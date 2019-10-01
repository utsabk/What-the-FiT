package com.example.runningapp

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningapp.services.RunningTrackerService
import com.google.android.material.navigation.NavigationView
import com.example.runningapp.ui.activity.RunningTrackerActivity.Companion.REQUEST_CHECK_SETTINGS



class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Log.d("Tag","I am inside onNewIntent")

        stopService(Intent(this, RunningTrackerService::class.java))
        val pendingIntent: PendingIntent? =
            intent?.getParcelableExtra(RunningTrackerService.RESOLUTION_DATA_KEY)
        pendingIntent?.let {

            Log.d("Tag","I am inside pendingIntent")

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        navView.getHeaderView(0).setOnClickListener { view ->
            Log.d("Tag","Clicked")
            //supportFragmentManager.beginTransaction().add(R.id.nav_host_fragment,ProfileFragment()).commit()
        }


        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_activity, R.id.nav_heartrate, R.id.nav_history,
                R.id.nav_bmi, R.id.nav_help, R.id.nav_exit
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }



}
