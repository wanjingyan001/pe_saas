package com.sogukj.pe.baselibrary.base

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sogukj.pe.baselibrary.R
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import org.jetbrains.anko.find

abstract class BaseRefreshActivity : ToolbarActivity(), OnRefreshListener, OnLoadMoreListener {
    var refresh: SmartRefreshLayout? = null
        private set


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRefreshConfig()
        initRefresh()
    }


    private fun initRefresh() {
        refresh = find(R.id.refresh)
        refresh?.let {
            val config = initRefreshConfig() ?: RefreshConfig.Default
            it.setDragRate(config.dragRate)
            it.isEnableRefresh = config.refreshEnable
            it.isEnableLoadMore = config.loadMoreEnable
            it.isEnableAutoLoadMore = config.autoLoadMoreEnable
            it.isNestedScrollingEnabled = config.nestedScrollEnable
            it.isEnableOverScrollBounce = config.overScrollBounceEnable
            it.setEnableLoadMoreWhenContentNotFull(config.loadMoreWhenContentNotFull)
            it.setEnableOverScrollDrag(config.overScrollDrag)
            it.setRefreshHeader(initRefreshHeader() ?: ClassicsHeader(this))
            it.setRefreshFooter(initRefreshFooter() ?: ClassicsFooter(this))
            it.setOnRefreshListener(this)
            it.setOnLoadMoreListener(this)
        }
    }


    abstract fun initRefreshConfig(): RefreshConfig?

    abstract fun initRefreshHeader(): RefreshHeader?

    abstract fun initRefreshFooter(): RefreshFooter?

}
