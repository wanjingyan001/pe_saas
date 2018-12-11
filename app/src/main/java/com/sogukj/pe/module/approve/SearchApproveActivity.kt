package com.sogukj.pe.module.approve

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.edit
import com.amap.api.mapcore.util.it
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.ApprovalBean
import com.sogukj.pe.bean.InvestmentEvent
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveListBean
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import io.reactivex.internal.util.HalfSerializer.onNext
import kotlinx.android.synthetic.main.activity_search_approve.*
import kotlinx.android.synthetic.main.item_approval.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.dip
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor

class SearchApproveActivity : BaseActivity() {
    private lateinit var searchAdapter: RecyclerAdapter<ApproveListBean>
    private lateinit var historyAdapter: TagAdapter<String>
    private val historyList = mutableSetOf<String>()
    private var page = 1
    private val kind by extraDelegate(Extras.TYPE, 4)
    private val mine by lazy { Store.store.getUser(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_approve)
        Utils.setWindowStatusBarColor(this, R.color.white)
        searchAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            ResultHolder(_adapter.getView(R.layout.item_approval, parent))
        }
        searchResultList.apply {
            layoutManager = LinearLayoutManager(ctx)
            addItemDecoration(SpaceItemDecoration(dip(10)))
            adapter = searchAdapter
        }
        searchAdapter.onItemClick = {v,p->
            val bean = searchAdapter.dataList[p]
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
        initHistory()
        initRefresh()
        initListener()
    }



    private fun initHistory(){
        val historyStr = sp.getString(Extras.APPROVE_SEARCH_HISTORY, "")
        val localHistory = historyStr.split(",").filter { it.isNotEmpty() }.toMutableList()
        localHistory.isNotEmpty().yes {
            historyList.addAll(localHistory)
            ll_empty_his.setVisible(false)
        }.otherWise {
            ll_empty_his.setVisible(true)
        }
        historyAdapter = HistoryAdapter(historyList.toMutableList())
        tfl.adapter = historyAdapter
    }

    private fun initRefresh() {
        refresh.isEnableRefresh = true
        refresh.isEnableLoadMore = true
        refresh.isEnableAutoLoadMore = true
        refresh.setRefreshHeader(ClassicsHeader(this))
        val footer = BallPulseFooter(ctx)
        footer.setIndicatorColor(Color.parseColor("#7BB4FC"))
        footer.setAnimatingColor(Color.parseColor("#7BB4FC"))
        refresh.setRefreshFooter(footer)
        refresh.setOnRefreshListener {
            page = 1
            et_search.textStr.isNotEmpty().yes {
                getApproveList(et_search.textStr)
            }.otherWise {
                getApproveList()
            }
        }
        refresh.setOnLoadMoreListener {
            page += 1
            et_search.textStr.isNotEmpty().yes {
                getApproveList(et_search.textStr)
            }.otherWise {
                getApproveList()
            }
        }
    }

    private fun initListener() {
        tv_cancel.clickWithTrigger {
            finish()
        }
        iv_del.clickWithTrigger {
            et_search.setText("")
            if (::historyAdapter.isLateinit){
                historyAdapter = HistoryAdapter(historyList.toMutableList())
                tfl.adapter = historyAdapter
                historyAdapter.notifyDataChanged()
                historyList.isNotEmpty().yes {
                    ll_empty_his.setVisible(false)
                }.otherWise {
                    ll_empty_his.setVisible(true)
                }
            }
        }
        tv_his.clickWithTrigger {
            sp.edit { putString(Extras.APPROVE_SEARCH_HISTORY, "") }
            historyList.clear()
            historyAdapter = HistoryAdapter(historyList.toMutableList())
            tfl.adapter = historyAdapter
            ll_empty_his.setVisible(true)
        }
        tfl.setOnTagClickListener { view, position, parent ->
            historyList.toMutableList().let {
                it.isNotEmpty().yes {
                    val str = it[position]
                    et_search.setText(str)
                    et_search.setSelection(str.length)
                }
            }
            true
        }
        et_search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                Utils.closeInput(ctx,et_search)
                et_search.textStr.isNotEmpty().yes {
                    getApproveList(et_search.textStr)
                    Utils.closeInput(this@SearchApproveActivity,et_search)
                }
                true
            }
            false
        }
        et_search.textChangedListener {
            afterTextChanged {
                if (et_search.text.isNotEmpty()) {
                    iv_del.setVisible(true)
                } else {
                    iv_del.setVisible(false)
                    et_search.setHint(R.string.search)
                    historyLayout.setVisible(true)
                    refresh.setVisible(false)
                }
            }
        }
    }


    private fun getApproveList(searchStr: String? = null) {
        SoguApi.getService(application, ApproveService::class.java)
                .getApproveList(kind, page, query = searchStr)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                searchAdapter.refreshData(it.data)
                                refresh.isEnableLoadMore = searchAdapter.dataList.size < it.total
                                ifNotNull(searchStr, it.data, { str, list ->
                                    list.isNotEmpty().yes {
                                        historyList.add(str)
                                    }
                                })
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        refresh.setVisible(searchAdapter.dataList.isNotEmpty())
                        search_iv_empty.setVisible(searchAdapter.dataList.isEmpty())
                        historyLayout.setVisible(searchAdapter.dataList.isEmpty())
                    }
                }
    }

    override fun onStop() {
        super.onStop()
        historyList.isNotEmpty().yes {
            sp.edit { putString(Extras.APPROVE_SEARCH_HISTORY, historyList.joinToString(",")) }
        }
    }

    inner class HistoryAdapter(data: MutableList<String>) : TagAdapter<String>(data) {
        override fun getView(parent: FlowLayout, position: Int, t: String): View {
            val itemView = View.inflate(this@SearchApproveActivity, R.layout.search_his_item, null)
            val history = itemView.findViewById<TextView>(R.id.tv_item)
            history.text = t
            return itemView
        }
    }

    inner class ResultHolder(itemView: View) : RecyclerHolder<ApproveListBean>(itemView) {
        @SuppressLint("SetTextI18n")
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
                    itemView.tv_state.text = "待用印"
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
