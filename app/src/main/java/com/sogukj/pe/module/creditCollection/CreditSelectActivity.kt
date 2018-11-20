package com.sogukj.pe.module.creditCollection

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alipay.sdk.app.PayTask
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.MineWalletBean
import com.sogukj.pe.bean.PayResultInfo
import com.sogukj.pe.bean.PdfBook
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.dataSource.DocumentsListActivity
import com.sogukj.pe.module.receipt.AllPayCallBack
import com.sogukj.pe.module.receipt.PayDialog
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.CreditService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_credit_select.*
import org.jetbrains.anko.startActivity

@Route(path = ARouterPath.CreditSelectActivity)
class CreditSelectActivity : ToolbarActivity(), AllPayCallBack {
    private val model by lazy { ViewModelProviders.of(this).get(CreditViewModel::class.java) }
    private var listIsEmpty = true
    private var creditCount = 0
    private var dialog : Dialog? = null
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
                        getCreditTimes()
                        if (null != dialog && dialog!!.isShowing){
                            dialog!!.dismiss()
                        }
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
        setContentView(R.layout.activity_credit_select)
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0)
        toolbar?.setBackgroundColor(resources.getColor(R.color.transparent))
        title = "征信类型选择"
        setBack(true)

        typeLayout1.clickWithTrigger {
            if (creditCount > 0){
                listIsEmpty.yes {
                    startActivity<HundredSearchActivity>()
                }.otherWise {
                    startActivity<NewCreditListActivity>()
                }
            }else{
                PayDialog.showPayCreditDialog(this,1,this)
            }

        }
        typeLayout2.clickWithTrigger {
            //信用查询
            if (creditCount > 0){
                oldCredit()
            }else{
                PayDialog.showPayCreditDialog(this,1,this)
            }

        }

        model.getCreditList().observe(this, Observer { list ->
            list?.let {
                listIsEmpty = it.isEmpty()
            }
        })
        getCreditTimes()
    }

    override fun pay(order_type: Int, count: Int, pay_type: Int, fee: String, tv_per_balance: TextView, iv_pre_select: ImageView,
                     tv_bus_balance: TextView, iv_bus_select: ImageView, tv_per_title: TextView, tv_bus_title: TextView, dialog: Dialog) {
        SoguApi.getStaticHttp(application)
                .getAccountPayInfo(order_type,count,pay_type,fee, Store.store.getUser(this)!!.accid)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if (pay_type == 1 || pay_type == 2){
                                showSuccessToast("支付成功")
                                getCreditTimes()
                                if (dialog.isShowing){
                                    dialog.dismiss()
                                }
                            }else{
                                if (pay_type == 3){
                                    //支付宝
                                    sendToZfbRequest(payload.payload as String?,dialog)
                                }else if (pay_type == 4){
                                    //微信
                                }
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        if (pay_type == 1 || pay_type == 2){
                            showErrorToast("支付失败")
                        }else{
                            showErrorToast("获取订单失败")
                        }
                    }
                }

    }


    /**
     * 支付宝支付
     */
    private fun sendToZfbRequest(commodityInfo: String?,dialog: Dialog) {
        this.dialog = dialog
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

    override fun payForOther(id: String, order_type: Int, count: Int, pay_type: Int, fee: String, tv_per_balance: TextView,
                             iv_pre_select: ImageView, tv_bus_balance: TextView, iv_bus_select: ImageView, tv_per_title: TextView,
                             tv_bus_title: TextView, dialog: Dialog, book: PdfBook?) {

    }

    /**
     * 获取征信个数
     */
    private fun getCreditTimes() {
        SoguApi.getStaticHttp(application)
                .getMineWalletData()
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val list = payload.payload
                            if (null != list && list.size > 0){
                                setMineWalletInfos(list)
                            }
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                    }
                }
    }

    private fun setMineWalletInfos(list: List<MineWalletBean>) {
        list.forEach {
            when(it.type){
                7 -> {
                    if (Utils.isInteger(it.over)){
                        creditCount = it.over.toInt()
                    }
                }
            }
        }
    }

    private fun oldCredit() {
        XmlDb.open(this).set("INNER", "FALSE")
        val first = XmlDb.open(this).get("FIRST", "TRUE")
        if (first == "FALSE") {
            SoguApi.getService(application, CreditService::class.java)
                    .showCreditList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            if (payload.payload == null) {
                                ShareHolderStepActivity.start(context, 1, 0, "")
                            } else {
                                if (payload.payload!!.size == 0) {
                                    ShareHolderStepActivity.start(context, 1, 0, "")
                                } else {
                                    val project = ProjectBean()
                                    project.name = ""
                                    project.company_id = 0
                                    ShareholderCreditActivity.start(context, project)
                                }
                            }
                        } else {
                            ShareHolderStepActivity.start(context, 1, 0, "")
                        }
                    }, { e ->
                        Trace.e(e)
                        ShareHolderStepActivity.start(context, 1, 0, "")
                    })
        } else if (first == "TRUE") {
            ShareHolderDescActivity.start(context, ProjectBean(), "OUTER")
            XmlDb.open(this).set("FIRST", "FALSE")
        }
    }
}
