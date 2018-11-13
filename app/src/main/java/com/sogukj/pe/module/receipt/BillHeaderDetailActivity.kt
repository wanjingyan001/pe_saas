package com.sogukj.pe.module.receipt

import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.BillDetailBean
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.layout_head_content.*
import kotlinx.android.synthetic.main.normal_toolbar.*
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/10/22.
 * 抬头详情
 */
class BillHeaderDetailActivity : ToolbarActivity() {
    private var id : Int ? = null
    private var billBean : BillDetailBean ? = null
    private var type : Int = 1   // 1 企业发票 2 个人发票
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
        SoguApi.getStaticHttp(application)
                .getBillHeaderInfo(id!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val bean = payload.payload
                            billBean = bean
                            bean?.let {
                                type = bean.type
                                setDetailData(bean)
                            }
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                    }
                }
    }

    private fun setDetailData(bean: BillDetailBean){
        tv_title.text = bean.title
        Utils.setSpaceText(tv_duty,bean.tax_no)
        tv_address.text = bean.address
        if (type == 1){
            tv_phone_tips.text = "电话号码"
            tv_phone.text = bean.telphone
            if (bean.telphone.isNullOrEmpty()){
                ll_phone.setVisible(false)
                view_phone.setVisible(false)
            }else{
                ll_phone.setVisible(true)
                view_phone.setVisible(true)
            }
        }else{
            tv_phone_tips.text = "手机号"
            tv_phone.text = bean.phone
            if (bean.phone.isNullOrEmpty()){
                ll_phone.setVisible(false)
                view_phone.setVisible(false)
            }else{
                ll_phone.setVisible(true)
                view_phone.setVisible(true)
            }
        }

        if (bean.tax_no.isNullOrEmpty()){
            ll_duty.setVisible(false)
            view_duty.setVisible(false)
        }else{
            ll_duty.setVisible(true)
            view_duty.setVisible(true)
        }

        if (bean.address.isNullOrEmpty()){
            ll_address.setVisible(false)
            view_address.setVisible(false)
        }else{
            ll_address.setVisible(true)
            view_address.setVisible(true)
        }

    }

    private fun bindListener() {
        toolbar_menu.clickWithTrigger {
            //编辑
            startActivity<AddBillHeaderActivity>(Extras.DATA to billBean,Extras.ID to id)
            finish()
        }
    }
}