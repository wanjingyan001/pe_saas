package com.sogukj.pe.module.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ApprovalBean
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.util.ColorUtil
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_approve_search.*

/**
 * Created by qinfei on 17/10/18.
 */
class ApproveSearchActivity : BaseRefreshActivity() {
    lateinit var adapter: RecyclerAdapter<ApprovalBean>
    lateinit var inflater: LayoutInflater
    var mType: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflater = LayoutInflater.from(this)
        mType = intent.getIntExtra(Extras.TYPE, 1)
        title = intent.getStringExtra(Extras.TITLE)
        setContentView(R.layout.activity_approve_search)
        search_bar.setCancel(true, {
            finish()
        })

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
                    tvType.text = data.kind
                    tvApplicant.text = data.name
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
                    ColorUtil.setColorStatus(tvState,data)
                }
            }
        })
        adapter.onItemClick = { v, p ->
            val data = adapter.dataList.get(p)
            when {
                data.type == 2 -> SealApproveActivity.start(this, data, if (mType == 3) 1 else 2)
                data.type == 3 -> SignApproveActivity.start(this, data, if (mType == 3) 1 else 2)
                data.type == 1 -> LeaveBusinessApproveActivity.start(this, data, if (mType == 3) 1 else 2)
            }
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

        search_bar.onTextChange = { text ->
            handler.removeCallbacks(searchTask)
            handler.postDelayed(searchTask, 100)
        }
        search_bar.onSearch = { text ->
            doRequest()
        }
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    val searchTask = Runnable {
        doRequest()
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

    var page = 1
    fun doRequest() {
        val text = search_bar.search
        var pro_id: Int? = null
        var status_tyoe: Int? = null
        if (intent.getStringExtra(Extras.TITLE) == "审批历史") {
            pro_id = intent.getIntExtra(Extras.ID, 1)
            status_tyoe = null
        } else {
            pro_id = null
            status_tyoe = mType
        }
        SoguApi.getService(application, ApproveService::class.java)
                .listApproval(status = status_tyoe, page = page,
                        fuzzyQuery = text, project_id = pro_id)
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
