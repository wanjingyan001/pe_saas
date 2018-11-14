package com.sogukj.pe.module.fund

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.toMoney
import com.sogukj.pe.baselibrary.Extended.toMoneyWithUnit
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.interf.MoneyUnit
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.DotView
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.FundCompany
import com.sogukj.pe.bean.JCKJLBean
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_financial.*
import org.jetbrains.anko.textColor

class FundFinancialActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?, data: FundCompany) {
            val intent = Intent(ctx, FundFinancialActivity::class.java)
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivity(intent)
        }
    }

    lateinit var bean: FundCompany
    lateinit var adapter1: RecyclerAdapter<JCKJLBean>
    lateinit var adapter2: RecyclerAdapter<JCKJLBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_financial)
        StatusBarUtil.setColor(this, resources.getColor(R.color.colorPrimary), 0)
        StatusBarUtil.setDarkMode(this)
        bean = intent.getSerializableExtra(Extras.DATA) as FundCompany
        setBack(true)
        title = bean.company_name

        kotlin.run {
            adapter1 = RecyclerAdapter(context, { _adapter, parent, type ->

                val convertView = _adapter.getView(R.layout.item_jckjl1, parent)
                val dot = convertView.findViewById<DotView>(R.id.dot) as DotView
                val tvSeq = convertView.findViewById<TextView>(R.id.tvSeq) as TextView
                val tvTime = convertView.findViewById<TextView>(R.id.timeTv) as TextView
                val tvFundName = convertView.findViewById<TextView>(R.id.fundName) as TextView
                val tvFundMoney = convertView.findViewById<TextView>(R.id.fundMoney) as TextView

                object : RecyclerHolder<JCKJLBean>(convertView) {
                    override fun setData(view: View, data: JCKJLBean, position: Int) {

                        var typeColor = if (data.type == 1) Color.parseColor("#1787FB") else Color.parseColor("#fff5a623")

                        dot.setLow(true)
                        dot.setImportant(true)
                        dot.importantColor = typeColor
                        dot.invalidate()

                        var tmp = if (data.type == 1) "出" else "回"
                        tvSeq.text = "第${data.seq}次${tmp}款"
                        tvSeq.textColor = typeColor

                        tvTime.text = data.date
                        tvFundName.text = "${tmp}款基金：${data.fundName}"
                        tvFundMoney.text = "${tmp}款：${data.figure.toMoneyWithUnit(true)}元"
                    }
                }
            })
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            jckjl.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
            jckjl.layoutManager = layoutManager
            jckjl.adapter = adapter1
        }

        kotlin.run {
            adapter2 = RecyclerAdapter(context, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_jckjl2, parent)
                val tv = convertView.findViewById<TextView>(R.id.tv) as TextView
                object : RecyclerHolder<JCKJLBean>(convertView) {
                    override fun setData(view: View, data: JCKJLBean, position: Int) {
                        tv.text = "${data.fundName}：${data.figure.toMoneyWithUnit(true)}元"
                    }
                }
            })
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            zck.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
            zck.layoutManager = layoutManager
            zck.adapter = adapter2
        }

        var type1 = 0
        var type2 = 0
        SoguApi.getService(application,ProjectService::class.java)
                .projIncomeOutcome(bean.projId!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload?.payload?.apply {
                            //总回款
                            this.allIncome?.apply {
                                zhk.text = "${this.toMoneyWithUnit(true)}元"
                            }
                            //总出款
                            adapter2.dataList.clear()
                            this.allOutcome?.forEach {
                                adapter2.dataList.add(it)
                            }
                            adapter2.notifyDataSetChanged()
                            //进出款记录
                            adapter1.dataList.clear()
                            this.record?.forEach {
                                if (it.type == 1) {
                                    it.seq = ++type1
                                }
                                if (it.type == 2) {
                                    it.seq = ++type2
                                }
                                adapter1.dataList.add(it)
                            }
                            adapter1.notifyDataSetChanged()
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }
}
