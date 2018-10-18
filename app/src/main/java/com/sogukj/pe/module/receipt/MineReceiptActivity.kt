package com.sogukj.pe.module.receipt

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setDrawable
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.MineReceiptBean
import com.sogukj.pe.widgets.indexbar.RecycleViewDivider
import kotlinx.android.synthetic.main.activity_mine_receipt.*
import org.jetbrains.anko.find

/**
 * Created by CH-ZH on 2018/10/16.
 * 我的订单
 */
class MineReceiptActivity :BaseRefreshActivity(){
    private var datas = ArrayList<MineReceiptBean>()
    private lateinit var adapter : RecyclerAdapter<MineReceiptBean>
    private lateinit var alreadySelected : MutableSet<MineReceiptBean>
    private var totalAmount = 0
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
        rv_express.layoutManager = LinearLayoutManager(this)
        rv_express.addItemDecoration(RecycleViewDivider(this, LinearLayoutManager.VERTICAL,
                Utils.dip2px(this, 10f), Color.parseColor("#f7f9fc")))
        alreadySelected = ArrayList<MineReceiptBean>().toMutableSet()
    }

    private fun initData() {
        for (i in 0 .. 10){
            var isPay = false
            if (i == 0){
                isPay = true
            }else{
                isPay = false
            }
            datas.add(MineReceiptBean("智能文书付费${i}","2018年09月27日 09:34","${i}",300,isPay))
        }
        adapter = RecyclerAdapter(this){ _adapter,parent,_->
            ReceiptHolder(_adapter.getView(R.layout.mine_receipt_item,parent))
        }
        adapter.dataList = datas
        rv_express.adapter = adapter
    }

    private fun bindListener() {
        tv_comit.clickWithTrigger {
            //确定
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

    inner class ReceiptHolder(itemView: View):RecyclerHolder<MineReceiptBean>(itemView){
        val iv_select = itemView.find<ImageView>(R.id.iv_select)
        val tv_title = itemView.find<TextView>(R.id.tv_title)
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        val tv_amount = itemView.find<TextView>(R.id.tv_amount)
        val tv_coin = itemView.find<TextView>(R.id.tv_coin)
        override fun setData(view: View, data: MineReceiptBean, position: Int) {
            if (null == data) return
            if (data.isPay){
                iv_select.visibility = View.INVISIBLE
                tv_title.setTextColor(resources.getColor(R.color.black_8028))
                tv_time.setDrawable(tv_time,0,getDrawable(R.mipmap.ic_receipt_time_pay))
                tv_time.setTextColor(resources.getColor(R.color.gray_80a0))

                tv_amount.setTextColor(resources.getColor(R.color.black_8028))
                tv_amount.setDrawable(tv_amount,0,getDrawable(R.drawable.selector_normal_amount_pay))

                tv_coin.setTextColor(resources.getColor(R.color.black_8028))
                tv_coin.setDrawable(tv_coin,0,getDrawable(R.drawable.selector_normal_coin_pay))
            }else{
                iv_select.visibility = View.VISIBLE
                tv_title.setTextColor(resources.getColor(R.color.black_28))
                tv_time.setDrawable(tv_time,0,getDrawable(R.mipmap.ic_receipt_time))
                tv_time.setTextColor(resources.getColor(R.color.gray_a0))

                tv_amount.setTextColor(resources.getColor(R.color.black_28))
                tv_amount.setDrawable(tv_amount,0,getDrawable(R.drawable.selector_normal_amount))

                tv_coin.setTextColor(resources.getColor(R.color.black_28))
                tv_coin.setDrawable(tv_coin,0,getDrawable(R.drawable.selector_normal_coin))
            }
            tv_title.text = data.title
            tv_time.text = data.time
            tv_amount.text = "数量：${data.count}"
            tv_coin.text = "支付金额：${data.coin}"

            itemView.setOnClickListener {
                if (alreadySelected.contains(data)){
                    alreadySelected.remove(data)
                    iv_select.setImageResource(R.mipmap.ic_select_receipt)
                    totalAmount -= data.coin
                }else{
                    alreadySelected.add(data)
                    iv_select.setImageResource(R.mipmap.ic_unselect_receipt)
                    totalAmount += data.coin
                }
                tv_total.text = "￥${totalAmount}"
            }
        }

    }

}