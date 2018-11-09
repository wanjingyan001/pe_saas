package com.sogukj.pe.module.project.overview

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.View
import com.bumptech.glide.Glide
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sogukj.pe.Extras

import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.Company
import com.sogukj.pe.bean.NewPro
import com.sogukj.pe.bean.Opinion
import com.sogukj.pe.bean.UserProjectInfo
import com.sogukj.pe.module.project.MainProjectFragment
import com.sogukj.pe.module.project.ProjectDetailActivity
import com.sogukj.pe.module.project.ProjectNewsActivity
import com.sogukj.pe.service.NewService
import com.sogukj.pe.service.ProjectService
import com.sogukj.pe.widgets.UserRequestListener
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.fragment_overview.*
import kotlinx.android.synthetic.main.item_opinion_info.view.*
import kotlinx.android.synthetic.main.item_overview_dynamic.view.*
import kotlinx.android.synthetic.main.item_overview_horizontal_list.view.*
import kotlinx.android.synthetic.main.item_project_overview_layout.view.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.dip
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.textColor


class OverviewFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_overview

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): OverviewFragment {
            val fragment = OverviewFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    private var mParam1: String? = null
    private var mParam2: String? = null
    /**
     * 项目总览adapter
     */
    private lateinit var overviewAdapter: RecyclerAdapter<Company>

    /**
     * 人员负责情况adapter
     */
    private lateinit var peopleStateAdapter: RecyclerAdapter<UserProjectInfo>

    /**
     * 新增项目动态adapter
     */
    private lateinit var dynamicAdapter: RecyclerAdapter<NewPro>

    /**
     * 本周项目工作动向adapter
     */
    private lateinit var weeklyWorkAdapter: RecyclerAdapter<NewPro>

    /**
     * 本周舆情adapter
     */
    private lateinit var opinionAdapter: RecyclerAdapter<Opinion>
    private var fragment: MainProjectFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply {
            fragment = supportFragmentManager.findFragmentByTag("MainProjectFragment") as MainProjectFragment
        }
        initRefresh()
        initOverview()
        initPeopleState()
        initNewProject()
        initWeeklyWork()
        initOpinion()
    }

    override fun onResume() {
        super.onResume()
        getData()

    }


    private fun initRefresh() {
        refresh.isEnableRefresh = true
        refresh.isEnableLoadMore = false
        refresh.setRefreshHeader(ClassicsHeader(ctx))
        refresh.setOnRefreshListener {
            refresh.finishRefresh(2000)
            getData()
        }
    }

    private fun getData() {
        getAllProjectOverview()
        getPrincipal()
        newCompanyAdd()
        companyTrends()
        companyNewsRank()
    }

    private fun initOverview() {
        projectInfoLayout.peopleOverviewTitle.setVisible(false)
        overviewAdapter = RecyclerAdapter(ctx) { _adapter, parent, _ ->
            OverviewHolder(_adapter.getView(R.layout.item_overview_horizontal_list, parent))
        }
        overviewAdapter.onItemClick = { _, p ->
            fragment?.selectTab(p + 1)
        }
        projectInfoLayout.itemInfoList.setPadding(dip(15), 0, dip(15), 0)
        projectInfoLayout.itemInfoList.apply {
            layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
            adapter = overviewAdapter
            addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.HORIZONTAL))
        }
    }


    private fun getAllProjectOverview() {
        SoguApi.getService(baseActivity!!.application, ProjectService::class.java)
                .getAllProjectOverview()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                overviewAdapter.refreshData(it)
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    private fun initPeopleState() {
        peopleStateAdapter = RecyclerAdapter(ctx) { _adapter, parent, _ ->
            PeopleStateHolder(_adapter.getView(R.layout.item_project_overview_layout, parent))
        }
        peopleStateList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = peopleStateAdapter
            addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        }
        showMore.clickWithTrigger {
            startActivity<MoreInfoActivity>(Extras.TYPE to 1)
        }
    }

    private fun getPrincipal() {
        SoguApi.getService(baseActivity!!.application, ProjectService::class.java)
                .getPrincipal()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                peopleStateAdapter.refreshData(it)
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        showMore.setVisible(peopleStateAdapter.dataList.size >= 3)
                    }
                }
    }


    private fun initNewProject() {
        dynamicAdapter = RecyclerAdapter(ctx) { _adapter, parent, _ ->
            DynamicHolder(_adapter.getView(R.layout.item_overview_dynamic, parent))
        }
        dynamicList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = dynamicAdapter
            addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        }
        dynamicAdapter.onItemClick = { v, p ->
            val pro = dynamicAdapter.dataList[p]
            getSingleDetail(pro.id)
        }
        showMore2.clickWithTrigger {
            startActivity<MoreInfoActivity>(Extras.TYPE to 2)
        }
    }

    private fun newCompanyAdd() {
        SoguApi.getService(baseActivity!!.application, ProjectService::class.java)
                .newCompanyAdd()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                val str = "本周新增入库项目：${it.count}个"
                                val spa = SpannableString(str)
                                spa.setSpan(AbsoluteSizeSpan(24, true), 9, str.length - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                if (null != newProjectNumber){
                                    newProjectNumber.text = spa
                                }
                                dynamicAdapter.refreshData(it.list)
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        showMore2.setVisible(dynamicAdapter.dataList.size >= 3)
                    }
                }
    }


    private fun initWeeklyWork() {
        weeklyWorkAdapter = RecyclerAdapter(ctx) { _adapter, parent, _ ->
            val holder = DynamicHolder(_adapter.getView(R.layout.item_overview_dynamic, parent))
            holder.showSubscript = true
            holder
        }
        weeklyWorkAdapter.onItemClick = { v, p ->
            val pro = weeklyWorkAdapter.dataList[p]
            getSingleDetail(pro.id)
        }
        weeklyWorkList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = weeklyWorkAdapter
            addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        }
        showMore3.clickWithTrigger {
            startActivity<MoreInfoActivity>(Extras.TYPE to 3)
        }
    }

    private fun companyTrends() {
        SoguApi.getService(baseActivity!!.application, ProjectService::class.java)
                .companyTrends()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                weeklyWorkAdapter.refreshData(it.list)
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        showMore3.setVisible(weeklyWorkAdapter.dataList.size >= 3)
                    }
                }
    }

    private fun initOpinion() {
        opinionAdapter = RecyclerAdapter(ctx) { _adapter, parent, _ ->
            OpinionHolder(_adapter.getView(R.layout.item_opinion_info, parent))
        }
        weeklyOpinionList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = opinionAdapter
            addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        }
        opinionAdapter.onItemClick = {v,p->
            val opinion = opinionAdapter.dataList[p]
            ProjectNewsActivity.start(baseActivity!!, "企业舆情", 2, opinion.company_id)
        }
        showMore4.clickWithTrigger {
            startActivity<MoreInfoActivity>(Extras.TYPE to 4)
        }
    }

    private fun companyNewsRank() {
        SoguApi.getService(baseActivity!!.application, ProjectService::class.java)
                .companyNewsRank()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                opinionAdapter.refreshData(it)
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        showMore4.setVisible(opinionAdapter.dataList.size >= 3)
                    }
                }
    }


    private fun getSingleDetail(cid: Int) {
        SoguApi.getService(baseActivity!!.application, NewService::class.java)
                .singleCompany(cid)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                ProjectDetailActivity.start(activity, it)
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
    inner class PeopleStateHolder(item: View) : RecyclerHolder<UserProjectInfo>(item) {
        override fun setData(view: View, data: UserProjectInfo, position: Int) {
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
    inner class DynamicHolder(item: View) : RecyclerHolder<NewPro>(item) {
        var showSubscript = false
        @SuppressLint("SetTextI18n")
        override fun setData(view: View, data: NewPro, position: Int) {
            view.projectName.text = data.cname
            if (showSubscript) {
                view.projectTime.text = data.update_time
                view.subscript.setVisible(true)
                view.subscriptTv.setVisible(true)
                view.subscriptTv.text = data.status
            } else {
                view.projectTime.text = "添加时间:${data.add_time}"
                view.subscript.setVisible(false)
                view.subscriptTv.setVisible(false)
            }
            view.userName.text = data.name
            data.name?.let {
                it.isNotEmpty().yes {
                    Glide.with(ctx).load(data.url).listener(UserRequestListener(view.userHeader, it)).into(view.userHeader)
                }
            }
        }
    }

    /**
     * 本周舆情viewHolder
     */
    inner class OpinionHolder(item: View) : RecyclerHolder<Opinion>(item) {
        @SuppressLint("SetTextI18n")
        override fun setData(view: View, data: Opinion, position: Int) {
            view.indexTv.text = "${position + 1}"
            view.infoTile.text = data.name
            view.infoNumberTv.text = "${data.total}条舆情"
        }
    }
}
