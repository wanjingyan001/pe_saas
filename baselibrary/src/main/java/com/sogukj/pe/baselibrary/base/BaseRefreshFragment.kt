package com.sogukj.pe.baselibrary.base


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sogukj.pe.baselibrary.R
import com.sogukj.pe.baselibrary.interf.SGRefreshListener
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find


/**
 * A simple [Fragment] subclass.
 */
abstract class BaseRefreshFragment : BaseFragment(), SGRefreshListener {
    private var refresh: SmartRefreshLayout? = null
    lateinit var config: RefreshConfig
    private val defaultHeader by lazy { ClassicsHeader(ctx) }
    private val defaultFooter by lazy { BallPulseFooter(ctx) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRefresh()
    }

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        refresh = find(R.id.refresh)
        refresh?.let {
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
            it.setOnRefreshListener {
                doRefresh()
            }
            it.setOnLoadMoreListener {
                doLoadMore()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onDestroyView() {
        super.onDestroyView()
        refresh?.let {
            if (it.refreshHeader != null){
                it.removeView(it.refreshHeader!!.view)
            }
            if (it.refreshFooter != null){
                it.removeView(it.refreshFooter!!.view)
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
        if (refresh != null) {
            refresh?.finishRefresh()
        }
    }

    fun finishLoadMore() {
        if (refresh != null) {
            refresh?.finishLoadMore(0)
        }
    }

    var isLoadMoreEnable: Boolean = RefreshConfig.Default.loadMoreEnable
        get() = if (refresh != null) refresh!!.isEnableLoadMore else field
        set(value) {
            if (refresh != null) {
                field = value
                refresh?.isEnableLoadMore = value
                config.loadMoreEnable = value
            }
        }

    var isRefreshEnable: Boolean = RefreshConfig.Default.refreshEnable
        get() = if (refresh != null) refresh!!.isEnableRefresh else field
        set(value) {
            if (refresh != null) {
                field = value
                refresh?.isEnableRefresh = value
                config.refreshEnable = value
            }
        }
}
