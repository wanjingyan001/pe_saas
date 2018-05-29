package com.sogukj.pe.module.approve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ApprovalBean
import com.sogukj.pe.bean.ApproveFilterBean
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.util.ColorUtil
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_approve_list.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor

/**
 * Created by qinfei on 17/10/18.
 */
class ApproveListActivity : BaseRefreshActivity(), TabLayout.OnTabSelectedListener {
    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (tab?.position == 1) {
            mType = 2
        } else {
            mType = 1
        }
        stateDefault()
    }

    class ItemAdapter(context: Context, creator: (adapter: RecyclerAdapter<ApprovalBean>, parent: ViewGroup, type: Int) -> RecyclerHolder<ApprovalBean>)
        : RecyclerAdapter<ApprovalBean>(context, creator)

    lateinit var adapter: RecyclerAdapter<ApprovalBean>
    lateinit var inflater: LayoutInflater
    var mType: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_list)
        inflater = LayoutInflater.from(this)
        mType = intent.getIntExtra(Extras.TYPE, 1)
        title = intent.getStringExtra(Extras.TITLE)
        setBack(true)
        if (mType == 3) {
            tab_title.visibility = View.GONE
        } else {
            tab_title.visibility = View.VISIBLE
            tab_title.addOnTabSelectedListener(this)
            stateDefault()
        }
        tab_title.visibility = View.GONE
        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_approval, parent)

            object : RecyclerHolder<ApprovalBean>(convertView) {
                val tvTitle = convertView.findViewById<TextView>(R.id.tv_title) as TextView
                val tvType = convertView.findViewById<TextView>(R.id.tv_type) as TextView
                val tvApplicant = convertView.findViewById<TextView>(R.id.tv_applicant) as TextView
                val tvDate = convertView.findViewById<TextView>(R.id.tv_date) as TextView
                val tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
                val tvState = convertView.findViewById<TextView>(R.id.tv_state) as TextView

                override fun setData(view: View, data: ApprovalBean, position: Int) {
                    tvTitle.text = data.title
                    tvType.text = "类别:" + data.kind
                    tvApplicant.text = "申请人:" + data.name
                    val strTime = data.add_time
                    tvTime.visibility = View.GONE
                    if (!TextUtils.isEmpty(strTime)) {
                        val strs = strTime!!.trim().split(" ")
                        if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                            tvTime.visibility = View.VISIBLE
                        }
                        tvDate.text = strs
                                .getOrNull(0)
                        tvTime.text = strs
                                .getOrNull(1)
                    }
                    ColorUtil.setColorStatus(tvState, data)
                }
            }
        })
        adapter.onItemClick = { v, p ->
            val data = adapter.dataList.get(p)
            if (data.type == 2)
                SealApproveActivity.start(this, data, if (mType == 3) 1 else 2)
            else if (data.type == 3)
                SignApproveActivity.start(this, data, if (mType == 3) 1 else 2)
            else if (data.type == 1)
                LeaveBusinessApproveActivity.start(this, data, if (mType == 3) 1 else 2)
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter
        kotlin.run {
            fl_filter.setOnClickListener {
                stateDefault()
            }
            ll_filter.setOnClickListener { }
            iv_filter.setOnClickListener {
                if (fl_filter.visibility == View.VISIBLE) {
                    stateDefault()
                } else {
                    stateFilter()
                }
            }
            search_box.root.backgroundColor = Color.WHITE
            (search_box.tv_cancel as TextView).textColor = Color.parseColor("#a0a4aa")
            search_box.tv_cancel.visibility = View.GONE
            search_box.et_search.hint = "搜索"
            search_box.onTextChange = { text ->
                if (null != text && !TextUtils.isEmpty(text)) {
                    searchStr = text
                    page = 1
                    doRequest()
                } else {
                    searchStr = null
                    page = 1
                    stateDefault()
                }
            }
        }
        stateDefault()
    }

    override fun onResume() {
        super.onResume()
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
        config.autoLoadMoreEnable = true
        config.loadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }

    var filterBean: ApproveFilterBean? = null
    var filterType: Int? = null
    fun stateDefault() {
        page = 1
        fl_filter.visibility = View.GONE
        paramStates.clear()
        paramTemplates.clear()
        doRequest()
    }

    fun stateSearch() {
        page = 1
        fl_filter.visibility = View.GONE
        paramStates.clear()
        paramTemplates.clear()
    }

    fun stateFilter() {
        page = 1
        if (filterBean == null) return
        fl_filter.visibility = View.VISIBLE
        tag_all.setOnClickListener {
            stateDefault()
        }
        rg_category.check(R.id.rb_all)
        setFilterTab(R.id.rb_all)
        rg_category.setOnCheckedChangeListener { group, checkedId ->
            setFilterTab(checkedId)
        }

        btn_reset.setOnClickListener {
            paramTemplates.clear()
            paramStates.clear()
            when (filterType) {
                1 -> setFilterTags(filterBean!!.leave)
                3 -> setFilterTags(filterBean!!.sign)
                2 -> setFilterTags(filterBean!!.approve)
                else -> {
                }
            }

        }
        btn_ok.setOnClickListener {
            fl_filter.visibility = View.GONE
            doRequest()
        }
    }

    private fun setFilterTab(checkedId: Int) {
        if (checkedId == R.id.rb_all) {
            filterType = null
            ll_filter_other.visibility = View.GONE
            setFilterTags(ApproveFilterBean.ItemBean())
        } else if (checkedId == R.id.rb_seal) {
            filterType = 2
            ll_filter_other.visibility = View.VISIBLE
            setFilterTags(filterBean!!.approve)
        } else if (checkedId == R.id.rb_sign) {
            filterType = 3
            setFilterTags(filterBean!!.sign)
            ll_filter_other.visibility = View.VISIBLE
        } else if (checkedId == R.id.rb_leave) {
            filterType = 1
            setFilterTags(filterBean!!.leave)
            ll_filter_other.visibility = View.VISIBLE
        }
    }


    var paramTemplates = ArrayList<String>()
    var paramStates = ArrayList<String>()
    fun setFilterTags(itemBean: ApproveFilterBean.ItemBean?) {
        if (itemBean == null) return
        tags_type.removeAllViews()
        tags_state.removeAllViews()
        val textColor1 = resources.getColor(R.color.white)
        val textColor0 = resources.getColor(R.color.text_1)
        val bgColor0 = R.drawable.bg_tag_filter_0
        val bgColor1 = R.drawable.bg_tag_filter_1
        val onClickTemplate: (View) -> Unit = { v ->
            val tvTag = v as TextView
            val ftag = tvTag.tag as String
            if (paramTemplates.contains(ftag)) {
                paramTemplates.remove(ftag)
                tvTag.setTextColor(textColor0)
                tvTag.setBackgroundResource(bgColor0)
            } else {
                paramTemplates.add(ftag)
                tvTag.setTextColor(textColor1)
                tvTag.setBackgroundResource(bgColor1)
            }
        }

        val onClickStatus: (View) -> Unit = { v ->
            val tvTag = v as TextView
            val ftag = tvTag.tag as String
            if (paramStates.contains(ftag)) {
                paramStates.remove(ftag)
                tvTag.setTextColor(textColor0)
                tvTag.setBackgroundResource(bgColor0)
            } else {
                paramStates.add(ftag)
                tvTag.setTextColor(textColor1)
                tvTag.setBackgroundResource(bgColor1)
            }
        }
        run {
            val itemTag = inflater.inflate(R.layout.item_tag_filter2, null)
            val tvTag = itemTag.findViewById<TextView>(R.id.tv_tag) as TextView
            tvTag.text = "全部"
            tvTag.setOnClickListener(onClickTemplate)
            tags_type.addView(itemTag)
            tvTag.setOnClickListener {
                paramStates.clear()
                paramTemplates.clear()
                fl_filter.visibility = View.GONE
                doRequest()
            }
        }

        run {
            val itemTag = inflater.inflate(R.layout.item_tag_filter2, null)
            val tvTag = itemTag.findViewById<TextView>(R.id.tv_tag) as TextView
            tvTag.text = "全部"
            tvTag.setOnClickListener(onClickTemplate)
            tags_state.addView(itemTag)
            tvTag.setOnClickListener {
                paramStates.clear()
                paramTemplates.clear()
                fl_filter.visibility = View.GONE
                doRequest()
            }
        }
        itemBean.kind?.entries?.forEach { e ->
            val itemTag = inflater.inflate(R.layout.item_tag_filter2, null)
            val tvTag = itemTag.findViewById<TextView>(R.id.tv_tag) as TextView
            tvTag.text = e.value
            tvTag.tag = e.key
            tvTag.setOnClickListener(onClickTemplate)
            tags_type.addView(itemTag)
            if (paramTemplates.contains(e.key)) {
                tvTag.setTextColor(textColor1)
                tvTag.setBackgroundResource(bgColor1)
            } else {
                tvTag.setTextColor(textColor0)
                tvTag.setBackgroundResource(bgColor0)
            }
        }
        itemBean.status?.entries?.forEach { e ->
            val itemTag = inflater.inflate(R.layout.item_tag_filter2, null)
            val tvTag = itemTag.findViewById<TextView>(R.id.tv_tag) as TextView
            tvTag.text = e.value
            tvTag.tag = e.key
            tvTag.setOnClickListener(onClickStatus)
            tags_state.addView(itemTag)
            if (paramStates.contains(e.key)) {
                tvTag.setTextColor(textColor1)
                tvTag.setBackgroundResource(bgColor1)
            } else {
                tvTag.setTextColor(textColor0)
                tvTag.setBackgroundResource(bgColor0)
            }
        }
    }

    var searchStr: String? = null
    var page = 1
    fun doRequest() {
        val templates = if (paramTemplates.isEmpty()) null else paramTemplates.joinToString(",")
        val status = if (paramStates.isEmpty()) null else paramStates.joinToString(",")

        var pro_id: Int? = null
        var status_tyoe: Int? = null
        if (intent.getStringExtra(Extras.TITLE).equals("审批历史")) {
            pro_id = intent.getIntExtra(Extras.ID, 1)
            status_tyoe = null
        } else {
            pro_id = null
            status_tyoe = mType
        }
        if (mType != 4) {
            SoguApi.getService(application, ApproveService::class.java)
                    .listApproval(status = status_tyoe, page = page,
                            fuzzyQuery = searchStr,
                            type = filterType, template_id = templates, filter = status, project_id = pro_id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            if (page == 1)
                                adapter.dataList.clear()
                            payload.payload?.apply {
                                adapter.dataList.addAll(this)
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        SupportEmptyView.checkEmpty(this, adapter)
                    }, {
                        SupportEmptyView.checkEmpty(this, adapter)
                        isLoadMoreEnable = adapter.dataList.size % 20 == 0
                        adapter.notifyDataSetChanged()
                        if (page == 1)
                            finishRefresh()
                        else
                            finishLoadMore()
                    })
        } else if (mType == 4) {
            SoguApi.getService(application, ApproveService::class.java)
                    .showCopy(page = page, template_id = templates)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            if (page == 1)
                                adapter.dataList.clear()
                            payload.payload?.apply {
                                adapter.dataList.addAll(this)
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        //showToast("暂无可用数据")
                        showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                        SupportEmptyView.checkEmpty(this, adapter)
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


        SoguApi.getService(application, ApproveService::class.java)
                .approveFilter()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk && payload.payload != null) {
                        filterBean = payload.payload!!
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }


    companion object {
        fun start(ctx: Activity?, type: Int? = null, id: Int? = null) {
            val intent = Intent(ctx, ApproveListActivity::class.java)
            val title = when (type) {
                1 -> "待我审批"
                2 -> "我已审批"
                3 -> "我发起的"
                4 -> "抄送我的"
                else -> "审批历史"
            }
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.TITLE, title)
            intent.putExtra(Extras.ID, id)
            ctx?.startActivity(intent)
        }
    }
}
