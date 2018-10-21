package com.sogukj.pe.module.receipt

import android.os.Bundle
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils

/**
 * Created by CH-ZH on 2018/10/19.
 * 开票详情
 */
class BillDetailActivity : ToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_detail)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        initView()
        initData()
    }

    private fun initView() {
        setBack(true)
        setTitle("开票详情")
    }

    private fun initData() {

    }

}