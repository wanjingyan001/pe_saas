package com.sogukj.pe.module.hotpost

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.HotPostInfo
import com.sogukj.pe.module.hotpost.adapter.HotPostAdapter
import kotlinx.android.synthetic.main.activity_hot_post.*

/**
 * Created by CH-ZH on 2018/9/6.
 */
class HotPostActivity : BaseRefreshActivity() {
    private var infos = ArrayList<HotPostInfo>()
    private var adapter : HotPostAdapter ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hot_post)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        title = "热门行业研报"
        setBack(true)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        hot_recycle.layoutManager = GridLayoutManager(this,2,GridLayoutManager.VERTICAL,false)
        hot_recycle.itemAnimator = DefaultItemAnimator()
        hot_recycle.addItemDecoration(GridSpacingItemDecoration(this,2))
    }

    private fun initData() {
        for (i in 0 .. 21){
            val hotPostInfo = HotPostInfo()
            hotPostInfo.image = "http://guwan-sg.oss-cn-hangzhou.aliyuncs.com/homePageIcon/BannerImage/kuaizhangxingqiu.png"
            hotPostInfo.title = "金融服务"+i
            infos.add(hotPostInfo)
        }
        adapter = HotPostAdapter(this,hot_recycle,infos)
        hot_recycle.adapter = adapter
        adapter!!.notifyDataSetChanged()
    }

    private fun bindListener() {
        adapter!!.setOnHeaderClickListener {
            showErrorToast(it.toString())
        }

        adapter!!.setOnItemClickListener {
            showErrorToast(it.toString())
        }

    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = false
        config.autoLoadMoreEnable = false
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun doRefresh() {
        if (this::refresh.isLateinit && refresh.isRefreshing) {
            refresh.finishRefresh()
        }
    }

    override fun doLoadMore() {

    }

    companion object {
        fun invoke(context: Context){
            var intent = Intent(context, HotPostActivity::class.java)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}