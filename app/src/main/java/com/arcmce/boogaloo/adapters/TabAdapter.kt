package com.arcmce.boogaloo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.arcmce.boogaloo.fragments.CatchUpFragment
import com.arcmce.boogaloo.fragments.LiveFragment


class TabAdapter(fm: androidx.fragment.app.FragmentManager, internal var tabCount: Int) :
    androidx.fragment.app.FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return tabCount
    }

    override fun getItem(i: Int): androidx.fragment.app.Fragment? {
        when (i) {
            0 -> {return LiveFragment()}
            1 -> {return CatchUpFragment()}
            else -> return null
        }
    }
}