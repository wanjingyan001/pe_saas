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
import com.sogukj.pe.baselibrary.interf.SGRefreshListener
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find

abstract class BaseRefreshActivity : ToolbarActivity(),SGRefreshListener{
    lateinit var refresh: SmartRefreshLayout
        private set


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    abstract fun initRefreshHeader(): RefreshHeader?

    abstract fun initRefreshFooter(): RefreshFooter?

}
