package com.sogukj.pe.module.hotpost

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.HotPostInfo
import com.sogukj.pe.module.dataSource.DocumentType
import com.sogukj.pe.module.dataSource.DocumentsListActivity
import com.sogukj.pe.module.dataSource.HotPostAdapter
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_hot_post.*

/**
 * Created by CH-ZH on 2018/9/6.
 */
class HotPostActivity : BaseRefreshActivity() {
    private var infos = ArrayList<HotPostInfo>()
    private lateinit var listAdapter: HotPostAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hot_post)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        title = "热门行业研报"
        setBack(true)
        initView()
        initData()
    }

    private fun initView() {
        hot_recycle.layoutManager = GridLayoutManager(this, 2)
        hot_recycle.itemAnimator = DefaultItemAnimator()
        hot_recycle.addItemDecoration(GridSpacingItemDecoration(this, 2))
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
    }

    private fun initData() {
        listAdapter = HotPostAdapter(infos)
        listAdapter.setSpanSizeLookup { _, position ->
            if (position == 0) {
                return@setSpanSizeLookup 2
            } else {
                return@setSpanSizeLookup 1
            }
        }
        hot_recycle.adapter = listAdapter
        listAdapter.setOnItemClickListener { adapter, view, position ->
            val info = infos[position]
            val intent = Intent(this, DocumentsListActivity::class.java)
            intent.putExtra(Extras.TYPE, DocumentType.INDUSTRY_REPORTS)
            intent.putExtra(Extras.TYPE1, info.id)
            intent.putExtra(Extras.NAME, info.name)
            startActivity(intent)
        }
        setLoadding()
        getData()
    }

    private fun setLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    private fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }

    private fun getData() {
        SoguApi.getService(application, DataSourceService::class.java)
                .getHotReport()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                it.forEachIndexed { index, hotPostInfo ->
                                    if (hotPostInfo.name == "全部行业") {
                                        hotPostInfo.setType(HotPostInfo.header)
                                    } else {
                                        hotPostInfo.setType(HotPostInfo.item)
                                    }
                                }
                                infos.addAll(it)
                                listAdapter.notifyDataSetChanged()
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                        goneLoadding()
                    }
                    onComplete {
                        goneLoadding()
                    }

                    onError {
                        it.printStackTrace()
                        goneLoadding()
                    }
                }
    }


    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.refreshEnable = false
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

    companion object {
        fun invoke(context: Context) {
            var intent = Intent(context, HotPostActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}