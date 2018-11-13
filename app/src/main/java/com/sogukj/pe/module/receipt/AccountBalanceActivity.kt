package com.sogukj.pe.module.receipt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.RechargeRecordBean
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_account_balance.*
import org.jetbrains.anko.find

/**
 * Created by CH-ZH on 2018/10/18.
 * 个人（企业）账户余额
 */
class AccountBalanceActivity : BaseRefreshActivity(){
    private var title = ""
    private var type = 1 // 1 :个人账户 2: 企业账户
    lateinit var adapter : RecyclerAdapter<RechargeRecordBean.AccountList>
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
        type = intent.getIntExtra(Extras.TYPE,1)
        setBack(true)
        setTitle(title)
        rv_balance.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapter(this){_adapter,parent,_ ->
            BalanceHolder(_adapter.getView(R.layout.item_account_balance,parent))
        }
    }

    private fun initData() {
        rv_balance.adapter = adapter
        getAccountDatas()
    }

    private fun getAccountDatas() {
        SoguApi.getStaticHttp(application)
                .getMineWalletDetail(type)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val recordBean = payload.payload
                            if (null != recordBean){
                                val list = recordBean.list
                                tv_coin.text = Utils.reserveDecimal(recordBean.balance.toDouble())
                                if (null != list && list.size > 0){
                                    showList()
                                    adapter.dataList.clear()
                                    adapter.dataList.addAll(list)
                                    adapter.notifyDataSetChanged()
                                }else{
                                    showEmpty()
                                }
                            }else{
                                showEmpty()
                            }
                        }
                        dofinishRefresh()
                    }

                    onError {
                        it.printStackTrace()
                        showEmpty()
                        dofinishRefresh()
                    }
                }
    }

    private fun showEmpty(){
        fl_empty.setVisible(true)
        fl_balance.setVisible(false)
    }

    private fun showList(){
        fl_empty.setVisible(false)
        fl_balance.setVisible(true)
    }

    companion object {
        val RECHARGE_CODE = 0x1001
    }

    private fun bindListener() {
        ll_recharge.clickWithTrigger {
            //充值
            when(type){
                1 -> AccountRechargeActivity.invokeForResult(this,"个人账户充值",tv_coin.textStr,1,RECHARGE_CODE)
                2 -> AccountRechargeActivity.invokeForResult(this,"企业账户充值",tv_coin.textStr,2,RECHARGE_CODE)
            }
        }
    }

    inner class BalanceHolder(itemView: View) : RecyclerHolder<RechargeRecordBean.AccountList>(itemView) {
        val tv_time = itemView.find<TextView>(R.id.tv_time)
        val tv_type = itemView.find<TextView>(R.id.tv_type)
        val tv_coin = itemView.find<TextView>(R.id.tv_coin)
        override fun setData(view: View, data: RechargeRecordBean.AccountList, position: Int) {
            if (null == data) return
            tv_time.text = data.add_time
            tv_coin.text = data.fee
            tv_type.text = data.info
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
        getAccountDatas()
    }

    override fun doLoadMore() {

    }

    fun dofinishRefresh() {
        if (this::refresh.isLateinit && refresh.isRefreshing) {
            refresh.finishRefresh()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            when(requestCode){
                RECHARGE_CODE -> {
                    getAccountDatas()
                }
            }
        }
    }
}


