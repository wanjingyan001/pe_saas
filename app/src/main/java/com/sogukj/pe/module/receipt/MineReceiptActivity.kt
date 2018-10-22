package com.sogukj.pe.module.receipt

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setDrawable
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.MineReceiptBean
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.widgets.indexbar.RecycleViewDivider
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_mine_receipt.*
import kotlinx.android.synthetic.main.normal_toolbar.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/10/16.
 * 我的订单
 */
class MineReceiptActivity : BaseRefreshActivity() {
    private var datas = ArrayList<MineReceiptBean>()
    private lateinit var adapter: RecyclerAdapter<MineReceiptBean>
    private lateinit var alreadySelected: MutableSet<MineReceiptBean>
    private lateinit var ordersSet : MutableSet<String>
    private var totalAmount = 0f
    private var type = 0  //0 不可选择  1 可以选择
    private var page = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mine_receipt)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        title = "我的订单"
        setBack(true)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        type = intent.getIntExtra(Extras.TYPE, -1)
        rv_express.layoutManager = LinearLayoutManager(this)
        rv_express.addItemDecoration(RecycleViewDivider(this, LinearLayoutManager.VERTICAL,
                Utils.dip2px(this, 10f), Color.parseColor("#f7f9fc")))
        alreadySelected = ArrayList<MineReceiptBean>().toMutableSet()
        ordersSet = ArrayList<String>().toMutableSet()
        if (type == 0) {
            rl_submit.setVisible(false)
            toolbar_menu.setVisible(true)
            toolbar_menu.text = "开发票"
        } else if (type == 1) {
            rl_submit.setVisible(true)
            toolbar_menu.setVisible(false)
        }

        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
    }

    private fun initData() {

        adapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            ReceiptHolder(_adapter.getView(R.layout.mine_receipt_item, parent))
        }
        rv_express.adapter = adapter
        setLoadding()
        getBillOrderDatas(false)
    }

    private fun getBillOrderDatas(isLoadMore : Boolean) {
        if (isLoadMore){
            page++
        }else{
            page = 1
        }
        SoguApi.getService(application,OtherService::class.java)
                .billOrderList(page)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val datas = payload.payload
                            if (null != datas && datas.size > 0){
                                if (!isLoadMore){
                                    adapter.dataList.clear()
                                    adapter.dataList.addAll(datas)
                                    adapter.notifyDataSetChanged()
                                }else{
                                    adapter.dataList.addAll(datas)
                                    adapter.notifyDataSetChanged()
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
        tv_comit.clickWithTrigger {
            //确定
            if (totalAmount != 0f){
                startActivity<CreateBillActivity>(Extras.DATA to totalAmount,Extras.LIST to ordersSet.toMutableList())
            }
        }

        toolbar_menu.clickWithTrigger {
            //开发票
            toolbar_menu.setVisible(false)
            rl_submit.setVisible(true)
            type = 1
            adapter.notifyDataSetChanged()
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
        getBillOrderDatas(false)
    }

    override fun doLoadMore() {
        getBillOrderDatas(true)
    }

    inner class ReceiptHolder(itemView: View) : RecyclerHolder<MineReceiptBean>(itemView) {
        val iv_select = itemView.find<ImageView>(R.id.iv_select)
        val tv_title = itemView.find<TextView>(R.id.tv_title)
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        val tv_amount = itemView.find<TextView>(R.id.tv_amount)
        val tv_coin = itemView.find<TextView>(R.id.tv_coin)
        override fun setData(view: View, data: MineReceiptBean, position: Int) {
            if (null == data) return
            Log.e("TAG","type ==" + type)
            if (data.is_invoice == 1) {
                iv_select.visibility = View.INVISIBLE
                tv_title.setTextColor(resources.getColor(R.color.black_8028))
                tv_time.setDrawable(tv_time, 0, getDrawable(R.mipmap.ic_receipt_time_pay))
                tv_time.setTextColor(resources.getColor(R.color.gray_80a0))

                tv_amount.setTextColor(resources.getColor(R.color.black_8028))
                tv_amount.setDrawable(tv_amount, 0, getDrawable(R.drawable.selector_normal_amount_pay))

                tv_coin.setTextColor(resources.getColor(R.color.black_8028))
                tv_coin.setDrawable(tv_coin, 0, getDrawable(R.drawable.selector_normal_coin_pay))
            } else {
                iv_select.visibility = View.VISIBLE
                tv_title.setTextColor(resources.getColor(R.color.black_28))
                tv_time.setDrawable(tv_time, 0, getDrawable(R.mipmap.ic_receipt_time))
                tv_time.setTextColor(resources.getColor(R.color.gray_a0))

                tv_amount.setTextColor(resources.getColor(R.color.black_28))
                tv_amount.setDrawable(tv_amount, 0, getDrawable(R.drawable.selector_normal_amount))

                tv_coin.setTextColor(resources.getColor(R.color.black_28))
                tv_coin.setDrawable(tv_coin, 0, getDrawable(R.drawable.selector_normal_coin))
            }

            when (type) {
                0 -> iv_select.setVisible(false)
                1 -> {
                    if (data.is_invoice == 1){
                        iv_select.visibility = View.INVISIBLE
                    }else{
                        iv_select.setVisible(true)
                    }
                }
            }

            tv_title.text = data.type
            tv_time.text = data.pay_time
            tv_amount.text = "数量：${data.count}"
            tv_coin.text = "支付金额：${data.fee}"
            if (type == 1){
                itemView.setOnClickListener {
                    if (alreadySelected.contains(data)) {
                        alreadySelected.remove(data)
                        ordersSet.remove(data.order_no)
                        iv_select.setImageResource(R.mipmap.ic_select_receipt)
                        totalAmount -= data.fee
                    } else {
                        alreadySelected.add(data)
                        ordersSet.add(data.order_no)
                        iv_select.setImageResource(R.mipmap.ic_unselect_receipt)
                        totalAmount += data.fee
                    }
                    tv_total.text = "￥${totalAmount}"
                    if (totalAmount > 0){
                        tv_comit.setBackgroundResource(R.drawable.selector_sure)
                    }else{
                        tv_comit.setBackgroundResource(R.drawable.selector_sure_gray)
                    }
                }
            }

        }

    }

}