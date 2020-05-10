package com.arcmce.boogaloo.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.interfaces.ControlListener
import kotlinx.android.synthetic.main.controls_layout.view.*


class ControlsFragment : androidx.fragment.app.Fragment() {

    lateinit var callback: ControlListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is ControlListener) {
            callback = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.controls_layout, container, false)

        view.playButton.setOnClickListener {
            callback.playButtonClick()
        }
        return view

    }

}



