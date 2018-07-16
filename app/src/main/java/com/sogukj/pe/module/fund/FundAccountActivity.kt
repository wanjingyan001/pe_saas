package com.sogukj.pe.module.fund

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.service.FundService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_account.*
import org.jetbrains.anko.find
@Route(path = ARouterPath.FundAccountActivity)
class FundAccountActivity : ToolbarActivity() {
    private lateinit var adapter: FundAccountAdapter
    private var map = HashMap<String, String?>()

    companion object {
        val TAG = FundAccountActivity::class.java.simpleName
        fun start(ctx: Context?, bean: FundSmallBean) {
            val intent = Intent(ctx, FundAccountActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_account)
        val bean = intent.getSerializableExtra(Extras.DATA) as FundSmallBean
        setBack(true)
        title = "基金台账"
        find<TextView>(R.id.cardCompanyName).text = bean.fundName
        initAdapter()
        doRequest(bean.id)
    }

    private fun initAdapter() {
        adapter = FundAccountAdapter(this, map)
        val recyclerView = find<RecyclerView>(R.id.accountList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter
    }

    fun doRequest(fundId: Int) {
        SoguApi.getService(application,FundService::class.java)
                .getFundAccount(fundId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            Log.d(TAG, Gson().toJson(this))
                            map.put("退出项目收益", quitIncome)
                            map.put("PE项目投资金额", investmentAmount)
                            map.put("PE已上市项目浮盈/亏", profitLoss)
                            map.put("定增项目浮盈/亏", fixProfit)
                            map.put("估值", valuations)
                            if (quitIncome == null
                                    && investmentAmount == null
                                    && profitLoss == null
                                    && fixProfit == null
                                    && valuations == null) {
                                accountList.visibility = View.GONE
                                unit4.visibility = View.GONE
                            } else {
                                accountList.visibility = View.VISIBLE
                                unit4.visibility = View.VISIBLE
                                adapter.setData(map)
                            }
                            if (fundSize == "0" && contributeSize == "0" && actualSize == "0") {
                                fund_histogram_layout.visibility = View.GONE
                                empty1.visibility = View.VISIBLE
                            } else {
                                fund_histogram_layout.visibility = View.VISIBLE
                                empty1.visibility = View.GONE
                                fund_histogram.setData(floatArrayOf(fundSize.toFloat(), contributeSize.toFloat(), actualSize.toFloat()))
                            }
                            if (RaiseFunds == "0" && freeFunds == "0") {
                                fundPie_layout.visibility = View.GONE
                                empty2.visibility = View.VISIBLE
                            } else {
                                fundPie_layout.visibility = View.VISIBLE
                                empty2.visibility = View.GONE
                                fundPie.setDatas(floatArrayOf(RaiseFunds.toFloat(), freeFunds.toFloat()))
                            }
                            if (quitAll == 0 && quitNum == 0 && investedNum == 0) {
                                progressChartLayout.visibility = View.GONE
                                empty3.visibility = View.VISIBLE
                            } else {
                                progressChartLayout.visibility = View.VISIBLE
                                empty3.visibility = View.GONE
                                progressChart.setData(floatArrayOf(quitAll.toFloat(), quitNum.toFloat(), investedNum.toFloat()))
                            }
                            if (investedMoney == "0" && fundSize == "0") {
                                fund_pie2_layout.visibility = View.GONE
                                empty4.visibility = View.VISIBLE
                            } else {
                                fund_pie2_layout.visibility = View.VISIBLE
                                empty4.visibility = View.GONE
                                fund_pie2.setColor(intArrayOf(R.color.fund_deep_blue, R.color.fund_light_blue))
                                fund_pie2.setDatas(floatArrayOf(investedMoney.toFloat(), fundSize.toFloat()))
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }
}
