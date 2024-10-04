package com.arcmce.boogaloo.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arcmce.boogaloo.R
import com.arcmce.boogaloo.adapters.TabAdapter
import com.arcmce.boogaloo.databinding.TabLayoutBinding
import com.google.android.material.tabs.TabLayout
////import kotlinx.android.synthetic.main.tab_layout.*

class TabFragment : androidx.fragment.app.Fragment() {

    var tabsAdapter: TabAdapter? = null

    private lateinit var binding: TabLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val view = inflater.inflate(R.layout.tab_layout, container, false)
//
//        return view

        binding = TabLayoutBinding.inflate(inflater, container, false)  // Inflate using binding
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("TBF", "onViewCreated")


        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Live"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Catch Up"))
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        binding.tabLayout.tabMode = TabLayout.MODE_FIXED

        tabsAdapter = TabAdapter(childFragmentManager, binding.tabLayout.tabCount)
        binding.viewPager.adapter = tabsAdapter

        binding.viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout))
        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPager.currentItem = tab.position
            }
            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }
        })

    }
}