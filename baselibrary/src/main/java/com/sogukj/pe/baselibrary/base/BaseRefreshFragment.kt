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
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find


/**
 * A simple [Fragment] subclass.
 */
abstract class BaseRefreshFragment : BaseFragment(), OnRefreshListener, OnLoadMoreListener {
    lateinit var refresh: SmartRefreshLayout
        private set

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRefresh()
    }

    private fun initRefresh() {
        refresh = find(R.id.refresh)
        if (this::refresh.isLateinit) {
            refresh.let {
                val config = initRefreshConfig() ?: RefreshConfig.Default
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
                it.setOnRefreshListener(this)
                it.setOnLoadMoreListener(this)
            }
        }
    }


    abstract fun initRefreshConfig(): RefreshConfig?

    abstract fun initRefreshHeader(): RefreshHeader?

    abstract fun initRefreshFooter(): RefreshFooter?

}
