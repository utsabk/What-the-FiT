package com.example.runningapp.ui.help

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.runningapp.R

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        val textView: TextView = findViewById(R.id.text_help)
        textView.text = getString(R.string.placeholder4HelpActivity)


    }

}