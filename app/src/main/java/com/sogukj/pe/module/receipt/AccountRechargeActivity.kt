package com.sogukj.pe.module.receipt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import com.alipay.sdk.app.PayTask
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.PayResultInfo
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.dataSource.DocumentsListActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.service.SoguApi
import com.tencent.mm.sdk.constants.Build
import com.tencent.mm.sdk.modelpay.PayReq
import com.tencent.mm.sdk.openapi.IWXAPI
import com.tencent.mm.sdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.activity_account_recharge.*
import kotlinx.android.synthetic.main.layout_recharge_type.*
import kotlinx.android.synthetic.main.recharge_top.*

/**
 * Created by CH-ZH on 2018/10/23.
 * 账户充值
 */
class AccountRechargeActivity : ToolbarActivity(), TextWatcher {
    private var title = ""
    private var balance = ""
    private var user : UserBean ? = null
    private var rechargeType = 1 // 1 : 支付宝 2 : 微信
    private var type = 1 // 1 : 个人 2 : 企业
    private var api: IWXAPI? = null
    private var mHandler : Handler = object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg!!.what){
                DocumentsListActivity.SDK_PAY_FLAG -> {
                    val payResult = PayResultInfo(msg.obj as Map<String, String>)
                    /**
                    对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    val resultInfo = payResult.getResult()// 同步返回需要验证的信息
                    val resultStatus = payResult.getResultStatus()
                    Log.e("TAG"," resultStatus ===" + resultStatus)
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        showSuccessToast("支付成功")
                        setResult(Activity.RESULT_OK)
                        finish()

                    } else if (TextUtils.equals(resultStatus, "6001")) {
                        showErrorToast("支付已取消")
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        showErrorToast("支付失败")
                    }
                }
                else -> {

                }
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_recharge)
        Utils.setWindowStatusBarColor(this, R.color.white)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)
        balance = intent.getStringExtra(Extras.DATA)
        title = intent.getStringExtra(Extras.TITLE)
        type = intent.getIntExtra(Extras.TYPE,1)
        user = Store.store.getUser(this)
        setTitle(title)
        initWXAPI()
    }

    private fun initData() {
        et_recharge.setSelection(et_recharge.textStr.length)
        tv_account.text = "账号：${user!!.phone}"
        tv_balance.text = Utils.reserveDecimal(balance.toDouble())
    }

    private fun initWXAPI() {
        if (null == api){
            api = WXAPIFactory.createWXAPI(this,Extras.WEIXIN_APP_ID)
            api!!.registerApp(Extras.WEIXIN_APP_ID)
        }
    }

    private fun bindListener() {
        et_recharge.addTextChangedListener(this)
        tv_commit.clickWithTrigger {
            //支付
            if (!Utils.isInteger(et_recharge.textStr)) return@clickWithTrigger
            getPayOrderInfo()
        }

        rl_wx.clickWithTrigger {
            //微信
            if (rechargeType == 1){
                iv_wx_select.setImageResource(R.mipmap.ic_unselect_receipt)
                iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                rechargeType = 2
            }
        }

        rl_zfb.clickWithTrigger {
            //支付宝
            if (rechargeType == 2){
                iv_zfb_select.setImageResource(R.mipmap.ic_unselect_receipt)
                iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                rechargeType = 1
            }
        }

    }

    private fun getPayOrderInfo() {
        SoguApi.getStaticHttp(application)
                .getPayInfo(type,et_recharge.textStr,rechargeType,Store.store.getUser(this)!!.accid!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val orderInfo = payload.payload
                            if (rechargeType == 1){
                                //支付宝
                                sendToZfbRequest(orderInfo)
                            }else if (rechargeType == 2){
                                //微信
                                sendToWxRequest(orderInfo)
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取订单失败")
                    }
                }
    }

    /**
     * 微信支付
     */
    private fun sendToWxRequest(orderInfo: String?) {
        if (!inspectWx()) return
        val req = PayReq()
        req.appId = ""
        req.nonceStr = ""
        req.packageValue = "Sign=WXPay"
        req.sign = ""
        req.partnerId = ""
        req.prepayId = ""
        req.timeStamp = ""
        //调起微信支付,如果b等于false说明订单中的参数或者签名错误
        val b = api!!.sendReq(req)
        if (!b) {
            showErrorToast("订单生成错误")
        }
        Log.e("TAG", "sendReq返回值=" + b)
    }

    private fun inspectWx(): Boolean {
        val sIsWXAppInstalledAndSupported = api!!.isWXAppInstalled() && api!!.isWXAppSupportAPI()
        if (!sIsWXAppInstalledAndSupported) {
            showCommonToast("您未安装微信")
            return false
        } else {
            val isPaySupported = api!!.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT
            if (isPaySupported) {
                return true
            } else {
                showCommonToast("您微信版本过低，不支持支付。")
                return false
            }
        }
    }

    /**
     * 支付宝支付
     */
    private fun sendToZfbRequest(commodityInfo: String?) {
        val payRunnable = Runnable {
            val alipay = PayTask(this)
            val result = alipay.payV2(commodityInfo, true)
            Log.e("TAG", "  result ===" + result.toString())
            val msg = Message()
            msg.what = DocumentsListActivity.SDK_PAY_FLAG
            msg.obj = result
            mHandler.sendMessage(msg)
        }
        val payThread = Thread(payRunnable)
        payThread.start()
    }

    override fun afterTextChanged(s: Editable?) {
        if (!Utils.isInteger(et_recharge.textStr)){
            tv_commit.setBackgroundResource(R.drawable.bg_create_pro_gray)
        }else{
            tv_commit.setBackgroundResource(R.drawable.bg_create_pro)
        }
        tv_commit.text = "需要支付${et_recharge.textStr}元"
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    companion object {
        fun invokeForResult(context : Activity,title : String,balance:String,type:Int,requestCode: Int){
            val intent = Intent(context,AccountRechargeActivity::class.java)
            intent.putExtra(Extras.TITLE,title)
            intent.putExtra(Extras.DATA,balance)
            intent.putExtra(Extras.TYPE,type)
            context.startActivityForResult(intent,requestCode)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        val code = intent!!.getStringExtra(Extras.WX_PAY_TYPE)
        if (null != code){
            when(code){
                "0" -> {
                    showSuccessToast("支付成功")
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                "-1" -> {
                    showErrorToast("支付失败  errorCode ==" + code)
                }
                "-2" -> {
                    showErrorToast("支付已取消")
                }
                else -> {

                }
            }
        }
        super.onNewIntent(intent)
        setIntent(intent)
    }
}