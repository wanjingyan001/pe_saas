package com.sogukj.pe.module.receipt

import android.os.Bundle
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.layout_acount_info.*
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
            startActivity<MineReceiptActivity>()
        }

    }

}