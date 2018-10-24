package com.sogukj.pe.module.receipt

import android.os.Bundle
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils

/**
 * Created by CH-ZH on 2018/10/24.
 * 订单详情
 */
class RecordDetailActivity : ToolbarActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder_detail)
        Utils.setWindowStatusBarColor(this, R.color.white)
        initView()
        initData()
    }

    private fun initData() {

    }

    private fun initView() {
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)
        setTitle("订单详情")
    }
}