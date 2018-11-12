package com.sogukj.pe.module.project.overview

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.project.ProjectDetailActivity
import com.sogukj.pe.module.project.ProjectNewsActivity
import com.sogukj.pe.service.NewService
import com.sogukj.pe.service.ProjectService
import com.sogukj.pe.widgets.UserRequestListener
import com.sogukj.service.SoguApi
import io.reactivex.internal.util.HalfSerializer.onNext
import kotlinx.android.synthetic.main.activity_more_info.*
import kotlinx.android.synthetic.main.item_opinion_info.view.*
import kotlinx.android.synthetic.main.item_overview_dynamic.view.*
import kotlinx.android.synthetic.main.item_overview_horizontal_list.view.*
import kotlinx.android.synthetic.main.item_project_overview_layout.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.textColor

class MoreInfoActivity : BaseRefreshActivity() {
    private lateinit var infoAdapter: RecyclerAdapter<Any>
    private val listType by extraDelegate(Extras.TYPE, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_info)
        title = when (listType) {
            1 -> "人员负责情况"
            2 -> "新增项目动向"
            3 -> "本周项目工作动向"
            4 -> "本周舆情发生数排行"
            else -> throw  Exception("类型错误")
        }
        setBack(true)
        infoAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            when (listType) {
                1 -> PeopleStateHolder(_adapter.getView(R.layout.item_project_overview_layout, parent))
                2 -> DynamicHolder(_adapter.getView(R.layout.item_overview_dynamic, parent))
                3 -> {
                    val holder = DynamicHolder(_adapter.getView(R.layout.item_overview_dynamic, parent))
                    holder.showSubscript = true
                    holder
                }
                4 -> OpinionHolder(_adapter.getView(R.layout.item_opinion_info, parent))
                else -> throw Exception("类型错误")
            }
        }
        infoAdapter.onItemClick = { v, p ->
            when (listType) {
                1 -> {

                }
            }
        }
        moreInfoList.apply {
            layoutManager = LinearLayoutManager(this@MoreInfoActivity)
            adapter = infoAdapter
            addItemDecoration(DividerItemDecoration(this@MoreInfoActivity, DividerItemDecoration.VERTICAL))
        }
        Glide.with(ctx)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        doRequest()
    }


    override fun doRefresh() {
        doRequest()
    }

    private fun doRequest() {
        val service = SoguApi.getService(application, ProjectService::class.java)
        val observable = when (listType) {
            1 -> {
                service.getPrincipal(more = 1)
            }
            2 -> {
                service.newCompanyAdd(more = 1)
            }
            3 -> {
                service.companyTrends(more = 1)
            }
            4 -> {
                service.companyNewsRank(more = 1)
            }
            else -> throw  Exception("类型错误")
        }
        observable.execute {
            onNext { payload ->
                payload.isOk.yes {
                    payload.payload?.let {
                        when (listType) {
                            2, 3 -> {
                                it as ProjectAdd
                                infoAdapter.refreshData(it.list)
                            }
                            else -> {
                                it as List<Any>
                                infoAdapter.refreshData(it)
                            }
                        }

                    }
                }.otherWise {
                    showErrorToast(payload.message)
                }
            }
            onComplete {
                emptyImg.setVisible(infoAdapter.dataList.isEmpty())
                iv_loading.setVisible(false)
            }
        }
    }

    override fun doLoadMore() {
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.refreshEnable = true
        config.loadMoreEnable = false
        return config
    }

    private fun getSingleDetail(cid: Int) {
        SoguApi.getService(application, NewService::class.java)
                .singleCompany(cid)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                ProjectDetailActivity.start(this@MoreInfoActivity, it)
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    /**
     * 横向viewHolder
     */
    inner class OverviewHolder(item: View) : RecyclerHolder<Company>(item) {
        var isPeopleState = false
        override fun setData(view: View, data: Company, position: Int) {
            if (isPeopleState) {
                view.numTv.textSize = 22f
                view.numTv.textColor = Color.parseColor("#616E82")
                view.unitTv.textColor = Color.parseColor("#616E82")
            } else {
                view.numTv.textSize = 26f
                view.numTv.textColor = Color.parseColor("#3C98E8")
                view.unitTv.textColor = Color.parseColor("#3C98E8")
            }
            view.typeName.text = data.name
            view.numTv.text = data.count.toString()
        }
    }

    /**
     * 人员负责情况一览viewHolder
     */
    inner class PeopleStateHolder(item: View) : RecyclerHolder<Any>(item) {
        override fun setData(view: View, data: Any, position: Int) {
            data as UserProjectInfo
            view.peopleOverviewTitle.setVisible(true)
            view.peopleOverviewTitle.text = Html.fromHtml("${data.name}   共负责<font color='#3C98E8'>${data.count}</font>个项目")

            val adapter = RecyclerAdapter<Company>(ctx) { _adapter, parent, _ ->
                val holder = OverviewHolder(_adapter.getView(R.layout.item_overview_horizontal_list, parent))
                holder.isPeopleState = true
                holder
            }
            adapter.dataList.addAll(data.company)
            view.itemInfoList.apply {
                layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = adapter
                addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.HORIZONTAL))
            }
            adapter.onItemClick = { _, _ ->
                val principalId = data.user_id
                startActivity<PeopleSituationActivity>(Extras.ID to principalId, Extras.NAME to data.name)
            }
            view.itemInfoLayout.clickWithTrigger {
                val principalId = data.user_id
                startActivity<PeopleSituationActivity>(Extras.ID to principalId, Extras.NAME to data.name)
            }
        }
    }

    /**
     * 新增项目动向和本周项目工作动向viewHolder
     */
    inner class DynamicHolder(item: View) : RecyclerHolder<Any>(item) {
        var showSubscript = false
        @SuppressLint("SetTextI18n")
        override fun setData(view: View, data: Any, position: Int) {
            if (null == data) return
            data as NewPro
            if (!data.cname.isNullOrEmpty()) {
                view.projectName.text = data.cname
            }
            if (showSubscript) {
                if (!data.update_time.isNullOrEmpty()) {
                    view.projectTime.text = data.update_time
                }
                view.subscript.setVisible(true)
                view.subscriptTv.setVisible(true)
                if (!data.status.isNullOrEmpty()) {
                    view.subscriptTv.text = data.status
                }
            } else {
                if (!data.add_time.isNullOrEmpty()) {
                    view.projectTime.text = "添加时间：${data.add_time}"
                }
                view.subscript.setVisible(false)
                view.subscriptTv.setVisible(false)
            }
            view.userName.text = data.name
            data.name?.let {
                it.isNotEmpty().yes {
                    Glide.with(ctx).load(data.url).listener(UserRequestListener(view.userHeader, it)).into(view.userHeader)
                }
            }
            view.clickWithTrigger {
                getSingleDetail(data.id)
            }
        }
    }

    /**
     * 本周舆情viewHolder
     */
    inner class OpinionHolder(item: View) : RecyclerHolder<Any>(item) {
        @SuppressLint("SetTextI18n")
        override fun setData(view: View, data: Any, position: Int) {
            data as Opinion
            if (position > 2) {
                view.indexTv.background = null
                view.indexTv.textColor = resources.getColor(R.color.text_1)
            }
            view.indexTv.text = "${position + 1}"
            view.infoTile.text = data.name
            view.infoNumberTv.text = "${data.total}条舆情"
            view.clickWithTrigger {
                ProjectNewsActivity.start(this@MoreInfoActivity, "企业舆情", 2, data.company_id)
            }

        }
    }
}
