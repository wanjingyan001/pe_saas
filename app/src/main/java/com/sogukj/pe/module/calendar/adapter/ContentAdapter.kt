package com.sogukj.pe.module.calendar.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup

/**
 * Created by admin on 2017/12/5.
 */
class ContentAdapter(fm: FragmentManager,
                     val fragments: List<Fragment>,
                     val titles: List<String>) : FragmentStatePagerAdapter(fm) {
    private lateinit var currentFragment: Fragment

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence = titles[position]

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        currentFragment = obj as Fragment
        super.setPrimaryItem(container, position, obj)
    }

    fun getCurrentItem() = currentFragment
}