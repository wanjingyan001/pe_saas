package com.sogukj.pe.module.receipt

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.InvoiceHisBean
import com.sogukj.pe.widgets.indexbar.RecycleViewDivider
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_invoice_his.*
import kotlinx.android.synthetic.main.normal_toolbar.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/10/18.
 * 发票历史和发票抬头
 */
@Route(path = ARouterPath.ReceiptHeaderActivity)
class InvoiceHistoryActivity : BaseRefreshActivity() {
    private var type  = 0
    private var title = ""
    lateinit var adapter : RecyclerAdapter<InvoiceHisBean>
    private var page = 1
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
        type = intent.getIntExtra(Extras.TYPE,0)
        title = intent.getStringExtra(Extras.TITLE)
        setTitle(title)
        setBack(true)
        rv_his.layoutManager = LinearLayoutManager(this)
        rv_his.addItemDecoration(RecycleViewDivider(this, LinearLayoutManager.VERTICAL,
                Utils.dip2px(this, 10f), Color.parseColor("#f7f9fc")))
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
    }

    private fun initData() {
        setLoadding()
        when (type) {
            0 -> {
                toolbar_menu.visibility = View.INVISIBLE
                adapter = RecyclerAdapter(this) { _adapter, parent, _ ->
                    BillHisHolder(_adapter.getView(R.layout.item_invoice_his, parent))
                }
                getBillHisData(false)
            }
            1 -> {
                toolbar_menu.visibility = View.VISIBLE
                toolbar_menu.text = "添加"
                toolbar_menu.setTextColor(resources.getColor(R.color.blue_3c))
                adapter = RecyclerAdapter(this) { _adapter, parent, _ ->
                    HeaderHisHolder(_adapter.getView(R.layout.item_head_his, parent))
                }
                getBillHeaderData(false)
            }
        }
        rv_his.adapter = adapter

    }

    fun showEmpty(){
        fl_empty.visibility = View.VISIBLE
        rv_his.visibility = View.INVISIBLE
    }

    fun goneEmpty(){
        fl_empty.visibility = View.INVISIBLE
        rv_his.visibility = View.VISIBLE
    }
    override fun onResume() {
        super.onResume()
        if (type == 1){
            getBillHeaderData(false)
        }
    }

    private fun getBillHeaderData(isLoadMore: Boolean) {
        if (isLoadMore){
            page++
        }else{
            page = 1
        }
        SoguApi.getStaticHttp(application)
                .getBillHeaderList(page)
                .execute {
                    onNext {payload ->
                        if (payload.isOk){
                            val infos = payload.payload
                            if (null != infos && infos.size > 0){
                                if (isLoadMore){
                                    adapter.dataList.addAll(infos)
                                    adapter.notifyDataSetChanged()
                                }else{
                                    adapter.dataList.clear()
                                    adapter.dataList.addAll(infos)
                                    adapter.notifyDataSetChanged()
                                }
                                goneEmpty()
                            }else{
                                if (!isLoadMore){
                                    showEmpty()
                                }
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                        if (isLoadMore){
                            dofinishLoadMore()
                        }else{
                            dofinishRefresh()
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        if (isLoadMore){
                            dofinishLoadMore()
                        }else{
                            dofinishRefresh()
                        }
                    }
                }
    }

    private fun getBillHisData(isLoadMore:Boolean) {
        if (isLoadMore){
            page++
        }else{
            page = 1
        }
        SoguApi.getStaticHttp(application)
                .billHisList(page)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val infos = payload.payload
                            if (null != infos && infos.size > 0){
                                if (isLoadMore){
                                    adapter.dataList.addAll(infos)
                                    adapter.notifyDataSetChanged()
                                }else{
                                    adapter.dataList.clear()
                                    adapter.dataList.addAll(infos)
                                    adapter.notifyDataSetChanged()
                                }
                                goneEmpty()
                            }else{
                                if (!isLoadMore){
                                    showEmpty()
                                }
                            }
                        }else{
                          showErrorToast(payload.message)
                        }
                        if (isLoadMore){
                            dofinishLoadMore()
                        }else{
                            dofinishRefresh()
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        if (isLoadMore){
                            dofinishLoadMore()
                        }else{
                            dofinishRefresh()
                        }
                    }
                }

    }

    fun setLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }
    fun dofinishRefresh() {
        if (this::refresh.isLateinit && refresh.isRefreshing) {
            refresh.finishRefresh()
        }
        goneLoadding()
    }

    fun dofinishLoadMore() {
        if (this::refresh.isLateinit && refresh.isLoading) {
            refresh.finishLoadMore()
        }
        goneLoadding()
    }

    private fun bindListener() {
        toolbar_menu.clickWithTrigger {
            //添加发票抬头
            startActivity<AddBillHeaderActivity>()
        }

        adapter.onItemClick = {v,p ->
            when(type){
                0 -> {
                    val dataList = adapter.dataList
                    if (null != dataList && dataList.size > 0){
                        val invoiceHisBean = dataList[p]
                        if (null != invoiceHisBean){
                            startActivity<BillDetailActivity>(Extras.ID to invoiceHisBean.id,Extras.DATA to invoiceHisBean)
                        }
                    }
                }

                1 -> {
                    //抬头详情
                    val dataList = adapter.dataList
                    if (null != dataList && dataList.size > 0){
                        val invoiceHisBean = dataList[p]
                        if (null != invoiceHisBean){
                            startActivity<BillHeaderDetailActivity>(Extras.ID to invoiceHisBean.id)
                        }
                    }
                }
            }
        }
    }

    inner class BillHisHolder(itemView : View):RecyclerHolder<InvoiceHisBean>(itemView){
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        val tv_status = itemView.find<TextView>(R.id.tv_status)
        val tv_type = itemView.find<TextView>(R.id.tv_type)
        val tv_company = itemView.find<TextView>(R.id.tv_company)
        val tv_coin = itemView.find<TextView>(R.id.tv_coin)
        override fun setData(view: View, data: InvoiceHisBean, position: Int) {
            if (null == data) return
            tv_time.text = data.add_time
            when(data.status){
                1 -> tv_status.text = "待开票"
                2 -> tv_status.text = "已发出"
            }
            tv_type.text = data.type
            tv_company.text = data.title
            tv_coin.text = data.amount
        }

    }

    inner class HeaderHisHolder(itemView : View):RecyclerHolder<InvoiceHisBean>(itemView){
        val tv_name = itemView.find<TextView>(R.id.tv_name)
        val tv_duty = itemView.find<TextView>(R.id.tv_duty)
        val view_devider = itemView.find<View>(R.id.view)
        override fun setData(view: View, data: InvoiceHisBean, position: Int) {
            if (null == data) return
            tv_name.text = data.title
            tv_duty.text = "税号：${Utils.getSpaceText(data.tax_no)}"
            if (data.tax_no.isNullOrEmpty()){
                tv_duty.setVisible(false)
                view_devider.setVisible(false)
            }else{
                tv_duty.setVisible(true)
                view_devider.setVisible(true)
            }
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
        when(type){
            0 -> getBillHisData(false)
            1 -> getBillHeaderData(false)
        }
    }

    override fun doLoadMore() {
        when(type){
            0 -> getBillHisData(true)
            1 -> getBillHeaderData(true)
        }
    }
}