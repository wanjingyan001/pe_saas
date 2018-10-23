package com.sogukj.pe.module.receipt

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.BillDetailBean
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.layout_add_content.*
import kotlinx.android.synthetic.main.normal_toolbar.*

/**
 * Created by CH-ZH on 2018/10/22.
 * 添加发票抬头
 */
class AddBillHeaderActivity : ToolbarActivity(), TextWatcher {
    private var type : Int = 1   // 1 企业发票 2 个人发票
    private var bean : BillDetailBean? = null
    private var id : Int ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bheader)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        initView()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        bean = intent.getSerializableExtra(Extras.DATA) as BillDetailBean?
        id = intent.getIntExtra(Extras.ID,-1)
        setTitle("发票抬头")
        toolbar_menu.text = "完成"
        toolbar_menu.setTextColor(resources.getColor(R.color.blue_3c))
        toolbar_menu.setVisible(true)

        setPrimeData()
    }

    private fun setPrimeData() {
        if (null == bean) return
        et_title.setText(bean!!.title)
        et_duty.setText(bean!!.tax_no)
        if (!bean!!.address.isNullOrEmpty()){
            et_address.setText(bean!!.address)
        }
        if (!bean!!.phone.isNullOrEmpty()){
            et_number.setText(bean!!.phone)
        }
    }

    private fun bindListener() {
        toolbar_menu.clickWithTrigger {
            //完成
            comitBillHeader()
        }

        ll_company.clickWithTrigger {
            //公司
            if (type == 2){
                iv_company.setImageResource(R.mipmap.ic_unselect_receipt)
                iv_personal.setImageResource(R.mipmap.ic_select_receipt)
                type = 1
                ll_other.setVisible(true)
            }
        }

        ll_personal.clickWithTrigger {
            //个人
            if (type == 1){
                iv_company.setImageResource(R.mipmap.ic_select_receipt)
                iv_personal.setImageResource(R.mipmap.ic_unselect_receipt)
                type = 2
                ll_other.setVisible(false)
            }
        }

        et_title.addTextChangedListener(this)

        iv_delete.clickWithTrigger {
            et_title.setText("")
            iv_delete.setVisible(false)
        }
    }

    private fun comitBillHeader() {
        when(type){
            1 -> {
                if (et_title.textStr.isNullOrEmpty()){
                    showCustomToast(R.drawable.icon_toast_common, "请填写名称")
                    return
                }
                if (et_duty.textStr.isNullOrEmpty()){
                    showCustomToast(R.drawable.icon_toast_common, "请填写税号")
                    return
                }
                if (!et_phone.textStr.isNullOrEmpty() && !Utils.isMobile(et_phone.textStr)){
                    showCustomToast(R.drawable.icon_toast_common, "请填写正确的手机号")
                    return
                }

                if (!et_email.textStr.isNullOrEmpty() && !Utils.isEmail(et_email.textStr)){
                    showCustomToast(R.drawable.icon_toast_common, "请填写正确的邮箱")
                    return
                }

            }

            2 -> {
                if (et_title.textStr.isNullOrEmpty()){
                    showCustomToast(R.drawable.icon_toast_common, "请填写名称")
                    return
                }

                if (!et_phone.textStr.isNullOrEmpty() && !Utils.isMobile(et_phone.textStr)){
                    showCustomToast(R.drawable.icon_toast_common, "请填写正确的手机号")
                    return
                }

                if (!et_email.textStr.isNullOrEmpty() && !Utils.isEmail(et_email.textStr)){
                    showCustomToast(R.drawable.icon_toast_common, "请填写正确的邮箱")
                    return
                }

            }
        }

        addBillHeader()
    }

    private fun addBillHeader() {
        val map = HashMap<String,Any>()
        map.put("email",et_email.textStr)
        map.put("type",type)
        map.put("title",et_title.textStr)
        map.put("phone",et_phone.textStr)
        map.put("tax_no",et_duty.textStr)
        map.put("address",et_address.textStr)
        map.put("telphone",et_number.textStr)
        map.put("id",id!!)
        SoguApi.getService(application,OtherService::class.java)
                .addBillHeader(map)
                .execute {
                    onNext {
                        payload ->
                        if (payload.isOk){
                            showSuccessToast("添加成功")
                            finish()
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("添加失败")
                    }
                }
    }

    override fun afterTextChanged(s: Editable?) {
      if (et_title.textStr.length > 0){
          iv_delete.setVisible(true)
      }else{
          iv_delete.setVisible(false)
      }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
}