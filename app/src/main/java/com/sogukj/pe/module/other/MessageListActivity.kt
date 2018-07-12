package com.sogukj.pe.module.other

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ApproveFilterBean
import com.sogukj.pe.bean.MessageBean
import com.sogukj.pe.bean.MessageIndexBean
import com.sogukj.pe.module.approve.ApproveListActivity
import com.sogukj.pe.module.approve.LeaveBusinessApproveActivity
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.util.ColorUtil
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_common.*
import org.jetbrains.anko.singleLine
import org.jetbrains.anko.textColor

class MessageListActivity : BaseRefreshActivity() {
    lateinit var inflater: LayoutInflater
    lateinit var bean: MessageIndexBean
    lateinit var adapter: RecyclerAdapter<MessageBean>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)
        bean = intent.getSerializableExtra(Extras.DATA) as MessageIndexBean
        if (bean.flag == 1) {
            title = "审批消息助手"
        } else if (bean.flag == 2) {
            title = "系统消息助手"
            iv_filter.visibility = View.INVISIBLE
        }
        setBack(true)
        inflater = LayoutInflater.from(this)
        adapter = RecyclerAdapter<MessageBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_msg_content, parent)
            object : RecyclerHolder<MessageBean>(convertView) {
                val tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
                val tvTitle = convertView.findViewById<TextView>(R.id.tv_title) as TextView
                val tvNum = convertView.findViewById<TextView>(R.id.tv_num) as TextView
                val tvState = convertView.findViewById<TextView>(R.id.tv_state) as TextView
                val tvFrom = convertView.findViewById<TextView>(R.id.tv_from) as TextView
                val tvType = convertView.findViewById<TextView>(R.id.tv_type) as TextView
                val tvMsg = convertView.findViewById<TextView>(R.id.tv_msg) as TextView
                val tvUrgent = convertView.findViewById<TextView>(R.id.tv_urgent) as TextView
                val ll_content = convertView.findViewById<LinearLayout>(R.id.ll_content)
                val ivIcon = convertView.findViewById<ImageView>(R.id.icon) as ImageView
                val ivPoint = convertView.findViewById<ImageView>(R.id.point) as ImageView
                override fun setData(view: View, data: MessageBean, position: Int) {

                    if (bean.flag == 1) {
                        val strType = when (data.type) {
                            1 -> "出勤休假"
                            2 -> "用印审批"
                            3 -> "签字审批"
                            else -> ""
                        }
                        if (data.status == 1) {
                            ll_content.setBackgroundResource(R.drawable.bg_pop_msg_left_1)
                        } else {
                            ll_content.setBackgroundResource(R.drawable.bg_pop_msg_left)
                        }
                        ivIcon.setImageResource(R.drawable.ic_sys_alert)
                        ColorUtil.setColorStatus(tvState, data)
                        tvTitle.text = "$strType ${data.title}"
                        tvTime.text = data.time
                        tvFrom.text = "发起人:" + data.username
                        tvType.text = "类型:" + data.type_name
                        tvMsg.text = "审批事由:" + data.reasons
                        val cnt = data.message_count
                        tvNum.text = "${cnt}"
                        if (cnt != null && cnt > 0)
                            tvNum.visibility = View.VISIBLE
                        else
                            tvNum.visibility = View.GONE
                        val urgnet = data.urgent_count
                        tvUrgent.text = "加急x${urgnet}"
                        if (urgnet != null && urgnet > 0)
                            tvUrgent.visibility = View.VISIBLE
                        else
                            tvUrgent.visibility = View.GONE

                        ivPoint.visibility = View.GONE
                        tvMsg.singleLine = true
                    } else if (bean.flag == 2) {
                        if (data.type == 1) {
                            tvUrgent.visibility = View.GONE
                            ivIcon.setImageResource(R.drawable.ic_msg_alert)
                            tvTime.text = data.time
                            val strType = when (data.type) {
                                1 -> "内部公告"
                                else -> ""
                            }
                            tvTitle.text = "${strType}:${data.title}"
                            tvTitle.textColor = Color.parseColor("#282828")
                            tvNum.visibility = View.INVISIBLE
                            tvState.visibility = View.INVISIBLE
                            tvFrom.text = "发起人:" + data.username
                            tvType.visibility = View.INVISIBLE
                            tvMsg.text = data.contents
                            tvMsg.singleLine = true

                            if (data.has_read == 0) {
                                ivPoint.visibility = View.VISIBLE
                            } else if (data.has_read == 1) {
                                ivPoint.visibility = View.GONE
                            } else {
                                ivPoint.visibility = View.GONE
                            }
                        } else if (data.type == 2) {
                            tvUrgent.visibility = View.GONE
                            ivIcon.setImageResource(R.drawable.ic_sys_alert)
                            tvTime.text = data.time
                            if (data.has_read == 0) {
                                ivPoint.visibility = View.VISIBLE
                            } else if (data.has_read == 1) {
                                ivPoint.visibility = View.GONE
                            } else {
                                ivPoint.visibility = View.GONE
                            }
                            tvTitle.text = "审批数据报表"
                            tvTitle.textColor = Color.parseColor("#fff5a623")
                            tvNum.visibility = View.INVISIBLE
                            tvState.visibility = View.INVISIBLE
                            tvFrom.visibility = View.GONE
                            tvType.visibility = View.GONE
                            tvMsg.text = data.contents
                            tvMsg.singleLine = false
                            tvMsg.maxLines = 2
                            tvMsg.minLines = 2
                        }
                    }
                }

            }
        })
        adapter.onItemClick = { v, p ->
            val data = adapter.dataList.get(p)
            if (bean.flag == 1) {
                val is_mine = if (data.status == -1 || data.status == 4) 1 else 2
                if (data.type == 2)
                    SealApproveActivity.start(this, data, is_mine)
                else if (data.type == 3)
                    SignApproveActivity.start(this, data, is_mine)
                else if (data.type == 1)
                    LeaveBusinessApproveActivity.start(this, data, is_mine)//出差  SealApproveActivity
            } else if (bean.flag == 2) {
                if (data.type == 1) {
                    // 内部公告
                    GongGaoDetailActivity.start(context, data)
                } else {
                    // 审批统计信息
                    ApproveListActivity.start(this@MessageListActivity, 6, data.news_id, data)
                }
            }
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
//        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

        stateDefault()

        kotlin.run {
            fl_filter.setOnClickListener {
                stateDefault()
            }
            iv_filter.setOnClickListener {
                if (fl_filter.visibility == View.VISIBLE) {
                    stateDefault()
                } else {
                    stateFilter()
                }
            }
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
        config.loadMoreEnable = false
        config.disableContentWhenRefresh = true
        return config
    }

    override fun initRefreshHeader(): RefreshHeader? = ClassicsHeader(this)

    override fun initRefreshFooter(): RefreshFooter? = null

    var filterBean: ApproveFilterBean? = null
    var paramTemplates = ArrayList<String>()
    var paramStates = ArrayList<String>()
    var filterType: Int? = null

    fun stateDefault() {
        page = 1
        fl_filter.visibility = View.GONE
        paramStates.clear()
        paramTemplates.clear()
        doRequest()
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
            page = 1
            doRequest()
        }
    }

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
                page = 1
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
                page = 1
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

    override fun onResume() {
        super.onResume()
        page = 1
        doRequest()
    }

    var page = 1
    fun doRequest() {
        if (bean.flag == 1) {
        SoguApi.getService(application, OtherService::class.java)
                .msgList(page = page, pageSize = 20)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1)
                            adapter.dataList.clear()

                        //filter   data.type=paramTemplates.key
                        var oriData = payload.payload!!
                        if (paramTemplates.size == 0) {
                            adapter.dataList.addAll(oriData)
                        } else {
                            var filtered = ArrayList<MessageBean>()
                            for (item in oriData) {
                                paramTemplates
                                        .filter { item.type == it.toInt() }
                                        .forEach { filtered.add(item) }
                            }
                            adapter.dataList.addAll(filtered)
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
        } else if (bean.flag == 2) {
            SoguApi.getService(application,OtherService::class.java)
                    .sysMsgList(page = page, pageSize = 20)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            if (page == 1) {
                                adapter.dataList.clear()
                            }
                            payload?.payload?.apply {
                                adapter.dataList.addAll(this)
                            }
                            adapter.notifyDataSetChanged()
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
    }

    companion object {
        fun start(ctx: Activity?, bean: MessageIndexBean) {
            val intent = Intent(ctx, MessageListActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }
}
