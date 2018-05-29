package com.sogukj.pe.module.project.businessDev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.FinanceHistoryBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_common.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class FinanceHistoryActivity : BaseRefreshActivity(), SupportEmptyView {
    lateinit var adapter: RecyclerAdapter<FinanceHistoryBean>
    lateinit var project: ProjectBean
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        title = "融资历史"

        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_finance_history, parent)
            object : RecyclerHolder<FinanceHistoryBean>(convertView) {

                val tvDate = convertView.findViewById<TextView>(R.id.tv_date) as TextView
                val tvRound = convertView.findViewById<TextView>(R.id.tv_round) as TextView
                val tvValue = convertView.findViewById<TextView>(R.id.tv_value) as TextView
                val tvOrganizationName = convertView.findViewById<TextView>(R.id.tv_organizationName) as TextView

                override fun setData(view: View, data: FinanceHistoryBean, position: Int) {
                    tvDate.text = data.date
                    tvRound.text = data.round
                    tvValue.text = data.value
                    tvOrganizationName.text = data.organizationName
                }

            }
        })
        adapter.onItemClick = { v, p ->
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
//        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

//        val header = ProgressLayout(this)
//        header.setColorSchemeColors(ContextCompat.getColor(this, R.color.color_main))
//        refresh.setHeaderView(header)
//        val footer = BallPulseView(this)
//        footer.setAnimatingColor(ContextCompat.getColor(this, R.color.color_main))
//        refresh.setBottomView(footer)
//        refresh.setOverScrollRefreshShow(false)
//        refresh.setEnableLoadmore(true)
//        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
//            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
//                page = 1
//                doRequest()
//            }
//
//            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
//                ++page
//                doRequest()
//            }
//
//        })
//        refresh.setAutoLoadMore(true)
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    override fun doRefresh() {
        page = 1
        doRequest()
    }

    override fun doLoadMore() {
        ++page
        doRequest()
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }


    var page = 1
    fun doRequest() {
        SoguApi.getService(application, InfoService::class.java)
                .listFinanceHistory(project.company_id!!, page = page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                }, {
                    SupportEmptyView.checkEmpty(this, adapter)
                    isLoadMoreEnable = adapter.dataList.size % 20 == 0
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        finishRefresh()
                    else
                        finishLoadMore()
                })
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, FinanceHistoryActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
