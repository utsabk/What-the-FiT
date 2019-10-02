package com.example.runningapp

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningapp.services.RunningTrackerService
import com.example.runningapp.ui.activity.RunningTrackerActivity.Companion.REQUEST_CHECK_SETTINGS
import com.example.runningapp.ui.editprofile.EditProfileActivity
import com.example.runningapp.ui.help.HelpActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val navBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_profile, R.id.navigation_history
            )
        )


        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.navigation_profile) {
                menu_setting.visibility = View.GONE
            } else menu_setting.visibility = View.VISIBLE

        }
        setupActionBarWithNavController(navController, navBarConfiguration)
        navView.setupWithNavController(navController)


        menu_setting.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        menu_help.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }


    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("Tag","I'm inside new intent")
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


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tool_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_exit -> {
                Toast.makeText(this, "Exit clicked", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
