package com.sogukj.pe.module.dataSource.lawcase

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.module.dataSource.lawcase.adapter.PagerAdapter
import kotlinx.android.synthetic.main.activity_law_search.*
import kotlinx.android.synthetic.main.law_search_title.*

/**
 * Created by CH-ZH on 2018/9/10.
 * 法律搜索
 */
class LawSearchResultActivity : ToolbarActivity() {
    private val titles = arrayOf("党内法规", "部门规章","行政法规", "法律","法规规章","行业规定","团体规定","司法解释")
    private var fragments = ArrayList<Fragment>()
    lateinit var mAdapter: PagerAdapter
    private var searchKey : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_law_search)
        searchKey = intent.getStringExtra(Extras.DATA)
        initData()
        bindListener()
    }

    private fun initData() {
        for (i in 0 .. titles.size - 1){
            fragments.add(LawSearchFragment.newInstance(i+1,searchKey))
        }
        mAdapter = PagerAdapter(supportFragmentManager)

        mAdapter.setFragments(fragments)
        view_pager!!.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
        for (i in titles.indices) {
            val tab = tabs.newTab().setCustomView(R.layout.item_tab_rank)
            tabs.addTab(tab)
            var tv = tab.customView?.findViewById(R.id.tv_tab_title) as TextView
            var line = tab.customView?.findViewById(R.id.view_line) as FrameLayout
            tv.text = titles[i]
            if (i == 0){
                tab.select()
                line!!.visibility = View.VISIBLE
            }else{
                line!!.visibility = View.INVISIBLE
            }
        }
        tabs.getTabAt(0)?.select()
        view_pager?.currentItem = 0
        et_search.setText(searchKey)
        et_search.setSelection(searchKey.length)
        if (null != et_search.textStr && et_search.textStr.length > 0){
            iv_del.visibility = View.VISIBLE
        }else{
            iv_del.visibility = View.INVISIBLE
        }
    }

    private fun bindListener() {
        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab!!.customView?.findViewById<FrameLayout>(R.id.view_line)!!.visibility = View.INVISIBLE
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager?.currentItem = tab.position
                tab!!.customView?.findViewById<FrameLayout>(R.id.view_line)!!.visibility = View.VISIBLE
            }

        })
        view_pager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                tabs?.getTabAt(position)?.select()
            }
        })

        iv_search.setOnClickListener {
            Utils.showSoftInputFromWindow(this,et_search)
        }

        toolbar_back.setOnClickListener {
            onBackPressed()
        }
    }

}