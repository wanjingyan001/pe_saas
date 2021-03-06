package com.sogukj.pe.module.project.intellectualProperty

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
import com.sogukj.pe.bean.PatentBean
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
class PatentListActivity : BaseRefreshActivity() {
    lateinit var adapter: RecyclerAdapter<PatentBean>
    lateinit var project: ProjectBean
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        title = "专利信息"

        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_patent, parent)
            object : RecyclerHolder<PatentBean>(convertView) {
                val tvPatentName = convertView.findViewById<TextView>(R.id.tv_patentName) as TextView
                val tvApplicationPublishNum = convertView.findViewById<TextView>(R.id.tv_applicationPublishNum) as TextView
                val tvPatentNum = convertView.findViewById<TextView>(R.id.tv_patentNum) as TextView
                override fun setData(view: View, data: PatentBean, position: Int) {
                    tvPatentName.text = data.patentName
                    tvApplicationPublishNum.text = "申请公布号：${data.applicationPublishNum}"
                    tvPatentNum.text = "申请号/专利号：${data.patentNum}"
                }

            }
        })
        adapter.onItemClick = { v, p ->
            val data = adapter.dataList[p]
            PatentInfoActivity.start(this@PatentListActivity, project, data)
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
                .listPatent(project.company_id!!, page = page)
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
                    finishLoad(page)
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
            val intent = Intent(ctx, PatentListActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}