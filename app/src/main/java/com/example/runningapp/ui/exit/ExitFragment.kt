package com.example.runningapp.ui.exit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.runningapp.R
import com.example.runningapp.ui.activity.ActivityViewModel

class ExitFragment : Fragment() {

    private lateinit var activityViewModel: ExitViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityViewModel =
            ViewModelProviders.of(this).get(ExitViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_activity, container, false)
        val textView: TextView = root.findViewById(R.id.text_activity)
        activityViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}