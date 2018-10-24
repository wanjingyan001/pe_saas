package com.sogukj.pe.module.receipt

import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.MineWalletBean
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.layout_acount_info.*
import kotlinx.android.synthetic.main.layout_bill_info.*
import kotlinx.android.synthetic.main.layout_wallet_top.*
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/10/17.
 * 我的钱包
 */
class MineWalletActivity : ToolbarActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mine_wallet)
        Utils.setWindowStatusBarColor(this, R.color.white)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)
        setTitle("我的钱包")
    }

    private fun initData() {
        SoguApi.getService(application,OtherService::class.java)
                .getMineWalletData()
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val list = payload.payload
                            if (null != list && list.size > 0){
                                setMineWalletInfos(list)
                            }
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                    }
                }
    }

    private fun setMineWalletInfos(list: List<MineWalletBean>) {
        list.forEach {
            when(it.type){
                1 -> tv_paccount_remain.text = "${it.balance}元"
                2 -> tv_eaccount_remain.text = "${it.balance}元"
                3 -> tv_order_count.text = "共${it.order_count}个订单"
                4 -> tv_his.text = "共${it.invoice_count}个开票历史"
                5 -> tv_header.text = "共${it.title_count}个抬头"
                6 -> {
                    tv_sentiment_count.text = "舆情监控共${it.max}次"
                    tv_sentiment_remain.text = "剩余${it.over}个"
                }
                7 -> {
                    tv_credit_count.text = "征信套餐共${it.max}次"
                    tv_credit_remain.text = "剩余${it.over}个"
                }
            }
        }
    }

    private fun bindListener() {
        rl_order.clickWithTrigger {
            //我的订单
            startActivity<MineReceiptActivity>(Extras.TYPE to 0)
        }

        rl_person_account.clickWithTrigger {
            //个人账户余额
            startActivity<AccountBalanceActivity>(Extras.TITLE to "个人账户余额",Extras.TYPE to 1)
        }

        rl_busi_account.clickWithTrigger {
            //企业账户余额
            startActivity<AccountBalanceActivity>(Extras.TITLE to "企业账户余额",Extras.TYPE to 2)
        }

        rl_bill_his.clickWithTrigger {
            //开票历史
            startActivity<InvoiceHistoryActivity>(Extras.TYPE to 0,Extras.TITLE to "开票历史")
        }

        rl_bill_header.clickWithTrigger {
            //发票抬头
            startActivity<InvoiceHistoryActivity>(Extras.TYPE to 1,Extras.TITLE to "发票抬头")
        }

        rl_open_bill.clickWithTrigger {
            //开发票
            startActivity<MineReceiptActivity>(Extras.TYPE to 1)
        }
    }

}