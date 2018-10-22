package com.sogukj.pe.module.receipt

import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.normal_toolbar.*

/**
 * Created by CH-ZH on 2018/10/22.
 */
class BillHeaderDetailActivity : ToolbarActivity() {
    private var id : Int ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_header_detail)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        id = intent.getIntExtra(Extras.ID,-1)
        setBack(true)
        setTitle("抬头详情")
        toolbar_menu.setVisible(true)
        toolbar_menu.text = "编辑"
        toolbar_menu.setTextColor(resources.getColor(R.color.blue_3c))
    }

    private fun initData() {
        SoguApi.getService(application,OtherService::class.java)
                .getBillHeaderInfo(id!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val bean = payload.payload

                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                    }
                }
    }

    private fun bindListener() {
        toolbar_menu.clickWithTrigger {
            //编辑

        }
    }
}