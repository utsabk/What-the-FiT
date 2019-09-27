package com.example.runningapp.ui.heartrate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.runningapp.R

class HeartRateFragment : Fragment() {

    private lateinit var heartRateViewModel: HeartRateViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        heartRateViewModel =
            ViewModelProviders.of(this).get(HeartRateViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_heartrate, container, false)
        val textView: TextView = root.findViewById(R.id.text_heartrate)
        heartRateViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}