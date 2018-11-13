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
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.layout_add_content.*
import kotlinx.android.synthetic.main.normal_toolbar.*

/**
 * Created by CH-ZH on 2018/10/22.
 * 添加和编辑发票抬头
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
        id = intent.getIntExtra(Extras.ID,0)
        setTitle("发票抬头")
        toolbar_menu.text = "完成"
        toolbar_menu.setTextColor(resources.getColor(R.color.blue_3c))
        toolbar_menu.setVisible(true)

        setPrimeData()
    }

    private fun setPrimeData() {
        if (null == bean) return
        if (bean!!.type == 1){
            //公司
            iv_company.setImageResource(R.mipmap.ic_unselect_receipt)
            iv_personal.setImageResource(R.mipmap.ic_select_receipt)
            type = 1
            ll_other.setVisible(true)
        }else{
            //个人
            iv_company.setImageResource(R.mipmap.ic_select_receipt)
            iv_personal.setImageResource(R.mipmap.ic_unselect_receipt)
            type = 2
            ll_other.setVisible(false)
        }
        et_title.setText(bean!!.title)
        iv_delete.setVisible(true)
        if (!bean!!.title.isNullOrEmpty()){
            et_title.setSelection(bean!!.title.length)
        }
        et_duty.setText(Utils.getSpaceText(bean!!.tax_no))
        if (!bean!!.address.isNullOrEmpty()){
            //公司地址
            et_address.setText(bean!!.address)
        }
        if (!bean!!.telphone.isNullOrEmpty()){
            //电话号码
            et_number.setText(bean!!.telphone)
        }
        if (!bean!!.bank.isNullOrEmpty()){
            //开户银行
            et_bank.setText(bean!!.bank)
        }
        if (!bean!!.account.isNullOrEmpty()){
            //银行账户
            et_account.setText(bean!!.account)
        }
        if (!bean!!.phone.isNullOrEmpty()){
            //手机号
            et_phone.setText(bean!!.phone)
        }
        if (!bean!!.email.isNullOrEmpty()){
            //邮箱
            et_email.setText(bean!!.email)
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
        et_duty.addTextChangedListener(AddSpaceTextWatcher())
        iv_delete.clickWithTrigger {
            et_title.setText("")
            iv_delete.setVisible(false)
        }
    }

    private var maxLength = 24
    inner class AddSpaceTextWatcher : TextWatcher {
        /** text改变之前的长度  */
        private var beforeTextLength = 0
        private val buffer = StringBuffer()
        var spaceCount = 0
        private var onTextLength = 0
        private var isChanged = false
        /** 记录光标的位置  */
        private var location = 0
        /** 是否是主动设置text  */
        private var isSetText = false

        override fun afterTextChanged(s: Editable) {
            if (isChanged) {
                location = et_duty.selectionEnd
                var index = 0
                while (index < buffer.length) { // 删掉所有空格
                    if (buffer[index] == ' ') {
                        buffer.deleteCharAt(index)
                    } else {
                        index++
                    }
                }

                index = 0
                var spaceNumberB = 0
                while (index < buffer.length) { // 插入所有空格
                    spaceNumberB = insertSpace(index, spaceNumberB)
                    index++
                }

                val str = buffer.toString()

                // 下面是计算光位置的
                if (spaceNumberB > spaceCount) {
                    location += spaceNumberB - spaceCount
                    spaceCount = spaceNumberB
                }
                if (isSetText) {
                    location = str.length
                    isSetText = false
                } else if (location > str.length) {
                    location = str.length
                } else if (location < 0) {
                    location = 0
                }
                updateContext(s, str)
                isChanged = false
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            beforeTextLength = s.length
            if (buffer.isNotEmpty()) {
                buffer.delete(0, buffer.length)
            }
            spaceCount = (0 until s.length).count { s[it] == ' ' }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            onTextLength = s.length
            buffer.append(s.toString())
            if (onTextLength == beforeTextLength || onTextLength > maxLength
                    || isChanged) {
                isChanged = false
                return
            }
            isChanged = true
        }

        /**
         * 根据类型插入空格
         *
         * @param index
         * @param spaceNumberAfter
         * @return
         * @see [类、类.方法、类.成员]
         */
        private fun insertSpace(index: Int, spaceNumberAfter: Int): Int {
            var spaceNumberAfter = spaceNumberAfter
            if (index > 3 && index % (4 * (spaceNumberAfter + 1)) == spaceNumberAfter) {
                buffer.insert(index, ' ')
                spaceNumberAfter++
            }
            return spaceNumberAfter
        }

        /**
         * 更新编辑框中的内容
         *
         * @param editable
         * @param values
         */
        private fun updateContext(editable: Editable, values: String) {
            et_duty.setText(values)
            try {
                et_duty.setSelection(location)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                if (!Utils.isDutyCode(et_duty.textStr)){
                    showCustomToast(R.drawable.icon_toast_common, "请填写正确的企业税号")
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
        map.put("bank",et_bank.textStr)
        map.put("account",et_account.textStr)
        SoguApi.getStaticHttp(application)
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