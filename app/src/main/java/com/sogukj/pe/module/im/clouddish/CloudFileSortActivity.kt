package com.sogukj.pe.module.im.clouddish

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.activity_cloud_sort.*

/**
 * Created by CH-ZH on 2018/10/31.
 */
class CloudFileSortActivity : BaseRefreshActivity() {
    private var dir : String = ""
    private var sort : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_sort)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setTitle("分类")
        setBack(true)
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        dir = intent.getStringExtra(Extras.TITLE)
        sort = intent.getStringExtra(Extras.SORT)
    }

    private fun initData() {
        tv_dir.text = "${dir}中的所有${sort}"
    }

    private fun bindListener() {

    }

    private fun showLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    private fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }

    private fun showEmpty(){
        fl_empty.visibility = View.VISIBLE
    }

    private fun goneEmpty(){
        fl_empty.visibility = View.INVISIBLE
    }

    override fun initRefreshConfig(): RefreshConfig?{
        val config = RefreshConfig()
        config.loadMoreEnable = false
        config.autoLoadMoreEnable = false
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun doRefresh() {

    }

    override fun doLoadMore() {

    }

    fun dofinishRefresh() {
        if (this::refresh.isLateinit && refresh.isRefreshing) {
            refresh.finishRefresh()
        }
        goneLoadding()
    }
}