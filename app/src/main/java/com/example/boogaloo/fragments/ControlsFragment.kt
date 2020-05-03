package com.example.boogaloo.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.boogaloo.R
import com.example.boogaloo.interfaces.ControlListener
import kotlinx.android.synthetic.main.controls_layout.view.*


class ControlsFragment : Fragment() {

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



