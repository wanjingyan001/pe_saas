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
import com.sogukj.pe.bean.CopyRightBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_common.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class CopyrightListActivity : BaseRefreshActivity(), SupportEmptyView {
    lateinit var adapter: RecyclerAdapter<CopyRightBean>
    lateinit var project: ProjectBean
    var type = 1
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)
        type = intent.getIntExtra(Extras.TYPE, 1)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        if (type == 2) {
            title = "著作权"
            adapter = RecyclerAdapter<CopyRightBean>(this, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_project_copyright_2, parent)
                object : RecyclerHolder<CopyRightBean>(convertView) {
                    val tvSimplename = convertView.findViewById<TextView>(R.id.tv_simplename) as TextView
                    val tvCategory = convertView.findViewById<TextView>(R.id.tv_category) as TextView
                    val tvRegnum = convertView.findViewById<TextView>(R.id.tv_regnum) as TextView
                    val tvRegtime = convertView.findViewById<TextView>(R.id.tv_regtime) as TextView
                    val tvFinishTime = convertView.findViewById<TextView>(R.id.tv_finishTime) as TextView
                    val tvPublishtime = convertView.findViewById<TextView>(R.id.tv_publishtime) as TextView
                    override fun setData(view: View, data: CopyRightBean, position: Int) {
                        tvSimplename.text = data.simplename
                        tvCategory.text = "类型：${data.category}"
                        tvRegnum.text = "登记号：${data.regnum}"
                        tvRegtime.text = "登记日期：${data.regtime}"
                        tvFinishTime.text = "完成日期：${data.finishTime}"
                        tvPublishtime.text = "首次发表日期：${data.publishtime}"
                    }

                }
            })
        } else {
            title = "软著权"
            adapter = RecyclerAdapter<CopyRightBean>(this, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_project_copyright_1, parent)
                object : RecyclerHolder<CopyRightBean>(convertView) {
                    val tvFullname = convertView.findViewById<TextView>(R.id.tv_fullname) as TextView
                    val tvRegnum = convertView.findViewById<TextView>(R.id.tv_regnum) as TextView
                    override fun setData(view: View, data: CopyRightBean, position: Int) {

                        tvFullname.text = data.fullname
                        tvRegnum.text = "登记号：${data.regnum}"
                    }

                }
            })
        }
        adapter.onItemClick = { v, p ->
            val data = adapter.dataList[p]
            CopyrightInfoActivity.start(this@CopyrightListActivity, project, data, type)
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
        SoguApi.getService(application, ProjectService::class.java)
                .listCopyright(project.company_id!!, type, page = page)
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
        fun start(ctx: Activity?, project: ProjectBean, type: Int = 1) {
            val intent = Intent(ctx, CopyrightListActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}