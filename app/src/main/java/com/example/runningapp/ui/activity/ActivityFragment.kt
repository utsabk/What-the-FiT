package com.example.runningapp.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.runningapp.R

class ActivityFragment : Fragment() {

    private lateinit var activityViewModel: ActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityViewModel =
            ViewModelProviders.of(this).get(ActivityViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_activity, container, false)
        val textView: TextView = root.findViewById(R.id.text_activity)
        activityViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}