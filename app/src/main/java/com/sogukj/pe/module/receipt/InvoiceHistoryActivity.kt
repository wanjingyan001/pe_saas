package com.sogukj.pe.module.receipt

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.InvoiceHisBean
import com.sogukj.pe.widgets.indexbar.RecycleViewDivider
import kotlinx.android.synthetic.main.activity_invoice_his.*
import kotlinx.android.synthetic.main.normal_toolbar.*

/**
 * Created by CH-ZH on 2018/10/18.
 * 发票历史和发票抬头
 */
class InvoiceHistoryActivity : BaseRefreshActivity() {
    private var type  = 0
    private var title = ""
    lateinit var adapter : RecyclerAdapter<InvoiceHisBean>
    private var data = ArrayList<InvoiceHisBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_his)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        type = intent.getIntExtra(Extras.TYPE,-1)
        title = intent.getStringExtra(Extras.TITLE)
        setTitle(title)
        rv_his.layoutManager = LinearLayoutManager(this)
        rv_his.addItemDecoration(RecycleViewDivider(this, LinearLayoutManager.VERTICAL,
                Utils.dip2px(this, 10f), Color.parseColor("#f7f9fc")))
    }

    private fun initData() {
        when (type) {
            0 -> {
                toolbar_menu.visibility = View.INVISIBLE
                adapter = RecyclerAdapter(this) { _adapter, parent, _ ->
                    BillHisHolder(_adapter.getView(R.layout.item_invoice_his, parent))
                }
            }
            1 -> {
                toolbar_menu.visibility = View.VISIBLE
                toolbar_menu.text = "添加"
                toolbar_menu.setTextColor(resources.getColor(R.color.blue_3c))
                adapter = RecyclerAdapter(this) { _adapter, parent, _ ->
                    HeaderHisHolder(_adapter.getView(R.layout.item_head_his, parent))
                }
            }
        }

        for (i in 0 .. 10){
            data.add(InvoiceHisBean())
        }
        adapter.dataList = data
        rv_his.adapter = adapter
    }

    private fun bindListener() {
        toolbar_menu.clickWithTrigger {
            //添加发票抬头
        }
    }

    inner class BillHisHolder(itemView : View):RecyclerHolder<InvoiceHisBean>(itemView){
        override fun setData(view: View, data: InvoiceHisBean, position: Int) {

        }

    }

    inner class HeaderHisHolder(itemView : View):RecyclerHolder<InvoiceHisBean>(itemView){
        override fun setData(view: View, data: InvoiceHisBean, position: Int) {

        }

    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun doRefresh() {

    }

    override fun doLoadMore() {

    }
}