package com.sogukj.pe.module.calendar.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Created by admin on 2017/12/5.
 */
class ContentAdapter(fm: FragmentManager,
                     val fragments: List<Fragment>,
                     val titles: List<String>) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence  = titles[position]
}