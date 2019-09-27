package com.example.runningapp.ui.editprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.runningapp.R

class EditProfileFragment : Fragment() {

    private lateinit var bmiViewModel: EditProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bmiViewModel =
            ViewModelProviders.of(this).get(EditProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_editprofile, container, false)
        val textView: TextView = root.findViewById(R.id.text_editProfile)
        bmiViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}