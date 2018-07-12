package com.sogukj.pe.module.project.listingInfo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CanGuBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_common.*

/**
 * Created by qinfei on 17/8/11.
 */
class CanGuActivity : BaseRefreshActivity(), SupportEmptyView {

    lateinit var adapter: RecyclerAdapter<CanGuBean>
    lateinit var project: ProjectBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        setTitle("参股控股")
        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_cangukonggu, parent)
            object : RecyclerHolder<CanGuBean>(convertView) {
                var tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
                var tvRelation = convertView.findViewById<TextView>(R.id.tv_relation) as TextView
                var tvPercent = convertView.findViewById<TextView>(R.id.tv_percent) as TextView
                var tvTouzijine = convertView.findViewById<TextView>(R.id.tv_touzijine) as TextView
                var tvJinlirun = convertView.findViewById<TextView>(R.id.tv_jinlirun) as TextView
                var tvIsMerge = convertView.findViewById<TextView>(R.id.tv_is_merge) as TextView
                var tvBis = convertView.findViewById<TextView>(R.id.tv_bis) as TextView
                override fun setData(view: View, data: CanGuBean, position: Int) {
                    tvName.text = data.name
                    tvRelation.text = Html.fromHtml(getString(R.string.tv_project_cangu_relation, data.relationship))
                    tvPercent.text = Html.fromHtml(getString(R.string.tv_project_cangu_percent, data.participationRatio))
                    tvTouzijine.text = Html.fromHtml(getString(R.string.tv_project_cangu_touzijine, data.investmentAmount))
                    tvJinlirun.text = Html.fromHtml(getString(R.string.tv_project_cangu_jinlirun, data.profit))
                    tvIsMerge.text = Html.fromHtml(getString(R.string.tv_project_cangu_is_merge, data.reportMerge))
                    tvBis.text = Html.fromHtml(getString(R.string.tv_project_cangu_bis, if (!TextUtils.isEmpty(data.mainBusiness)) data.mainBusiness else ""))
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
                .cangu(project.company_id!!)
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
                    SupportEmptyView.checkEmpty(this, adapter)
                    adapter.notifyDataSetChanged()
                    finishRefresh()
                })
    }


    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, CanGuActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
