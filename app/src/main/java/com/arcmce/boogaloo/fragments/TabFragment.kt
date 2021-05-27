package com.arcmce.boogaloo.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.adapters.TabAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.tab_layout.*

class TabFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_layout, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("TBF", "onViewCreated")


        tab_layout.addTab(tab_layout.newTab().setText("Live"))
        tab_layout.addTab(tab_layout.newTab().setText("Catch Up"))
        tab_layout.tabGravity = TabLayout.GRAVITY_FILL
        tab_layout.tabMode = TabLayout.MODE_FIXED

        val tabsAdapter = TabAdapter(childFragmentManager, tab_layout.tabCount)
        view_pager.adapter = tabsAdapter

        view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout))
        tab_layout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager.currentItem = tab.position
            }
            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }
        })

    }
}