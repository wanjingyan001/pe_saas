package com.sogukj.pe.module.dataSource

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.edit
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
import com.sogukj.pe.bean.InvestmentEvent
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.activity_invest_search.*
import kotlinx.android.synthetic.main.item_investment_event_list.view.*
import kotlinx.android.synthetic.main.search_header.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.dip
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

class InvestSearchActivity : BaseActivity() {
    private lateinit var searchAdapter: RecyclerAdapter<InvestmentEvent>
    private lateinit var historyAdapter: TagAdapter<String>
    private val historyList = mutableSetOf<String>()
    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invest_search)
        Utils.setWindowStatusBarColor(this, R.color.white)

        searchAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            ResultHolder(_adapter.getView(R.layout.item_investment_event_list, parent))
        }
        searchResultList.apply {
            layoutManager = LinearLayoutManager(ctx)
            addItemDecoration(SpaceItemDecoration(dip(10)))
            adapter = searchAdapter
        }
        initData()
        initListener()
    }


    private fun initData() {
        val historyStr = sp.getString(Extras.INVEST_SEARCH_HISTORY, "")
        val localHistory = historyStr.split(",").filter { it.isNotEmpty() }.toMutableList()
        localHistory.isNotEmpty().yes {
            historyList.addAll(localHistory)
            ll_empty_his.setVisible(false)
        }.otherWise {
            ll_empty_his.setVisible(true)
        }
        historyAdapter = HistoryAdapter(historyList.toMutableList())
        tfl.adapter = historyAdapter

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
                getInvestList(et_search.textStr)
            }.otherWise {
                getInvestList()
            }
        }
        refresh.setOnLoadMoreListener {
            page += 1
            et_search.textStr.isNotEmpty().yes {
                getInvestList(et_search.textStr)
            }.otherWise {
                getInvestList()
            }
        }
    }

    private fun initListener() {
        tv_cancel.clickWithTrigger {
            finish()
        }
        iv_del.clickWithTrigger {
            et_search.setText("")
            if (null != historyAdapter){
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
            sp.edit { putString(Extras.INVEST_SEARCH_HISTORY, "") }
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
                    Utils.toggleSoftInput(this@InvestSearchActivity,et_search)
                    getInvestList(et_search.textStr)
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


    private fun getInvestList(searchStr: String? = null) {
        SoguApi.getService(application, DataSourceService::class.java)
                .getInvestList(search = searchStr, page = page)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                if (page == 1) {
                                    searchAdapter.refreshData(it)
                                } else {
                                    searchAdapter.dataList.addAll(it)
                                    searchAdapter.notifyDataSetChanged()
                                }
                                ifNotNull(searchStr, it, { str, list ->
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
                        (page == 1).yes {
                            refresh.finishRefresh()
                        }.otherWise {
                            refresh.finishLoadMore()
                        }
                        searchAdapter.dataList.isNotEmpty().yes {
                            search_iv_empty.setVisible(false)
                            refresh.setVisible(true)
                            historyLayout.setVisible(false)
                        }.otherWise {
                            search_iv_empty.setVisible(true)
                            refresh.setVisible(false)
                            historyLayout.setVisible(true)
                        }
                    }
                }
    }

    override fun onStop() {
        super.onStop()
        historyList.isNotEmpty().yes {
            sp.edit { putString(Extras.INVEST_SEARCH_HISTORY, historyList.joinToString(",")) }
        }
    }

    inner class HistoryAdapter(data:MutableList<String>) : TagAdapter<String>(data) {
        override fun getView(parent: FlowLayout, position: Int, t: String): View {
            val itemView = View.inflate(this@InvestSearchActivity, R.layout.search_his_item, null)
            val history = itemView.findViewById<TextView>(R.id.tv_item)
            history.text = t
            return itemView
        }
    }

    inner class ResultHolder(itemView: View) : RecyclerHolder<InvestmentEvent>(itemView) {
        override fun setData(view: View, data: InvestmentEvent, position: Int) {
            view.eventName.text = data.invested_sname
            view.eventTime.text = data.invest_time
            view.investmentMoney.text = data.money
            view.industry.text = data.industry_name
            view.investor.text = data.investor.joinToString(" / ")
        }
    }
}
