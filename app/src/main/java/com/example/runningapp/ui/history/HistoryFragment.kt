package com.example.runningapp.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runningapp.R
import com.example.runningapp.adapters.HistoryAdapter
import kotlinx.android.synthetic.main.fragment_history.*

class HistoryFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_history, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HistoryAdapter(activity!!)

        val viewModel =
            WorkoutViewModelFactory(activity!!.application).create(WorkoutViewModel::class.java)

        viewModel.workoutList.observe(this, Observer { workouts ->
            workouts.let { adapter.collectData(it) }
        })
        history_recycler_view.adapter = adapter
        history_recycler_view.layoutManager = LinearLayoutManager(activity!!.applicationContext)
    }
}