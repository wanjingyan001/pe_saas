package com.sogukj.pe.module.project.listingInfo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
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
import com.sogukj.pe.bean.BonusBean
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
class BonusInfoActivity : BaseRefreshActivity(), SupportEmptyView {

    lateinit var adapter: RecyclerAdapter<BonusBean>
    lateinit var project: ProjectBean
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        title = "分红情况"

        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_bonus_info, parent)
            object : RecyclerHolder<BonusBean>(convertView) {


                val tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
                val tvBoardDate = convertView.findViewById<TextView>(R.id.tv_boardDate) as TextView
                val tvShareholderDate = convertView.findViewById<TextView>(R.id.tv_shareholderDate) as TextView
                val tvImplementationDate = convertView.findViewById<TextView>(R.id.tv_implementationDate) as TextView
                val tvIntroduction = convertView.findViewById<TextView>(R.id.tv_introduction) as TextView
                val tvAsharesDate = convertView.findViewById<TextView>(R.id.tv_asharesDate) as TextView
                val tvAcuxiDate = convertView.findViewById<TextView>(R.id.tv_acuxiDate) as TextView
                val tvAdividendDate = convertView.findViewById<TextView>(R.id.tv_adividendDate) as TextView
                val tvProgress = convertView.findViewById<TextView>(R.id.tv_progress) as TextView
                val tvDividendRate = convertView.findViewById<TextView>(R.id.tv_dividendRate) as TextView


                override fun setData(view: View, data: BonusBean, position: Int) {
                    tvTime.text = data.boardDate
                    tvBoardDate.text = data.boardDate
                    tvShareholderDate.text = data.shareholderDate
                    tvImplementationDate.text = data.implementationDate
                    tvIntroduction.text = data.introduction
                    tvAsharesDate.text = data.asharesDate
                    tvAcuxiDate.text = data.acuxiDate
                    tvAdividendDate.text = data.adividendDate
                    tvProgress.text = data.progress
                    tvDividendRate.text = data.dividendRate
                }

            }
        })
        adapter.onItemClick = { v, p ->
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

        handler.postDelayed({
            doRequest()
        }, 100)
    }

    override fun doRefresh() {
        doRequest()
    }

    override fun doLoadMore() {
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = false
        config.autoLoadMoreEnable = false
        config.disableContentWhenRefresh = true
        return config
    }

    fun doRequest() {
        SoguApi.getService(application,InfoService::class.java)
                .listBonusInfo(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    adapter.dataList.clear()
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, {
                    SupportEmptyView.checkEmpty(this,adapter)
                    adapter.notifyDataSetChanged()
                  finishRefresh()
                })
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, BonusInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
