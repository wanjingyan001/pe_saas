package com.sogukj.pe.module.receipt

import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.layout_acount_info.*
import kotlinx.android.synthetic.main.layout_bill_info.*
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

    }

    private fun bindListener() {
        rl_order.clickWithTrigger {
            //我的订单
            startActivity<MineReceiptActivity>(Extras.TYPE to 0)
        }

        rl_person_account.clickWithTrigger {
            //个人账户余额
            startActivity<AccountBalanceActivity>(Extras.TITLE to "个人账户余额")
        }

        rl_busi_account.clickWithTrigger {
            //企业账户余额
            startActivity<AccountBalanceActivity>(Extras.TITLE to "企业账户余额")
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