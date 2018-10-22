package com.sogukj.pe.module.receipt

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.RechargeRecordBean
import kotlinx.android.synthetic.main.activity_account_balance.*
import org.jetbrains.anko.find

/**
 * Created by CH-ZH on 2018/10/18.
 * 个人（企业）账户余额
 */
class AccountBalanceActivity : BaseRefreshActivity(){
    private var title = ""
    private var infos = ArrayList<RechargeRecordBean>()
    lateinit var adapter : RecyclerAdapter<RechargeRecordBean>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_balance)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        title = intent.getStringExtra(Extras.TITLE)
        setBack(true)
        setTitle(title)
        rv_balance.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapter(this){_adapter,parent,_ ->
            BalanceHolder(_adapter.getView(R.layout.item_account_balance,parent))
        }
    }

    private fun initData() {
        for (i in 0 .. 10){
            val rechargeRecordBean = RechargeRecordBean()
            rechargeRecordBean.time = "2018年7月10日 15:12"
            rechargeRecordBean.type = "支付宝充值金额"
            rechargeRecordBean.coin = "560"
            infos.add(rechargeRecordBean)
        }
        adapter.dataList = infos
        rv_balance.adapter = adapter
    }

    private fun bindListener() {
        ll_recharge.clickWithTrigger {
            //充值
        }
    }

    inner class BalanceHolder(itemView: View) : RecyclerHolder<RechargeRecordBean>(itemView) {
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        val tv_type = itemView.find<TextView>(R.id.tv_type)
        val tv_coin = itemView.find<TextView>(R.id.tv_coin)
        override fun setData(view: View, data: RechargeRecordBean, position: Int) {
            if (null == data) return
            tv_time.text = data.time
            tv_type.text = data.type
            tv_coin.text = data.coin
        }
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = false
        config.autoLoadMoreEnable = false
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun doRefresh() {

    }

    override fun doLoadMore() {

    }
}


