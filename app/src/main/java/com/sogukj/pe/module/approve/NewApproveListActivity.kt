package com.sogukj.pe.module.approve

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ApprovalBean
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveListBean
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_new_approve_list.*
import kotlinx.android.synthetic.main.item_approval.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor

@Route(path = ARouterPath.ApproveListActivity)
class NewApproveListActivity : BaseRefreshActivity() {
    private var page = 1
    private val kind by extraDelegate(Extras.TYPE, 1)
    private lateinit var listAdapter: RecyclerAdapter<ApproveListBean>
    private val mine by lazy { Store.store.getUser(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_approve_list)
        title = when (kind) {
            1 -> "待我审批"
            2 -> "我已审批"
            3 -> "我发起的审批"
            4 -> "抄送我的"
            else -> ""
        }
        setBack(true)
        listAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            ListHolder(_adapter.getView(R.layout.item_approval, parent))
        }
        listAdapter.onItemClick = { v, position ->
            val bean = listAdapter.dataList[position]
            when (bean.mark) {
                "old" -> {
                    val data = ApprovalBean()
                    data.add_time = Utils.getTime(bean.add_time, "yyyy/MM/dd")
                    data.approval_id = bean.approval_id
                    data.kind = bean.kind
                    data.name = bean.name
                    data.status = bean.status
                    data.status_str = bean.status_str
                    data.title = bean.title
                    data.type = bean.type
                    when {
                        bean.type == 2 -> SealApproveActivity.start(this, data, if (bean.uid == mine!!.uid) 1 else 2)
                        bean.type == 3 -> SignApproveActivity.start(this, data, if (bean.uid == mine!!.uid) 1 else 2)
                        bean.type == 1 -> LeaveBusinessApproveActivity.start(this, data, if (bean.uid == mine!!.uid) 1 else 2)
                    }
                }
                "new" -> {
                    val isMine = when {
                        kind == 1 -> 0
                        bean.uid != mine!!.uid -> 0
                        else -> 1
                    }
                    startActivity<ApproveDetailActivity>(Extras.ID to bean.approval_id,
                            Extras.FLAG to isMine)
                }
            }
        }
        approveList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = listAdapter
            addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        }
        searchLayout.clickWithTrigger {
            startActivity<SearchApproveActivity>(Extras.TYPE to kind)
        }
    }

    override fun onResume() {
        super.onResume()
        getApproveList()
    }

    override fun doRefresh() {
        page = 1
        getApproveList()
    }

    override fun doLoadMore() {
        page += 1
        getApproveList()
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.autoLoadMoreEnable = true
        config.loadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }


    private fun getApproveList() {
        SoguApi.getService(application, ApproveService::class.java)
                .getApproveList(kind, page)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                (page == 1).yes {
                                    listAdapter.dataList.clear()
                                }
                                listAdapter.dataList.addAll(it.data)
                                listAdapter.notifyDataSetChanged()
                                refresh.isEnableLoadMore = listAdapter.dataList.size < it.total
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        (page == 1).yes {
                            finishRefresh()
                        }.otherWise {
                            finishLoadMore()
                        }
                        emptyImg.setVisible(listAdapter.dataList.isEmpty())
                    }
                    onError {
                        it.printStackTrace()
                        finishLoad(page)
                    }
                }
    }


    inner class ListHolder(itemView: View) : RecyclerHolder<ApproveListBean>(itemView) {
        override fun setData(view: View, data: ApproveListBean, position: Int) {
            itemView.tv_title.text = data.title + data.number
            itemView.tv_type.text = "类别:${data.temName}"
            itemView.tv_applicant.text = "申请人:${data.name}"
            val time = Utils.getTime(data.add_time * 1000, "yyyy/MM/dd HH:mm")
            itemView.tv_date.text = time.split(" ")[0]
            itemView.tv_time.text = time.split(" ")[1]
            when (data.status) {
                -1 -> {
                    itemView.tv_state.text = "审批不通过"
                    itemView.tv_state.textColor = Color.parseColor("#ffff5858")
                }
                0 -> {
                    itemView.tv_state.text = "待审批"
                    itemView.tv_state.textColor = Color.parseColor("#ffffa715")
                }
                1 -> {
                    itemView.tv_state.text = "审批中"
                    itemView.tv_state.textColor = Color.parseColor("#ff4aaaf4")
                }
                2 -> {
                    itemView.tv_state.text = "审批通过"
                    itemView.tv_state.textColor = Color.parseColor("#50D59D")
                }
                3 -> {
                    itemView.tv_state.text = "待处理"
                    itemView.tv_state.textColor = Color.parseColor("#806AF2")
                }
                4 -> {
                    itemView.tv_state.text = "审批完成"
                    itemView.tv_state.textColor = Color.parseColor("#50d59d")
                }
                5 -> {
                    itemView.tv_state.text = "撤销成功"
                    itemView.tv_state.textColor = Color.parseColor("#d9d9d9")
                }
                else -> {
                    itemView.tv_state.textColor = Color.parseColor("#50d59d")
                }
            }
        }
    }
}
