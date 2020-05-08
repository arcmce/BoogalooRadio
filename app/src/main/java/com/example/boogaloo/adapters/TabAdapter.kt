package com.example.boogaloo.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.boogaloo.fragments.CatchUpFragment
import com.example.boogaloo.fragments.LiveFragment


class TabAdapter(fm: FragmentManager, internal var tabCount: Int) :
    FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return tabCount
    }

    override fun getItem(i: Int): Fragment? {
        when (i) {
            0 -> {return LiveFragment()}
            1 -> {return CatchUpFragment()}
            else -> return null
        }
    }
}