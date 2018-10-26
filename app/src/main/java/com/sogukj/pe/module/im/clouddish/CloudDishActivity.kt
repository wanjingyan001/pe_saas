package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.module.dataSource.lawcase.adapter.PagerAdapter
import kotlinx.android.synthetic.main.activity_cloud_dish.*
import kotlinx.android.synthetic.main.white_normal_toolbar.*

/**
 * Created by CH-ZH on 2018/10/25.
 * 加密云盘
 */
class CloudDishActivity : ToolbarActivity(){
    private val titlesInfo = arrayOf("我的文件", "企业文件")
    private var fragments = ArrayList<Fragment>()
    lateinit var mAdapter: PagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_dish)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle("加密云盘")
        toolbar_menu.text = "取消"
        toolbar_menu.setVisible(false)
    }

    private fun initData() {
        fragments.add(CloudMineFileFragment.newInstance(1))
        fragments.add(CloudMineFileFragment.newInstance(2))
        titlesInfo.forEach {
            tabs.addTab(tabs.newTab().setText(it))
        }
        mAdapter = PagerAdapter(supportFragmentManager)

        mAdapter.setFragments(fragments)
        view_pager.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        tabs.getTabAt(0)!!.select()
        view_pager.currentItem = 0
    }

    private fun bindListener() {
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                view_pager.currentItem = tab!!.position
            }

        })

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                tabs.getTabAt(position)!!.select()
            }

        })
    }

    companion object {
        fun invoke(context : Context){
            val intent = Intent(context, CloudDishActivity::class.java)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

}