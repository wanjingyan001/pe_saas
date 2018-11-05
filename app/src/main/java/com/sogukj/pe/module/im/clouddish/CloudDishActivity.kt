package com.sogukj.pe.module.im.clouddish

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.fromJson
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.BatchRemoveBean
import com.sogukj.pe.bean.MechanismBasicInfo
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
    private var invokeType = 1 // 1:加密云盘按钮跳转 2：保存到云盘
    private var path = ""
    private var company = ""
    private var previousPath = "" //文件复制前的目录
    private var fileName = "" //要复制的文件名
    private var isCopy = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_dish)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        invokeType = intent.getIntExtra("invokeType",1)
        path = intent.getStringExtra("path")
        isCopy = intent.getBooleanExtra("isCopy",false)
        previousPath = intent.getStringExtra("previousPath")
        fileName = intent.getStringExtra("fileName")
        setBack(true)
        setTitle("加密云盘")
        toolbar_menu.text = "取消"
        toolbar_menu.setVisible(false)
        val companyInfo = sp.getString(Extras.SAAS_BASIC_DATA, "")
        val detail = Gson().fromJson<MechanismBasicInfo?>(companyInfo)
        if (null != detail){
            company = detail.mechanism_name?:""
        }
        if (null == previousPath){
            previousPath = ""
        }
        if (null == fileName){
            fileName = ""
        }
    }

    private fun initData() {
        fragments.add(CloudMineFileFragment.newInstance(1,invokeType,path,"/我的文件",
                true,false,isCopy,fileName,previousPath,BatchRemoveBean()))
        fragments.add(CloudMineFileFragment.newInstance(2,invokeType,path,"/${company}",
                true,true,isCopy,fileName,previousPath,BatchRemoveBean()))
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
        fun invoke(context : Context,invokeType:Int,path:String){
            val intent = Intent(context, CloudDishActivity::class.java)
            intent.putExtra("invokeType",invokeType)
            intent.putExtra("path",path)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        fun invokeForResult(context : Activity,invokeType:Int,requestCode : Int,path:String){
            val intent = Intent(context, CloudDishActivity::class.java)
            intent.putExtra("invokeType",invokeType)
            intent.putExtra("path",path)
            context.startActivityForResult(intent,requestCode)
        }

        fun invoke(context : Context,invokeType:Int,path:String,isCopy:Boolean,fileName:String,previousPath:String){
            val intent = Intent(context, CloudDishActivity::class.java)
            intent.putExtra("invokeType",invokeType)
            intent.putExtra("path",path)
            intent.putExtra("isCopy",isCopy)
            intent.putExtra("fileName",fileName)
            intent.putExtra("previousPath",previousPath)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}