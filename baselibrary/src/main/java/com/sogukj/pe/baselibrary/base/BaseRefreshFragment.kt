package com.sogukj.pe.baselibrary.base


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener

import com.sogukj.pe.baselibrary.R
import com.sogukj.pe.baselibrary.interf.SGRefreshListener
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find


/**
 * A simple [Fragment] subclass.
 */
abstract class BaseRefreshFragment : BaseFragment(),SGRefreshListener {
    lateinit var refresh: SmartRefreshLayout
        private set
    lateinit var config: RefreshConfig
    protected val defaultHeader by lazy { ClassicsHeader(ctx) }
    protected val defaultFooter by lazy { ClassicsFooter(ctx) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRefresh()
    }

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
                    it.setRefreshHeader(ClassicsHeader(ctx), 0, 0)
                } else {
                    it.setRefreshHeader(header)
                }
                val footer = initRefreshFooter()
                if (footer == null) {
                    it.setRefreshFooter(ClassicsFooter(ctx), 0, 0)
                } else {
                    it.setRefreshFooter(footer)
                }
                it.setOnRefreshListener{
                    doRefresh()
                }
                it.setOnLoadMoreListener{
                    doLoadMore()
                }
            }
        }
    }


    abstract fun initRefreshConfig(): RefreshConfig?

    open fun initRefreshHeader(): RefreshHeader? = defaultHeader

    open fun initRefreshFooter(): RefreshFooter? = defaultFooter

    fun finishRefresh() {
        if (this::refresh.isLateinit) {
            refresh.finishRefresh()
        }
    }

    fun finishLoadMore() {
        if (this::refresh.isLateinit) {
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
            if (this::refresh.isLateinit){
                field = value
                refresh.isEnableRefresh = value
                config.refreshEnable = value
            }
        }
}
