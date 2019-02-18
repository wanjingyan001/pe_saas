package com.sogukj.pe.module.receipt

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.alipay.sdk.app.PayTask
import com.google.gson.Gson
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.utils.ToastUtils
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.citypicker.CityConfig
import com.sogukj.pe.baselibrary.widgets.citypicker.CityPickerView
import com.sogukj.pe.baselibrary.widgets.citypicker.OnCityItemClickListener
import com.sogukj.pe.baselibrary.widgets.citypicker.bean.CityBean
import com.sogukj.pe.baselibrary.widgets.citypicker.bean.DistrictBean
import com.sogukj.pe.baselibrary.widgets.citypicker.bean.ProvinceBean
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.dataSource.DocumentsListActivity
import com.sogukj.pe.module.user.PayManagerActivity
import com.sogukj.pe.peUtils.ShareUtils.Companion.mHandler
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.service.SoguApi
import com.tencent.mm.sdk.constants.Build
import com.tencent.mm.sdk.openapi.IWXAPI
import com.tencent.mm.sdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.fragment_eletron_bill.*
import kotlinx.android.synthetic.main.layout_accept_type.*
import kotlinx.android.synthetic.main.layout_bill_detail.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.progressDialog
import org.jetbrains.anko.support.v4.startActivity

/**
 * Created by CH-ZH on 2018/10/18.
 * 电子发票
 */
class ElectronBillFragment : Fragment(), TextWatcher, ShowMoreCallBack {
    private var rootView: View? = null
    private var money = "0.00"
    private var mCityPickerView: CityPickerView? = null
    private var title_type: Int = 1   // 1 企业发票 2 个人发票
    private var explain = ""
    private var phoneAddress = ""
    private var bankAccount = ""
    var type: Int = 1  // 1 : 电子发票 2 : 纸质发票
    private var userBean: UserBean? = null
    private var isSubmitEnbale = false
    private var map: HashMap<String, Any> = HashMap()
    private var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg!!.what) {
                DocumentsListActivity.SDK_PAY_FLAG -> {
                    val pair = msg.obj as Pair<Map<String, String>, () -> Unit>
                    val payResult = PayResultInfo(pair.first)
                    /**
                    对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    val resultInfo = payResult.result// 同步返回需要验证的信息
                    val resultStatus = payResult.resultStatus
                    Log.e("TAG", " resultStatus ===$resultStatus")
                    // 判断resultStatus 为9000则代表支付成功
                    when {
                        TextUtils.equals(resultStatus, "9000") -> {
                            // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                            ToastUtils.showSuccessToast("支付成功", ctx)
                            pair.second.invoke()
                        }
                        TextUtils.equals(resultStatus, "6001") ->
                            ToastUtils.showErrorToast("支付已取消", ctx)
                        else ->
                            // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                            ToastUtils.showErrorToast("支付失败", ctx)
                    }
                }
                else -> {

                }
            }
        }
    }


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
        initWXAPI()
        LocalBroadcastManager.getInstance(ctx).registerReceiver(receiver, IntentFilter(PayManagerActivity.PAYMANAGER_ACTION))
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setCommitButtonStatus()
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            ToastUtils.showSuccessToast("支付成功", ctx)
            submit(map)
        }
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
        if (!userBean!!.phone.isNullOrEmpty()) {
            et_phone.setText(userBean!!.phone)
        }

        if (!userBean!!.person_email.isNullOrEmpty()) {
            et_email.setText(userBean!!.person_email)
        }
        if (!userBean!!.mechanism_name.isNullOrEmpty()) {
            tv_header.text = userBean!!.mechanism_name
        }
        if (!userBean!!.tax_no.isNullOrEmpty()) {
            et_duty.setText(Utils.getSpaceText(userBean!!.tax_no))
            et_duty.setSelection(et_duty.textStr.length)
            isSubmitEnbale = if (CreateBillActivity.currentType == 1 && userBean!!.tax_no.isNotEmpty()) {
                tv_submit?.setBackgroundResource(R.drawable.bg_create_pro)
                true
            } else {
                tv_submit?.setBackgroundResource(R.drawable.bg_create_pro_gray)
                false
            }
        }
        if (!userBean!!.name.isNullOrEmpty()) {
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
                if (CreateBillActivity.currentType == 2) {
                    if (!Utils.isMobile(et_phone?.textStr)) {
                        ToastUtils.showWarnToast("请填写正确的手机号", ctx)
                        return@clickWithTrigger
                    }
                }
                if (et_email != null && !Utils.isEmail(et_email?.textStr)) {
                    ToastUtils.showWarnToast("请填写正确的邮箱", ctx)
                    return@clickWithTrigger
                }
                if (et_duty != null && title_type == 1 && !Utils.isDutyCode(et_duty?.textStr)) {
                    ToastUtils.showWarnToast("请填写正确的企业税号", ctx)
                    return@clickWithTrigger
                }
                if (CreateBillActivity.currentType == 2) {
                    //todo 纸质发票需要先进行支付运费功能
                    val inflate = LayoutInflater.from(ctx).inflate(R.layout.layout_input_dialog1, null)
                    val dialog = MaterialDialog.Builder(ctx)
                            .customView(inflate, false)
                            .cancelable(true)
                            .build()
                    dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val veto = inflate.find<TextView>(R.id.veto_comment)
                    val confirm = inflate.find<TextView>(R.id.confirm_comment)
                    val title = inflate.find<TextView>(R.id.approval_comments_title)
                    title.text = "纸质发票需要支付20元运费,\n是否继续？"
                    veto.text = "取消"
                    confirm.text = "确定"
                    veto.clickWithTrigger {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                        }
                    }
                    confirm.clickWithTrigger {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                        }
                        submitBillDetail { map ->
                            this.map = map
                            //todo 弹出选择支付方式对话框
                            PayDialog.showPayFareDialog(ctx, object : AllPayCallBack {
                                override fun pay(order_type: Int, count: Int, pay_type: Int, fee: String, tv_per_balance: TextView, iv_pre_select: ImageView, tv_bus_balance: TextView, iv_bus_select: ImageView, tv_per_title: TextView, tv_bus_title: TextView, dialog: Dialog) {
                                    Log.d("WJY", "支付")
                                    //todo 调用接口获取订单信息
                                    getPayInfo(order_type, count, pay_type, fee) {
                                        submit(map)
                                    }
                                }

                                override fun payForOther(id: String, order_type: Int, count: Int, pay_type: Int, fee: String, tv_per_balance: TextView, iv_pre_select: ImageView, tv_bus_balance: TextView, iv_bus_select: ImageView, tv_per_title: TextView, tv_bus_title: TextView, dialog: Dialog, book: PdfBook?) {

                                }

                            })
                        }
                    }
                    dialog.show()
                } else {
                    submitBillDetail()
                }
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


    /**
     * 获取订单信息
     */
    private fun getPayInfo(order_type: Int, count: Int, pay_type: Int, fee: String, block: () -> Unit) {
        val progress = ProgressDialog(ctx)
        SoguApi.getStaticHttp(App.INSTANCE)
                .getAccountPayInfo(order_type, count, pay_type, fee)
                .execute {
                    onSubscribe {
                        progress.show()
                    }
                    onNext { payload ->
                        if (payload.isOk) {
                            when (pay_type) {
                                1, 2 -> {
                                    ToastUtils.showSuccessToast("支付成功", ctx)
                                    block.invoke()
                                }
                                3 -> {
                                    sendToZfbRequest(payload.payload as String?, block)
                                }
                                4 -> {
                                    val orderInfo = payload.payload as? String
                                    if (orderInfo.isNullOrEmpty()) {
                                        ToastUtils.showErrorToast("获取订单失败", ctx)
                                    } else {
                                        sendToWxRequest(orderInfo!!)
                                    }
                                }
                            }
                        } else {
                            ToastUtils.showErrorToast(payload.message!!, ctx)
                        }
                    }
                    onError {
                        ToastUtils.showErrorToast("获取订单信息失败，请重试", ctx)
                    }
                    onComplete {
                        progress.dismiss()
                    }
                }
    }

    /**
     * 支付宝支付
     */
    private fun sendToZfbRequest(commodityInfo: String?, block: () -> Unit) {
        val payRunnable = Runnable {
            val alipay = PayTask(activity)
            val result = alipay.payV2(commodityInfo, true)
            Log.e("TAG", "  result ===$result")
            val msg = Message()
            msg.what = DocumentsListActivity.SDK_PAY_FLAG
            msg.obj = result to block
            mHandler.sendMessage(msg)
        }
        val payThread = Thread(payRunnable)
        payThread.start()
    }

    private var api: IWXAPI? = null

    private fun initWXAPI() {
        if (null == api) {
            api = WXAPIFactory.createWXAPI(ctx, Extras.WEIXIN_APP_ID)
            api!!.registerApp(Extras.WEIXIN_APP_ID)
        }
    }

    private fun inspectWx(): Boolean {
        val sIsWXAppInstalledAndSupported = api!!.isWXAppInstalled && api!!.isWXAppSupportAPI
        return if (!sIsWXAppInstalledAndSupported) {
            ToastUtils.showWarnToast("您未安装微信", ctx)
            false
        } else {
            val isPaySupported = api!!.wxAppSupportAPI >= Build.PAY_SUPPORTED_SDK_INT
            return if (isPaySupported) {
                true
            } else {
                ToastUtils.showWarnToast("您微信版本过低，不支持支付。", ctx)
                false
            }
        }
    }

    /**
     * 微信支付
     */
    private fun sendToWxRequest(orderInfo: String) {
        val wxPayBean = Gson().fromJson<WxPayBean>(orderInfo, WxPayBean::class.java)
        if (!inspectWx()) return
        val req = com.tencent.mm.sdk.modelpay.PayReq()
        req.appId = wxPayBean.appid
        req.nonceStr = wxPayBean.noncestr
        req.packageValue = "Sign=WXPay"
        req.sign = wxPayBean.sign
        req.partnerId = wxPayBean.partnerid
        req.prepayId = wxPayBean.prepayid
        req.timeStamp = wxPayBean.timestamp
        //调起微信支付,如果b等于false说明订单中的参数或者签名错误
        val b = api!!.sendReq(req)
        if (!b) {
            ToastUtils.showErrorToast("订单生成错误", ctx)
        } else {
            XmlDb.open(ctx).set("invokeType", 2)
        }
        Log.e("TAG", "sendReq返回值=$b")
    }


    private fun submit(map: HashMap<String, Any>) {
        SoguApi.getStaticHttp(App.INSTANCE)
                .submitBillDetail(map)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            startActivity<SubmitBillSucActivity>(Extras.TITLE to "开具纸质发票")
                            LocalBroadcastManager.getInstance(ctx).sendBroadcast(Intent(MineReceiptActivity.REFRESH_ACTION))
                            activity?.finish()
                        } else {
                            ToastUtils.showErrorToast(payload.message!!, ctx)
                        }
                    }
                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "提交失败", ctx)
                    }
                }
    }

    private fun submitBillDetail(block: ((map: HashMap<String, Any>) -> Unit)? = null) {
        val duty = find<EditText>(R.id.et_duty)
        if (CreateBillActivity.currentType == 1 && duty.textStr.isEmpty()) {
            ToastUtils.showErrorToast("开具电子发票必须填写税号", ctx)
            return
        }
        val map = HashMap<String, Any>()
        map.put("type", CreateBillActivity.currentType)
        map.put("email", et_email.textStr)
        map.put("title_type", title_type)
        map.put("title", tv_header.textStr)
        map.put("amount", money.toFloat())
        map.put("content", tv_content.textStr)
        map.put("tax_no", et_duty.textStr)
        map.put("remark", explain)
        map.put("address_tel", phoneAddress)
        map.put("bank_account", bankAccount)
        map.put("order_no", getCreateBillActivity().getOrders()!!)
        if (CreateBillActivity.currentType == 2) {
            map.put("receiver", et_accept.textStr)
            map.put("phone", et_phone.textStr)
            map.put("province", tv_province.textStr)
            map.put("city", tv_city.textStr)
            map.put("county", tv_district.textStr)
            map.put("address", et_detail.textStr)
        }
        Log.d("WJY", "map type${CreateBillActivity.currentType}")
        CreateBillDialog.showBillDialog(activity!!, map, block)
    }

    companion object {
        private var ll_submit: View? = null
        private var tv_submit: TextView? = null

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
            editable.replace(0, editable.length, values)
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
