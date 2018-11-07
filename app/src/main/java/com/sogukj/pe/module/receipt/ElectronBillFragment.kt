package com.sogukj.pe.module.receipt

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import kotlinx.android.synthetic.main.fragment_eletron_bill.*
import kotlinx.android.synthetic.main.layout_accept_type.*
import kotlinx.android.synthetic.main.layout_bill_detail.*

/**
 * Created by CH-ZH on 2018/10/18.
 * 电子发票
 */
class ElectronBillFragment : Fragment(), TextWatcher,ShowMoreCallBack {
    private var rootView : View ? = null
    private var money = 0f
    private var mCityPickerView : CityPickerView? = null
    private var title_type : Int = 1   // 1 企业发票 2 个人发票
    private var explain  = ""
    private var phoneAddress = ""
    private var bankAccount = ""
    private var type : Int = 2  // 1 : 电子发票 2 : 纸质发票
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments!!.getInt("type")
        money = arguments!!.getFloat("money",0f)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_eletron_bill,container,false)
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
        when(type){
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
    }

    private fun bindListener() {
        et_header.addTextChangedListener(this)
        et_duty.addTextChangedListener(this)
        et_accept.addTextChangedListener(this)
        et_phone.addTextChangedListener(this)
        et_detail.addTextChangedListener(this)
        et_email.addTextChangedListener(this)

        iv_delete.clickWithTrigger {
            et_header.setText("")
            iv_delete.setVisible(false)
        }

        ll_city.clickWithTrigger {
            //所在地区
            val config = CityConfig.Builder().title("选择城市").build()
            mCityPickerView!!.setConfig(config)
            mCityPickerView!!.setOnCityItemClickListener(object : OnCityItemClickListener(){
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
            if (isSubmitEnbale){
                if (type == 2 && !Utils.isMobile(et_phone.textStr)){
                    getCreateBillActivity().showCustomToast(R.drawable.icon_toast_common, "请填写正确的手机号")
                    return@clickWithTrigger
                }
                if (!Utils.isEmail(et_email.textStr)){
                    getCreateBillActivity().showCustomToast(R.drawable.icon_toast_common, "请填写正确的邮箱")
                    return@clickWithTrigger
                }
                submitBillDetail()
            }
        }

        ll_enterprise.clickWithTrigger {
            //企业单位
            if (title_type == 2){
                iv_enterprise.setImageResource(R.mipmap.ic_unselect_receipt)
                iv_personal.setImageResource(R.mipmap.ic_select_receipt)
                title_type = 1
                ll_duty.setVisible(true)
                view_duty.setVisible(true)
            }
        }

        ll_personal.clickWithTrigger {
            //个人
            if (title_type == 1){
                iv_enterprise.setImageResource(R.mipmap.ic_select_receipt)
                iv_personal.setImageResource(R.mipmap.ic_unselect_receipt)
                title_type = 2
                ll_duty.setVisible(false)
                view_duty.setVisible(false)
            }
        }

        ll_more.clickWithTrigger {
            //更多信息
            CreateBillDialog.showMoreDialog(activity!!,this,explain,phoneAddress,bankAccount)
        }
    }
    private fun getCreateBillActivity():CreateBillActivity{
        return activity as CreateBillActivity
    }
    private fun submitBillDetail() {
        val map = HashMap<String,Any>()
        map.put("type",type)
        map.put("email",et_email.textStr)
        map.put("title_type",title_type)
        map.put("title",et_header.textStr)
        map.put("amount",money)
        map.put("content",tv_content.textStr)
        map.put("tax_no",et_duty.textStr)
        map.put("remark",explain)
        map.put("address_tel",phoneAddress)
        map.put("bank_account",bankAccount)
        map.put("receiver",et_accept.textStr)
        map.put("phone",et_phone.textStr)
        map.put("province",tv_province.textStr)
        map.put("city",tv_city.textStr)
        map.put("county",tv_district.textStr)
        map.put("address",et_detail.textStr)
        map.put("order_no", getCreateBillActivity().getOrders()!!)
        CreateBillDialog.showBillDialog(activity!!,map)
    }

    companion object {
        private var ll_submit : View? = null
        private var tv_submit : TextView ? = null
        private var isSubmitEnbale = false
        fun newInstance(type:Int,money : Float,ll_submit:View,tv_submit:TextView):ElectronBillFragment{
            this.ll_submit = ll_submit
            this.tv_submit = tv_submit
            val fragment = ElectronBillFragment()
            val bundle = Bundle()
            bundle.putInt("type",type)
            bundle.putFloat("money",money)
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

    override fun afterTextChanged(s: Editable?) {
        if (et_header.textStr.length > 0){
            iv_delete.setVisible(true)
        }else{
            iv_delete.setVisible(false)
        }

        if (type == 1){
            //电子发票
            if (!et_header.textStr.isNullOrEmpty() && !tv_content.textStr.isNullOrEmpty() && !tv_coin.textStr.isNullOrEmpty()
            && !et_email.textStr.isNullOrEmpty()){
                if (title_type == 1){
                    if (!et_duty.textStr.isNullOrEmpty()){
                        tv_submit?.let {
                            it.setBackgroundResource(R.drawable.bg_create_pro)
                            isSubmitEnbale = true
                        }
                    }else{
                        tv_submit?.let {
                            it.setBackgroundResource(R.drawable.bg_create_pro_gray)
                            isSubmitEnbale = false
                        }
                    }
                }else{
                    tv_submit?.let {
                        it.setBackgroundResource(R.drawable.bg_create_pro)
                        isSubmitEnbale = true
                    }
                }
            }else{
                tv_submit?.let {
                    it.setBackgroundResource(R.drawable.bg_create_pro_gray)
                    isSubmitEnbale = false
                }
            }
        }else if (type == 2){
            //纸质发票
            if (!et_header.textStr.isNullOrEmpty() && !tv_content.textStr.isNullOrEmpty() && !tv_coin.textStr.isNullOrEmpty()
                    && !et_email.textStr.isNullOrEmpty() && !et_accept.textStr.isNullOrEmpty()&&!et_phone.textStr.isNullOrEmpty()
                    && !tv_province.textStr.isNullOrEmpty() && !tv_city.textStr.isNullOrEmpty() && !tv_district.textStr.isNullOrEmpty()
                    && !et_detail.textStr.isNullOrEmpty()){
                if (title_type == 1){
                    if (!et_duty.textStr.isNullOrEmpty()){
                        tv_submit?.let {
                            it.setBackgroundResource(R.drawable.bg_create_pro)
                            isSubmitEnbale = true
                        }
                    }else{
                        tv_submit?.let {
                            it.setBackgroundResource(R.drawable.bg_create_pro_gray)
                            isSubmitEnbale = false
                        }
                    }
                }else{
                    tv_submit?.let {
                        it.setBackgroundResource(R.drawable.bg_create_pro)
                        isSubmitEnbale = true
                    }
                }
            }else{
                tv_submit?.let {
                    it.setBackgroundResource(R.drawable.bg_create_pro_gray)
                    isSubmitEnbale = false
                }
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
}