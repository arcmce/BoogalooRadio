package com.arcmce.boogaloo.adapters

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.arcmce.boogaloo.fragments.CatchUpFragment
import com.arcmce.boogaloo.fragments.LiveFragment


class TabAdapter(fm: FragmentManager, internal var tabCount: Int) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var liveFragment: LiveFragment? = null

    override fun getCount(): Int {
        return tabCount
    }

    override fun getItem(i: Int): Fragment {
        when (i) {
            0 -> {
                return LiveFragment()
            }
            1 -> {
                return CatchUpFragment()
            }
        }
        throw IllegalStateException("position $i is invalid for this viewpager")
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment

        // save the appropriate reference depending on position
        when (position) {
            0 -> {
                liveFragment = fragment as LiveFragment
            }
        }

        return fragment
    }
}
