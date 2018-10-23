package com.sogukj.pe.module.receipt

import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils

/**
 * Created by CH-ZH on 2018/10/23.
 * 账户充值
 */
class AccountRechargeActivity : ToolbarActivity() {
    private var title = ""
    private var balance = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_recharge)
        Utils.setWindowStatusBarColor(this, R.color.white)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)
        setTitle(intent.getStringExtra(Extras.TITLE))
    }

    private fun initData() {

    }

    private fun bindListener() {

    }

}