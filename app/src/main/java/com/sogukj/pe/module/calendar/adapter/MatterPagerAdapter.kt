package com.sogukj.pe.module.calendar.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Created by admin on 2017/12/7.
 */
class MatterPagerAdapter(fm: FragmentManager,
                         val fragments: List<Fragment>) : FragmentStatePagerAdapter(fm) {
    val titles = arrayListOf("项目关键节点","项目代办事项", "项目完成事项日程")

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence = titles[position]

}