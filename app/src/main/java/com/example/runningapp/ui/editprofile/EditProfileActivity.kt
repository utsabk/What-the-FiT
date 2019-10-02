package com.example.runningapp.ui.editprofile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.runningapp.R

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        val textView: TextView = findViewById(R.id.text_editProfile)
        textView.text = getString(R.string.placeholder4EditProfileActivity)
    }

}