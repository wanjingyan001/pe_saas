package com.sogukj.pe.baselibrary.base

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sogukj.pe.baselibrary.R
import com.sogukj.pe.baselibrary.interf.SGRefreshListener
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find

abstract class BaseRefreshActivity : ToolbarActivity(), SGRefreshListener {
    lateinit var refresh: SmartRefreshLayout
        private set
    lateinit var config: RefreshConfig
    protected val defaultHeader by lazy { ClassicsHeader(this) }
    protected val defaultFooter by lazy { BallPulseFooter(this) }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        initRefresh()
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        initRefresh()
    }

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        refresh = find(R.id.refresh)
        if (this::refresh.isLateinit) {
            refresh.let {
                config = initRefreshConfig() ?: RefreshConfig.Default
                it.setDragRate(config.dragRate)
                it.isEnableRefresh = config.refreshEnable
                it.isEnableLoadMore = config.loadMoreEnable
                it.isEnableAutoLoadMore = config.autoLoadMoreEnable
                it.isNestedScrollingEnabled = config.nestedScrollEnable
                it.isEnableOverScrollBounce = config.overScrollBounceEnable
                it.setDisableContentWhenLoading(config.disableContentWhenLoading)
                it.setDisableContentWhenRefresh(config.disableContentWhenRefresh)
                it.setEnableLoadMoreWhenContentNotFull(config.loadMoreWhenContentNotFull)
                it.setEnableOverScrollDrag(config.overScrollDrag)
                it.setEnableFooterFollowWhenLoadFinished(config.footerFollowWhenLoadFinished)
                val header = initRefreshHeader()
                if (header == null) {
                    it.setRefreshHeader(ClassicsHeader(this), 0, 0)
                } else {
                    it.setRefreshHeader(header)
                }
                val footer = initRefreshFooter()
                if (footer == null) {
                    it.setRefreshFooter(ClassicsFooter(this), 0, 0)
                } else {
                    it.setRefreshFooter(footer)
                }
                it.setOnRefreshListener {
                    doRefresh()
                    refresh.finishRefresh(1000)
                }
                it.setOnLoadMoreListener {
                    doLoadMore()
                    refresh.finishLoadMore(1000)
                }
            }
        }
    }


    abstract fun initRefreshConfig(): RefreshConfig?

    open fun initRefreshHeader(): RefreshHeader? = defaultHeader

    open fun initRefreshFooter(): RefreshFooter? {
        defaultFooter.setIndicatorColor(Color.parseColor("#7BB4FC"))
        defaultFooter.setAnimatingColor(Color.parseColor("#7BB4FC"))
        return defaultFooter
    }

    fun finishRefresh() {
        if (this::refresh.isLateinit && refresh.isRefreshing) {
            refresh.finishRefresh()
        }
    }

    fun finishLoadMore() {
        if (this::refresh.isLateinit && refresh.isLoading) {
            refresh.finishLoadMore()
        }
    }

    var isLoadMoreEnable: Boolean = RefreshConfig.Default.loadMoreEnable
        get() = if (this::refresh.isLateinit) refresh.isEnableLoadMore else field
        set(value) {
            if (this::refresh.isLateinit) {
                field = value
                refresh.isEnableLoadMore = value
                config.loadMoreEnable = value
            }
        }

    var isRefreshEnable: Boolean = RefreshConfig.Default.refreshEnable
        get() = if (this::refresh.isLateinit) refresh.isEnableRefresh else field
        set(value) {
            if (this::refresh.isLateinit) {
                field = value
                refresh.isEnableRefresh = value
                config.refreshEnable = value
            }
        }
}
