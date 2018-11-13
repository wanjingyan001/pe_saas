package com.sogukj.pe.module.receipt

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.BillDetailBean
import com.sogukj.pe.bean.InvoiceHisBean
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_bill_detail.*
import kotlinx.android.synthetic.main.layout_accept_info.*
import kotlinx.android.synthetic.main.layout_bill_tip.*

/**
 * Created by CH-ZH on 2018/10/19.
 * 开票详情
 */
class BillDetailActivity : ToolbarActivity() {
    private var id : Int ? = null
    private var his : InvoiceHisBean? = null
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
        id = intent.getIntExtra(Extras.ID, -1)
        his = intent.getSerializableExtra(Extras.DATA) as InvoiceHisBean
    }

    private fun initData() {
        SoguApi.getStaticHttp(application)
                .getBillDetailInfo(id!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val detailBean = payload.payload
                            detailBean?.let {
                                setDetailInfo(detailBean)
                            }
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                    }
                }
    }

    private fun setDetailInfo(detailBean: BillDetailBean) {
        if (null != his){
            when(his!!.status){
                1 -> tv_status.text = "纸质发票待发出"
                2 -> tv_status.text = "纸质发票已发出"
            }
        }
        tv_time.text = detailBean.add_time
        tv_address.text = detailBean.province + detailBean.city + detailBean.county + detailBean.address
        tv_accept.text = "${detailBean.receiver} ${detailBean.phone}"
        tv_company.text = detailBean.title
        Utils.setSpaceText(tv_duty,detailBean.tax_no)
        tv_content.text = detailBean.content
        tv_accept_time.text = detailBean.add_time

        if (detailBean.tax_no.isNullOrEmpty()){
            ll_duty.setVisible(false)
            view_duty.setVisible(false)
        }else{
            ll_duty.setVisible(true)
            view_duty.setVisible(true)
        }
        val spannable = SpannableString(detailBean.amount+"元")
        spannable.setSpan(ForegroundColorSpan(Color.parseColor("#F7B62B")),0,
                detailBean.amount.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tv_coin.text = spannable
    }

}