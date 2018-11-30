package com.sogukj.pe.module.receipt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.citypicker.CityConfig
import com.sogukj.pe.baselibrary.widgets.citypicker.CityPickerView
import com.sogukj.pe.baselibrary.widgets.citypicker.OnCityItemClickListener
import com.sogukj.pe.baselibrary.widgets.citypicker.bean.CityBean
import com.sogukj.pe.baselibrary.widgets.citypicker.bean.DistrictBean
import com.sogukj.pe.baselibrary.widgets.citypicker.bean.ProvinceBean
import com.sogukj.pe.bean.InvoiceHisBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.peUtils.Store
import kotlinx.android.synthetic.main.fragment_eletron_bill.*
import kotlinx.android.synthetic.main.layout_accept_type.*
import kotlinx.android.synthetic.main.layout_bill_detail.*

/**
 * Created by CH-ZH on 2018/10/18.
 * 电子发票
 */
class ElectronBillFragment : Fragment(), TextWatcher, ShowMoreCallBack{
    private var rootView: View? = null
    private var money = "0.00"
    private var mCityPickerView: CityPickerView? = null
    private var title_type: Int = 1   // 1 企业发票 2 个人发票
    private var explain = ""
    private var phoneAddress = ""
    private var bankAccount = ""
    private var type: Int = 2  // 1 : 电子发票 2 : 纸质发票
    private var userBean : UserBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments!!.getInt("type")
        money = arguments!!.getString("money")
        userBean = Store.store.getUser(activity!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_eletron_bill, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
        bindListener()
    }

    private fun initView() {
        when (type) {
            1 -> {
                tv_tips.text = getString(R.string.electron_tips)
                ll_accept.setVisible(false)
                view_accept.setVisible(false)
                ll_phone.setVisible(false)
                view_phone.setVisible(false)
                ll_city.setVisible(false)
                view_city.setVisible(false)
                ll_address.setVisible(false)
                view_address.setVisible(false)
            }
            2 -> {
                tv_tips.text = getString(R.string.paper_tips)
                ll_accept.setVisible(true)
                view_accept.setVisible(true)
                ll_phone.setVisible(true)
                view_phone.setVisible(true)
                ll_city.setVisible(true)
                view_city.setVisible(true)
                ll_address.setVisible(true)
                view_address.setVisible(true)
            }
        }
        mCityPickerView = CityPickerView()
        mCityPickerView!!.init(activity)
    }

    private fun initData() {
        tv_coin.text = "${money}元"
        val digits = getString(R.string.input_number_letter)
        if (!userBean!!.phone.isNullOrEmpty()){
            et_phone.setText(userBean!!.phone)
        }

        if (!userBean!!.person_email.isNullOrEmpty()){
            et_email.setText(userBean!!.person_email)
        }
        if (!userBean!!.mechanism_name.isNullOrEmpty()){
            tv_header.setText(userBean!!.mechanism_name)
        }
        if (!userBean!!.tax_no.isNullOrEmpty()){
            et_duty.setText(Utils.getSpaceText(userBean!!.tax_no))
            et_duty.setSelection(et_duty.textStr.length)
        }
        if (!userBean!!.name.isNullOrEmpty()){
            et_accept.setText(userBean!!.name)
        }
    }

    private fun bindListener() {
        tv_header.clickWithTrigger {
            BillHeadSearchActivity.invokeForResult(this, BILL_SEARCH, tv_header.textStr)
        }
        et_duty.inputType = InputType.TYPE_CLASS_TEXT
        et_duty.addTextChangedListener(AddSpaceTextWatcher())
        et_accept.addTextChangedListener(this)
        et_phone.addTextChangedListener(this)
        et_detail.addTextChangedListener(this)
        et_email.addTextChangedListener(this)

        ll_city.clickWithTrigger {
            //所在地区
            Utils.forceCloseInput(activity, et_accept)
            val config = CityConfig.Builder().title("选择城市").build()
            config.defaultProvinceName = tv_province.textStr
            config.defaultCityName = tv_city.textStr
            config.defaultDistrict = tv_district.textStr
            mCityPickerView!!.setConfig(config)
            mCityPickerView!!.setOnCityItemClickListener(object : OnCityItemClickListener() {
                override fun onSelected(province: ProvinceBean?, city: CityBean?, district: DistrictBean?) {
                    tv_province.text = province!!.name
                    tv_city.text = city!!.name
                    tv_district.text = district!!.name
                }

                override fun onCancel() {

                }
            })

            mCityPickerView!!.showCityPicker()
        }

        ll_submit!!.clickWithTrigger {
            //提交
            if (isSubmitEnbale) {
                if (type == 2 && !Utils.isMobile(et_phone.textStr)) {
                    getCreateBillActivity().showCustomToast(R.drawable.icon_toast_common, "请填写正确的手机号")
                    return@clickWithTrigger
                }
                if (!Utils.isEmail(et_email.textStr)) {
                    getCreateBillActivity().showCustomToast(R.drawable.icon_toast_common, "请填写正确的邮箱")
                    return@clickWithTrigger
                }
                if (title_type == 1 && !Utils.isDutyCode(et_duty.textStr)){
                    getCreateBillActivity().showCustomToast(R.drawable.icon_toast_common, "请填写正确的企业税号")
                    return@clickWithTrigger
                }
                submitBillDetail()
            }
        }

        ll_enterprise.clickWithTrigger {
            //企业单位
            if (title_type == 2) {
                iv_enterprise.setImageResource(R.mipmap.ic_unselect_receipt)
                iv_personal.setImageResource(R.mipmap.ic_select_receipt)
                title_type = 1
                ll_duty.setVisible(true)
                view_duty.setVisible(true)
            }
        }

        ll_personal.clickWithTrigger {
            //个人
            if (title_type == 1) {
                iv_enterprise.setImageResource(R.mipmap.ic_select_receipt)
                iv_personal.setImageResource(R.mipmap.ic_unselect_receipt)
                title_type = 2
                ll_duty.setVisible(false)
                view_duty.setVisible(false)
            }
        }

        ll_more.clickWithTrigger {
            //更多信息
            CreateBillDialog.showMoreDialog(activity!!, this, explain, phoneAddress, bankAccount)
        }
    }

    private fun getCreateBillActivity(): CreateBillActivity {
        return activity as CreateBillActivity
    }

    private fun submitBillDetail() {
        val map = HashMap<String, Any>()
        map.put("type", type)
        map.put("email", et_email.textStr)
        map.put("title_type", title_type)
        map.put("title", tv_header.textStr)
        map.put("amount", money.toFloat())
        map.put("content", tv_content.textStr)
        map.put("tax_no", et_duty.textStr)
        map.put("remark", explain)
        map.put("address_tel", phoneAddress)
        map.put("bank_account", bankAccount)
        map.put("receiver", et_accept.textStr)
        map.put("phone", et_phone.textStr)
        map.put("province", tv_province.textStr)
        map.put("city", tv_city.textStr)
        map.put("county", tv_district.textStr)
        map.put("address", et_detail.textStr)
        map.put("order_no", getCreateBillActivity().getOrders()!!)
        CreateBillDialog.showBillDialog(activity!!, map)
    }

    companion object {
        private var ll_submit: View? = null
        private var tv_submit: TextView? = null
        private var isSubmitEnbale = false
        val BILL_SEARCH = 1002
        fun newInstance(type: Int, money: String, ll_submit: View, tv_submit: TextView): ElectronBillFragment {
            this.ll_submit = ll_submit
            this.tv_submit = tv_submit
            val fragment = ElectronBillFragment()
            val bundle = Bundle()
            bundle.putInt("type", type)
            bundle.putString("money", money)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun showMoreDetail(count: Int, explain: String, address: String, bank: String) {
        tv_more.text = "共3项 已填写${count}项"
        this.explain = explain
        this.phoneAddress = address
        this.bankAccount = bank
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

            setCommitButtonStatus()
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
//            et_duty.setText(values)
            editable.replace(0,editable.length,values)
            try {
                et_duty.setSelection(location)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun afterTextChanged(s: Editable?) {
        setCommitButtonStatus()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    private fun setCommitButtonStatus() {
        if (null == tv_submit) return
        if (type == 1) {
            //电子发票
            if (!tv_header.textStr.isNullOrEmpty() && !tv_content.textStr.isNullOrEmpty() && !tv_coin.textStr.isNullOrEmpty()
                    && !et_email.textStr.isNullOrEmpty()) {
                if (title_type == 1) {
                    if (!et_duty.textStr.isNullOrEmpty()) {
                        tv_submit?.let {
                            it.setBackgroundResource(R.drawable.bg_create_pro)
                            isSubmitEnbale = true
                        }
                    } else {
                        tv_submit?.let {
                            it.setBackgroundResource(R.drawable.bg_create_pro_gray)
                            isSubmitEnbale = false
                        }
                    }
                } else {
                    tv_submit?.let {
                        it.setBackgroundResource(R.drawable.bg_create_pro)
                        isSubmitEnbale = true
                    }
                }
            } else {
                tv_submit?.let {
                    it.setBackgroundResource(R.drawable.bg_create_pro_gray)
                    isSubmitEnbale = false
                }
            }
        } else if (type == 2) {
            //纸质发票
            if (!tv_header.textStr.isNullOrEmpty() && !tv_content.textStr.isNullOrEmpty() && !tv_coin.textStr.isNullOrEmpty()
                    && !et_email.textStr.isNullOrEmpty() && !et_accept.textStr.isNullOrEmpty() && !et_phone.textStr.isNullOrEmpty()
                    && !tv_province.textStr.isNullOrEmpty() && !tv_city.textStr.isNullOrEmpty() && !tv_district.textStr.isNullOrEmpty()
                    && !et_detail.textStr.isNullOrEmpty()) {
                if (title_type == 1) {
                    if (!et_duty.textStr.isNullOrEmpty()) {
                        tv_submit?.let {
                            it.setBackgroundResource(R.drawable.bg_create_pro)
                            isSubmitEnbale = true
                        }
                    } else {
                        tv_submit?.let {
                            it.setBackgroundResource(R.drawable.bg_create_pro_gray)
                            isSubmitEnbale = false
                        }
                    }
                } else {
                    tv_submit?.let {
                        it.setBackgroundResource(R.drawable.bg_create_pro)
                        isSubmitEnbale = true
                    }
                }
            } else {
                tv_submit?.let {
                    it.setBackgroundResource(R.drawable.bg_create_pro_gray)
                    isSubmitEnbale = false
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                BILL_SEARCH -> {
                    if (null != data) {
                        val receiptBean = data.getSerializableExtra(Extras.DATA) as InvoiceHisBean
                        if (null != receiptBean) {
                            tv_header.text = receiptBean.title
                            et_duty.setText(Utils.getSpaceText(receiptBean.tax_no))
                            et_duty.setSelection(et_duty.textStr.length)
                        }
                    }
                }
            }
        }
    }
}
