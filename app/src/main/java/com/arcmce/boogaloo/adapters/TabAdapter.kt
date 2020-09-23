package com.arcmce.boogaloo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.arcmce.boogaloo.fragments.CatchUpFragment
import com.arcmce.boogaloo.fragments.LiveFragment
import com.arcmce.boogaloo.fragments.OrderingFragment


class TabAdapter(fm: FragmentManager, internal var tabCount: Int) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return tabCount
    }

    override fun getItem(i: Int): Fragment {
        when (i) {
            0 -> {return LiveFragment()}
            1 -> {return CatchUpFragment()}
            2 -> {return OrderingFragment()}
        }
        throw IllegalStateException("position $i is invalid for this viewpager")
    }
}