package com.sogukj.pe.module.project

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.GridLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.alipay.sdk.app.PayTask
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.team.TeamService
import com.netease.nimlib.sdk.team.model.Team
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.*
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.approve.ApproveListActivity
import com.sogukj.pe.module.creditCollection.ShareHolderDescActivity
import com.sogukj.pe.module.creditCollection.ShareholderCreditActivity
import com.sogukj.pe.module.dataSource.DocumentsListActivity
import com.sogukj.pe.module.fund.BookListActivity
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.module.project.archives.EquityListActivity
import com.sogukj.pe.module.project.archives.FinanceListActivity
import com.sogukj.pe.module.project.archives.ManagerActivity
import com.sogukj.pe.module.project.archives.RecordTraceActivity
import com.sogukj.pe.module.project.businessBg.*
import com.sogukj.pe.module.project.businessDev.*
import com.sogukj.pe.module.project.intellectualProperty.CopyrightListActivity
import com.sogukj.pe.module.project.intellectualProperty.ICPListActivity
import com.sogukj.pe.module.project.intellectualProperty.PatentListActivity
import com.sogukj.pe.module.project.listingInfo.*
import com.sogukj.pe.module.project.operate.*
import com.sogukj.pe.module.project.originpro.NewOriginProjectActivity
import com.sogukj.pe.module.project.originpro.OtherProjectShowActivity
import com.sogukj.pe.module.project.originpro.ProjectApprovalShowActivity
import com.sogukj.pe.module.project.originpro.ProjectUploadShowActivity
import com.sogukj.pe.peExtended.needIm
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.NewService
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.widgets.DividerGridItemDecoration
import com.sogukj.service.SoguApi
import com.tencent.mm.sdk.constants.Build
import com.tencent.mm.sdk.openapi.IWXAPI
import com.tencent.mm.sdk.openapi.WXAPIFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_detail.*
import kotlinx.android.synthetic.main.layout_project_header.view.*
import org.jetbrains.anko.*

class ProjectDetailActivity : ToolbarActivity(), BaseQuickAdapter.OnItemClickListener {
    lateinit var project: ProjectBean
    var position = 0
    var type = 0
    private lateinit var detailAdapter: ProjectDetailAdapter
    private val detailModules = mutableListOf<ProjectModules>()
    private lateinit var headView: View
    private var projectDetail: ProjectDetailBean? = null
    private val user by lazy { Store.store.getUser(this) }

    private var is_business: Int? = null//非空(1=>有价值 ,2=>无价值)
    private var is_ability: Int? = null//非空(1=>有能力,2=>无能力)
    private var isHidden: Int = 0
    private var isStartOpen = false
    private var api: IWXAPI? = null
    private var NAVIGATION_GESTURE: String = when {
        Rom.isEmui() -> "navigationbar_is_min"
        Rom.isMiui() -> "force_fsg_nav_bar"
        else -> "navigation_gesture_on"
    }

    companion object {
        val PROJECT_ACTION = "project_action"
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, ProjectDetailActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Context, project: ProjectBean, type: Int, position: Int) {
            val intent = Intent(ctx, ProjectDetailActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.CODE, position)
            if (ctx is Activity) {
                ctx.startActivityForResult(intent, 0x001)
            } else if (ctx is Fragment) {
                ctx.startActivityForResult(intent, 0x001)
            }
        }

        fun start(ctx: Fragment, project: ProjectBean, type: Int, position: Int) {
            val intent = Intent(ctx.activity, ProjectDetailActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.CODE, position)
            ctx.startActivityForResult(intent, 0x001)
        }
    }

    private var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg!!.what) {
                DocumentsListActivity.SDK_PAY_FLAG -> {
                    val payResult = PayResultInfo(msg.obj as Map<String, String>)
                    /**
                    对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    val resultInfo = payResult.getResult()// 同步返回需要验证的信息
                    val resultStatus = payResult.getResultStatus()
                    Log.e("TAG", " resultStatus ===" + resultStatus)
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        showSuccessToast("支付成功")
                        getSentimentStatus(project.company_id!!)
                        gonePayDialog()
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
        setContentView(R.layout.activity_project_detail)
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0)
        setBack(true)
        toolbar?.setBackgroundColor(resources.getColor(R.color.transparent))
        initDataWithIntent()
        deviceHasNavigationBar()
        initView()
        initDetailsList()
        getProjectDetail(project.company_id!!)
        getSentimentStatus(project.company_id!!)
        bindListener()
        initWXAPI()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(PROJECT_ACTION))
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            showSuccessToast("支付成功")
            getSentimentStatus(project.company_id!!)
            gonePayDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initWXAPI() {
        if (null == api) {
            api = WXAPIFactory.createWXAPI(this, Extras.WEIXIN_APP_ID)
            api!!.registerApp(Extras.WEIXIN_APP_ID)
        }
    }

    private fun bindListener() {
        headView.iv_button.clickWithTrigger {
            setSentimentStatus(project.company_id!!)
        }

        headView.tv_buy.clickWithTrigger {
            //立即购买
            showPayDialog()
        }
    }

    private var isClickPer = false
    private var isClickBus = false
    private var perBalance = "" //个人账户余额
    private var busBalance = "" //企业账户余额
    private var dialog: Dialog? = null
    private fun showPayDialog() {
        dialog = MaterialDialog.Builder(context)
                .theme(Theme.DARK)
                .customView(R.layout.layout_show_pay, false)
                .canceledOnTouchOutside(false)
                .build()
        dialog!!.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.show()
        val iv_close = dialog!!.find<ImageView>(R.id.iv_close)
        val tv_subtract = dialog!!.find<TextView>(R.id.tv_subtract)
        val fl_subtract = dialog!!.find<FrameLayout>(R.id.fl_subtract)
        val et_count = dialog!!.find<EditText>(R.id.et_count)
        val tv_add = dialog!!.find<TextView>(R.id.tv_add)
        val fl_add = dialog!!.find<FrameLayout>(R.id.fl_add)
        val tv_coin = dialog!!.find<TextView>(R.id.tv_coin)
        val rl_bus = dialog!!.find<RelativeLayout>(R.id.rl_bus)
        val tv_bus_balance = dialog!!.find<TextView>(R.id.tv_bus_balance)
        val iv_bus_select = dialog!!.find<ImageView>(R.id.iv_bus_select)
        val rl_pre = dialog!!.find<RelativeLayout>(R.id.rl_pre)
        val tv_per_balance = dialog!!.find<TextView>(R.id.tv_per_balance)
        val iv_pre_select = dialog!!.find<ImageView>(R.id.iv_pre_select)
        val rl_wx = dialog!!.find<RelativeLayout>(R.id.rl_wx)
        val iv_wx_select = dialog!!.find<ImageView>(R.id.iv_wx_select)
        val rl_zfb = dialog!!.find<RelativeLayout>(R.id.rl_zfb)
        val iv_zfb_select = dialog!!.find<ImageView>(R.id.iv_zfb_select)
        val tv_pay = dialog!!.find<TextView>(R.id.tv_pay)
        val tv_bus_title = dialog!!.find<TextView>(R.id.tv_bus_title)
        val tv_per_title = dialog!!.find<TextView>(R.id.tv_per_title)
        var count = 1 //订单数量
        var coin = 9.9
        var isCheckPer = false
        var isCheckBus = false
        var isCheckWx = false
        var isCheckZfb = true
        var pay_type = 3 //3 支付宝 4 微信 1 个人账号 2 企业账号
        et_count.setSelection(et_count.textStr.length)
        et_count.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (et_count.textStr.isNullOrEmpty()) {
                    showCommonToast("购买数量不能为空")
                    et_count.setText("1")
                    return
                }
                if (et_count.textStr.startsWith("0")) {
                    showCommonToast("购买数量最少为一个")
                    et_count.setText("1")
                }
                et_count.setSelection(et_count.textStr.length)
                count = et_count.textStr.toInt()
                coin = Utils.reserveTwoDecimal(9.9 * count, 2)
                tv_coin.text = "￥${coin}"

                if (coin > perBalance.toDouble()) {
                    iv_pre_select.setImageResource(R.mipmap.ic_gray_receipt)
                    tv_per_title.setTextColor(resources.getColor(R.color.gray_a0))
                    tv_per_balance.setTextColor(resources.getColor(R.color.gray_a0))
                    isClickPer = false
                } else {
                    if (!isCheckPer) {
                        iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                    }
                    tv_per_title.setTextColor(resources.getColor(R.color.black_28))
                    tv_per_balance.setTextColor(resources.getColor(R.color.gray_80))
                    isClickPer = true
                }

                if (coin > busBalance.toDouble()) {
                    iv_bus_select.setImageResource(R.mipmap.ic_gray_receipt)
                    tv_bus_title.setTextColor(resources.getColor(R.color.gray_a0))
                    tv_bus_balance.setTextColor(resources.getColor(R.color.gray_a0))
                    isClickBus = false
                } else {
                    if (!isCheckBus) {
                        iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                    }
                    tv_bus_title.setTextColor(resources.getColor(R.color.black_28))
                    tv_bus_balance.setTextColor(resources.getColor(R.color.gray_80))
                    isClickBus = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        iv_close.clickWithTrigger {
            if (dialog!!.isShowing) {
                dialog!!.dismiss()
            }
        }

        fl_subtract.setOnClickListener {
            //减
            count = et_count.textStr.toInt()
            count--
            if (count <= 1) {
                count = 1
            }
            coin = Utils.reserveTwoDecimal(9.9 * count, 2)
            tv_coin.text = "￥${coin}"
            et_count.setText(count.toString())
            et_count.setSelection(et_count.textStr.length)

            if (coin > perBalance.toDouble()) {
                iv_pre_select.setImageResource(R.mipmap.ic_gray_receipt)
                tv_per_title.setTextColor(resources.getColor(R.color.gray_a0))
                tv_per_balance.setTextColor(resources.getColor(R.color.gray_a0))
                isClickPer = false
            } else {
                if (!isCheckPer) {
                    iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                }
                tv_per_title.setTextColor(resources.getColor(R.color.black_28))
                tv_per_balance.setTextColor(resources.getColor(R.color.gray_80))
                isClickPer = true
            }

            if (coin > busBalance.toDouble()) {
                iv_bus_select.setImageResource(R.mipmap.ic_gray_receipt)
                tv_bus_title.setTextColor(resources.getColor(R.color.gray_a0))
                tv_bus_balance.setTextColor(resources.getColor(R.color.gray_a0))
                isClickBus = false
            } else {
                if (!isCheckBus) {
                    iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                }
                tv_bus_title.setTextColor(resources.getColor(R.color.black_28))
                tv_bus_balance.setTextColor(resources.getColor(R.color.gray_80))
                isClickBus = true
            }
        }

        fl_add.setOnClickListener {
            //加
            count++
            coin = Utils.reserveTwoDecimal(9.9 * count, 2)
            tv_coin.text = "￥${coin}"
            et_count.setText(count.toString())
            et_count.setSelection(et_count.textStr.length)

            if (coin > perBalance.toDouble()) {
                iv_pre_select.setImageResource(R.mipmap.ic_gray_receipt)
                tv_per_title.setTextColor(resources.getColor(R.color.gray_a0))
                tv_per_balance.setTextColor(resources.getColor(R.color.gray_a0))
                isClickPer = false
            } else {
                if (!isCheckPer) {
                    iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                }
                tv_per_title.setTextColor(resources.getColor(R.color.black_28))
                tv_per_balance.setTextColor(resources.getColor(R.color.gray_80))
                isClickPer = true
            }

            if (coin > busBalance.toDouble()) {
                iv_bus_select.setImageResource(R.mipmap.ic_gray_receipt)
                tv_bus_title.setTextColor(resources.getColor(R.color.gray_a0))
                tv_bus_balance.setTextColor(resources.getColor(R.color.gray_a0))
                isClickBus = false
            } else {
                if (!isCheckBus) {
                    iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                }
                tv_bus_title.setTextColor(resources.getColor(R.color.black_28))
                tv_bus_balance.setTextColor(resources.getColor(R.color.gray_80))
                isClickBus = true
            }
        }
        getPerAccountInfo(tv_per_balance, iv_pre_select, tv_per_title, false, coin)
        getBusAccountInfo(tv_bus_balance, iv_bus_select, tv_bus_title, false, coin)
        rl_bus.clickWithTrigger {
            //企业账户
            if (isClickBus) {
                if (!isCheckBus) {
                    isCheckBus = !isCheckBus
                    iv_bus_select.setImageResource(R.mipmap.ic_unselect_receipt)
                    if (isClickPer) {
                        iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckPer = false
                    }
                    iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                    iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckWx = false
                    isCheckZfb = false

                    pay_type = 2
                }
            }
        }

        rl_pre.clickWithTrigger {
            //个人账户
            if (isClickPer) {
                if (!isCheckPer) {
                    isCheckPer = !isCheckPer
                    iv_pre_select.setImageResource(R.mipmap.ic_unselect_receipt)
                    if (isClickBus) {
                        iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckBus = false
                    }
                    iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                    iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckWx = false
                    isCheckZfb = false

                    pay_type = 1
                }
            }
        }

        rl_wx.clickWithTrigger {
            //微信
            if (!isCheckWx) {
                isCheckWx = !isCheckWx
                iv_wx_select.setImageResource(R.mipmap.ic_unselect_receipt)
                iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                if (isClickBus) {
                    iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckBus = false
                }
                if (isClickPer) {
                    iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckPer = false
                }
                isCheckZfb = false
                pay_type = 4
            }
        }

        rl_zfb.clickWithTrigger {
            //支付宝
            if (!isCheckZfb) {
                isCheckZfb = !isCheckZfb
                iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                iv_zfb_select.setImageResource(R.mipmap.ic_unselect_receipt)
                if (isClickBus) {
                    iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckBus = false
                }
                if (isClickPer) {
                    iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckPer = false
                }
                isCheckWx = false

                pay_type = 3
            }
        }

        tv_pay.clickWithTrigger {
            //去支付
            goToPay(4, count, pay_type, coin.toString(), tv_per_balance, iv_pre_select, tv_bus_balance, iv_bus_select, tv_per_title, tv_bus_title, coin)
        }
    }

    private fun getBusAccountInfo(tv_bus_balance: TextView, iv_bus_select: ImageView,
                                  tv_bus_title: TextView, isRefresh: Boolean, coin: Double) {
        SoguApi.getStaticHttp(application)
                .getBussAccountInfo()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val recordBean = payload.payload
                            if (null != recordBean) {
                                busBalance = recordBean.balance
                                tv_bus_balance.text = "账户余额：${Utils.reserveDecimal(recordBean.balance.toDouble())}"
                                if (coin > recordBean.balance.toDouble()) {
                                    iv_bus_select.setImageResource(R.mipmap.ic_gray_receipt)
                                    tv_bus_title.setTextColor(resources.getColor(R.color.gray_a0))
                                    tv_bus_balance.setTextColor(resources.getColor(R.color.gray_a0))
                                    isClickBus = false
                                } else {
                                    if (recordBean.balance.equals("0") || recordBean.balance.equals("")) {
                                        iv_bus_select.setImageResource(R.mipmap.ic_gray_receipt)
                                        tv_bus_title.setTextColor(resources.getColor(R.color.gray_a0))
                                        tv_bus_balance.setTextColor(resources.getColor(R.color.gray_a0))
                                        isClickBus = false
                                    } else {
                                        if (!isRefresh) {
                                            iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                                        }
                                        tv_bus_title.setTextColor(resources.getColor(R.color.black_28))
                                        tv_bus_balance.setTextColor(resources.getColor(R.color.gray_80))
                                        isClickBus = true
                                    }
                                }
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取企业账号信息失败")
                    }
                }
    }

    private fun getPerAccountInfo(tv_per_balance: TextView, iv_pre_select: ImageView,
                                  tv_per_title: TextView, isRefresh: Boolean, coin: Double) {
        SoguApi.getStaticHttp(application)
                .getPersonAccountInfo()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val recordBean = payload.payload
                            if (null != recordBean) {
                                perBalance = recordBean.balance
                                tv_per_balance.text = "账户余额：${Utils.reserveDecimal(recordBean.balance.toDouble())}"
                                if (coin > recordBean.balance.toDouble()) {
                                    iv_pre_select.setImageResource(R.mipmap.ic_gray_receipt)
                                    tv_per_title.setTextColor(resources.getColor(R.color.gray_a0))
                                    tv_per_balance.setTextColor(resources.getColor(R.color.gray_a0))
                                    isClickPer = false
                                } else {
                                    if (recordBean.balance.equals("0") || recordBean.balance.equals("")) {
                                        iv_pre_select.setImageResource(R.mipmap.ic_gray_receipt)
                                        tv_per_title.setTextColor(resources.getColor(R.color.gray_a0))
                                        tv_per_balance.setTextColor(resources.getColor(R.color.gray_a0))
                                        isClickPer = false
                                    } else {
                                        if (!isRefresh) {
                                            iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                                        }
                                        tv_per_title.setTextColor(resources.getColor(R.color.black_28))
                                        tv_per_balance.setTextColor(resources.getColor(R.color.gray_80))
                                        isClickPer = true
                                    }
                                }
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取个人账号信息失败")
                    }
                }
    }

    private fun gonePayDialog() {
        if (null != dialog && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }

    private fun goToPay(order_type: Int, count: Int, pay_type: Int, fee: String,
                        tv_per_balance: TextView, iv_pre_select: ImageView,
                        tv_bus_balance: TextView, iv_bus_select: ImageView,
                        tv_per_title: TextView, tv_bus_title: TextView, coin: Double) {
        SoguApi.getStaticHttp(application)
                .getAccountPayInfo(order_type, count, pay_type, fee, Store.store.getUser(this)!!.accid)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            if (pay_type == 1 || pay_type == 2) {
                                showSuccessToast("支付成功")
                                refreshAccountData(tv_per_balance, iv_pre_select, tv_bus_balance, iv_bus_select, tv_per_title, tv_bus_title, coin)
                                getSentimentStatus(project.company_id!!)
                                gonePayDialog()
                            } else {
                                if (pay_type == 3) {
                                    //支付宝
                                    sendToZfbRequest(payload.payload as String?)
                                } else if (pay_type == 4) {
                                    //微信
                                    val orderInfo = payload.payload as String
                                    if (null != orderInfo) {
                                        sendToWxRequest(orderInfo)
                                    } else {
                                        showErrorToast("获取订单失败")
                                    }
                                }
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        if (pay_type == 1 || pay_type == 2) {
                            showErrorToast("支付失败")
                        } else {
                            showErrorToast("获取订单失败")
                        }
                    }
                }
    }

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
            showErrorToast("订单生成错误")
        } else {
            XmlDb.open(this).set("invokeType", 3)
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

    override fun onNewIntent(intent: Intent?) {
        val code = intent!!.getStringExtra(Extras.WX_PAY_TYPE)
        Log.e("TAG", "  onNewIntent -- code ==" + code)
        if (null != code) {
            when (code) {
                "0" -> {
                    showSuccessToast("支付成功")
                    getSentimentStatus(project.company_id!!)
                    gonePayDialog()
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

    /**
     * 支付宝支付
     */
    private fun sendToZfbRequest(commodityInfo: String?) {
        val payRunnable = Runnable {
            val alipay = PayTask(this)
            val result = alipay.payV2(commodityInfo, true)
            val msg = Message()
            msg.what = DocumentsListActivity.SDK_PAY_FLAG
            msg.obj = result
            mHandler.sendMessage(msg)
        }
        val payThread = Thread(payRunnable)
        payThread.start()
    }

    private fun refreshAccountData(tv_per_balance: TextView, iv_pre_select: ImageView,
                                   tv_bus_balance: TextView, iv_bus_select: ImageView, tv_per_title: TextView, tv_bus_title: TextView, coin: Double) {
        getPerAccountInfo(tv_per_balance, iv_pre_select, tv_per_title, true, coin)
        getBusAccountInfo(tv_bus_balance, iv_bus_select, tv_bus_title, true, coin)
    }

    private fun setSentimentStatus(company_id: Int) {
        SoguApi.getStaticHttp(application)
                .setSentimentStatus(company_id)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            if (isStartOpen) {
                                showSuccessToast("关闭舆情成功")
                                headView.iv_button.setImageResource(R.mipmap.ic_sentiment_off)
                                headView.tv_senti_title.text = "开启舆情监控"
                                headView.tv_senti_time.setVisible(false)
                                headView.ll_times_buy.setVisible(true)
                            } else {
                                showSuccessToast("开启舆情成功")
                                headView.iv_button.setImageResource(R.mipmap.ic_sentiment_on)
                                headView.tv_senti_title.text = "监控到期时间："
                                headView.tv_senti_time.setVisible(true)
                                headView.ll_times_buy.setVisible(true)

                                if (remainder <= 0) {
                                    showPayDialog()
                                }
                            }
                            isStartOpen = !isStartOpen
                        } else {
                            if (!isStartOpen) {
                                showPayDialog()
                            } else {
                                showErrorToast(payload.message)
                            }
                        }
                    }

                    onError {
                        it.printStackTrace()
                        if (isStartOpen) {
                            showSuccessToast("关闭舆情失败")
                        } else {
                            showSuccessToast("开启舆情失败")
                        }
                    }
                }
    }

    private fun getSentimentStatus(company_id: Int) {
        SoguApi.getStaticHttp(application)
                .getSentimentInfo(company_id)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val sentimentInfoBean = payload.payload
                            if (null != sentimentInfoBean) {
                                setSentimentData(sentimentInfoBean)
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                    }
                }
    }

    var remainder = 0
    private fun setSentimentData(info: SentimentInfoBean) {
        remainder = info.remainder
        headView.tv_senti_time.text = info.expire
        headView.tv_times.text = "剩余次数：${info.remainder}次"
        when (info.is_open) {
            0 -> {
                headView.iv_button.setImageResource(R.mipmap.ic_sentiment_off)
                headView.tv_senti_title.text = "开启舆情监控"
                headView.tv_senti_time.setVisible(false)
                headView.ll_times_buy.setVisible(true)

                isStartOpen = false
            }
            1 -> {
                headView.iv_button.setImageResource(R.mipmap.ic_sentiment_on)
                headView.tv_senti_title.text = "监控到期时间："
                headView.tv_senti_time.setVisible(true)
                headView.ll_times_buy.setVisible(true)
                isStartOpen = true
            }
        }
        if (info.remainder <= 0) {
            showPayDialog()
        }
    }

    private fun initDataWithIntent() {
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        position = intent.getIntExtra(Extras.CODE, 0)
        type = intent.getIntExtra(Extras.TYPE, 0)

        isHidden = Store.store.getApproveConfig(this)
    }

    /**
     * 判断设备是否存在NavigationBar
     *
     * @return true 存在, false 不存在
     */
    @SuppressLint("PrivateApi")
    private fun deviceHasNavigationBar() {
        var haveNav = false
        try {
            //1.通过WindowManagerGlobal获取windowManagerService
            // 反射方法：IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
            val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
            val getWmServiceMethod = windowManagerGlobalClass.getDeclaredMethod("getWindowManagerService")
            getWmServiceMethod.isAccessible = true
            //getWindowManagerService是静态方法，所以invoke null
            val iWindowManager = getWmServiceMethod.invoke(null)

            //2.获取windowMangerService的hasNavigationBar方法返回值
            // 反射方法：haveNav = windowManagerService.hasNavigationBar();
            val iWindowManagerClass = iWindowManager.javaClass
            val hasNavBarMethod = iWindowManagerClass.getDeclaredMethod("hasNavigationBar")
            hasNavBarMethod.isAccessible = true
            haveNav = hasNavBarMethod.invoke(iWindowManager) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (haveNav && !navigationGestureEnabled()) {
            val param1 = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            param1.bottomMargin = Utils.dpToPx(ctx, 50)
            //var params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutRoot.layoutParams = param1
        }
    }

    private fun navigationGestureEnabled(): Boolean {
        return Settings.Global.getInt(contentResolver, NAVIGATION_GESTURE, 0) != 0
    }


    private fun initView() {
        Glide.with(context)
                .load(project.logo)
                .apply(RequestOptions().placeholder(R.drawable.default_icon).error(R.drawable.default_icon))
                .into(imgIcon)
        companyTitle.text = project.name
        if (sp.getString(Extras.HTTPURL, "").contains("sr")) {
            proj_stage.visibility = View.INVISIBLE
        } else {
            proj_stage.visibility = View.VISIBLE
        }
        if (isHidden == 1) {
            //隐藏
            proj_stage.visibility = View.INVISIBLE
            edit.visibility = View.GONE
            history.visibility = View.GONE
        } else {
            when (type) {
                ProjectListFragment.TYPE_DY -> {
                    proj_stage.text = "储 备"
                    history.visibility = View.GONE
                }
                ProjectListFragment.TYPE_CB -> {
                    proj_stage.text = "立 项"
                    history.visibility = View.GONE
                }
                ProjectListFragment.TYPE_LX -> {
                    proj_stage.text = "投 决"
                    edit.visibility = View.GONE
                    history.visibility = View.GONE
                }
                ProjectListFragment.TYPE_YT -> {
                    proj_stage.text = "退 出"
                    edit.visibility = View.GONE
                    history.visibility = View.GONE
                    delete.visibility = View.GONE
                    if (project.quit == 1) {
                        history.visibility = View.VISIBLE
                    }
                }
                ProjectListFragment.TYPE_TC -> {
                    proj_stage.visibility = View.GONE
                    edit.visibility = View.GONE
                    delete.visibility = View.GONE
                }
            }
        }

        proj_stage.clickWithTrigger {
            when (type) {
                ProjectListFragment.TYPE_YT -> ProjectTCActivity.start(context, false, project)  //进入新增的退出模块
                else -> doAdd()
            }
        }
        delete.clickWithTrigger {
            doDel()
        }
        edit.clickWithTrigger {
            if (type == ProjectListFragment.TYPE_CB) {
                StoreProjectAddActivity.startEdit(context, project)
            } else if (type == ProjectListFragment.TYPE_DY) {
                ProjectAddActivity.startEdit(context, project)
            }
        }
        history.clickWithTrigger {
            ProjectTcHistoryActivity.start(context, project)
        }
        im.clickWithTrigger {
            projectDetail?.let {
                when (it.type) {
                    0 -> {
                        //群组存在就申请加群
                        showProgress("已发送入群申请")
                        NIMClient.getService(TeamService::class.java).applyJoinTeam(it.group_id, "")
                                .setCallback(object : RequestCallback<Team> {
                                    override fun onFailed(code: Int) {
                                        hideProgress()
                                        if (code == 809) {
                                            NimUIKit.startTeamSession(this@ProjectDetailActivity, it.group_id.toString())
                                        } else {
                                            showCustomToast(R.drawable.icon_toast_common, "申请已发出,等待群主同意")
                                        }
                                    }

                                    override fun onSuccess(param: Team) {
                                        hideProgress()
                                        NimUIKit.startTeamSession(this@ProjectDetailActivity, param.id)
                                    }

                                    override fun onException(exception: Throwable) {
                                        hideProgress()
                                        showCustomToast(R.drawable.icon_toast_fail, exception.message)
                                    }
                                })
                    }
                    1 -> {
                        //可以建群就去建群
                        val alreadySelect = ArrayList<UserBean>()
                        alreadySelect.add(Store.store.getUser(this)!!)
                        ContactsActivity.start(this, alreadySelect, true, true, project = project)
                    }
                    2 -> {
                        //不可以建群就弹提示
                        showCustomToast(R.drawable.icon_toast_fail, "项目群需要该项目负责人创建")
                    }
                }
            }
        }
//        divide2.visibility = View.VISIBLE
        AppBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            val alpha = Math.abs(verticalOffset) * 1.0 / Utils.dpToPx(context, 60)
            down.alpha = 1 - alpha.toFloat()

            if (down.alpha < 0.05) {
                toolbar_title.text = if (project.shortName.isNullOrEmpty()) project.name else project.shortName
            } else {
                toolbar_title.text = ""
            }
        }
    }

    private fun initDetailsList() {
        initDetailHeadView()
        detailAdapter = ProjectDetailAdapter(detailModules)
        detailAdapter.addHeaderView(headView)
        detailList.apply {
            layoutManager = GridLayoutManager(this@ProjectDetailActivity, 4)
            addItemDecoration(DividerGridItemDecoration(this@ProjectDetailActivity))
            adapter = detailAdapter

        }
        detailAdapter.onItemClickListener = this
    }

    private fun initDetailHeadView() {
        headView = layoutInflater.inflate(R.layout.layout_project_header, null)
        is_business = project.is_business
        is_ability = project.is_ability
        when (is_business) {
            1 -> {
                headView.btn_yes.setBackgroundResource(R.drawable.bg_rectangle_blue)
                headView.btn_yes.textColor = Color.parseColor("#ffffff")

                headView.btn_no.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_no.textColor = Color.parseColor("#282828")
            }
            2 -> {
                headView.btn_no.setBackgroundResource(R.drawable.bg_rectangle_blue)
                headView.btn_no.textColor = Color.parseColor("#ffffff")

                headView.btn_yes.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_yes.textColor = Color.parseColor("#282828")
            }
            null -> {
                headView.btn_yes.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_yes.textColor = Color.parseColor("#282828")

                headView.btn_no.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_no.textColor = Color.parseColor("#282828")
            }
        }

        when (is_ability) {
            1 -> {
                headView.btn_you.setBackgroundResource(R.drawable.bg_rectangle_blue)
                headView.btn_you.textColor = Color.parseColor("#ffffff")

                headView.btn_wu.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_wu.textColor = Color.parseColor("#282828")
            }
            2 -> {
                headView.btn_wu.setBackgroundResource(R.drawable.bg_rectangle_blue)
                headView.btn_wu.textColor = Color.parseColor("#ffffff")

                headView.btn_you.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_you.textColor = Color.parseColor("#282828")
            }
            null -> {
                headView.btn_you.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_you.textColor = Color.parseColor("#282828")

                headView.btn_wu.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_wu.textColor = Color.parseColor("#282828")
            }
        }
        headView.btn_yes.setOnClickListener {
            if (is_business == 1) {
                return@setOnClickListener
            }
            headView.btn_yes.setBackgroundResource(R.drawable.bg_rectangle_blue)
            headView.btn_yes.textColor = Color.parseColor("#ffffff")

            headView.btn_no.setBackgroundResource(R.drawable.bg_rectangle_white)
            headView.btn_no.textColor = Color.parseColor("#282828")

            is_business = 1

            managerAssess()
        }

        headView.btn_no.setOnClickListener {
            if (is_business == 2) {
                return@setOnClickListener
            }
            headView.btn_no.setBackgroundResource(R.drawable.bg_rectangle_blue)
            headView.btn_no.textColor = Color.parseColor("#ffffff")

            headView.btn_yes.setBackgroundResource(R.drawable.bg_rectangle_white)
            headView.btn_yes.textColor = Color.parseColor("#282828")

            is_business = 2

            managerAssess()
        }

        headView.btn_you.setOnClickListener {
            if (is_ability == 1) {
                return@setOnClickListener
            }
            headView.btn_you.setBackgroundResource(R.drawable.bg_rectangle_blue)
            headView.btn_you.textColor = Color.parseColor("#ffffff")

            headView.btn_wu.setBackgroundResource(R.drawable.bg_rectangle_white)
            headView.btn_wu.textColor = Color.parseColor("#282828")

            is_ability = 1

            managerAssess()
        }

        headView.btn_wu.setOnClickListener {
            if (is_ability == 2) {
                return@setOnClickListener
            }
            headView.btn_wu.setBackgroundResource(R.drawable.bg_rectangle_blue)
            headView.btn_wu.textColor = Color.parseColor("#ffffff")

            headView.btn_you.setBackgroundResource(R.drawable.bg_rectangle_white)
            headView.btn_you.textColor = Color.parseColor("#282828")

            is_ability = 2

            managerAssess()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initIntelligence(yu: Int? = 0, fu: Int? = 0) {
        headView.neg_num.setVisible(true)
        headView.yq_num.setVisible(true)
        if (fu == 0 || fu == null) {
            headView.neg_num.text = "运营良好"
            headView.neg_num.textColor = Color.parseColor("#ff27d2ab")
            headView.neg_num.backgroundResource = R.drawable.neg_yq_bg2
            headView.neg.setOnClickListener(null)
        } else {
            headView.neg_num.text = "${fu}条"
            headView.neg_num.textColor = Color.parseColor("#ffff5858")
            headView.neg_num.backgroundResource = R.drawable.neg_yq_bg
            headView.neg.setOnClickListener {
                ProjectNewsActivity.start(context, "负面讯息", 1, project.company_id!!)
            }
        }
        headView.yq_num.text = "${yu?:0}条"
        if (yu == 0 || yu == null) {
            headView.yuqing.setOnClickListener(null)
        } else {
            headView.yuqing.setOnClickListener {
                ProjectNewsActivity.start(context, "企业舆情", 2, project.company_id!!)
            }
        }
    }


    private fun getProjectDetail(companyId: Int) {
        SoguApi.getService(application, NewService::class.java)
                .projectPage(companyId)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                //如果没有消息，也就不需要im
                                im.setVisible(it.type != 2 && !user?.accid.isNullOrEmpty() && needIm())
                                projectDetail = it
                                it.counts?.forEach {
                                    detailModules.add(ProjectModules(true, it.title!!, it.state!!))
                                    it.value?.forEach {
                                        detailModules.add(ProjectModules(it))
                                    }
                                }
                                initIntelligence(it.yu, it.fu)
                            }
                        } else {
                            payload.message
                        }
                    }
//                    onSubscribe {
//                        Glide.with(this@ProjectDetailActivity)
//                                .asGif()
//                                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
//                                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
//                                .into(iv_loading)
//                        iv_loading?.visibility = View.VISIBLE
//                    }
//                    onComplete {
//                        iv_loading?.visibility = View.GONE
//                    }
//                    onError { e ->
//                        iv_loading?.visibility = View.GONE
//                        Trace.e(e)
//                    }
                }
    }


    private fun managerAssess() {
        ifNotNull(is_business, is_ability, project.company_id) { is_business, is_ability, id ->
            SoguApi.getService(application, OtherService::class.java)
                    .assess(id, is_business, is_ability)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                Log.e("success", "success")
                                project.is_ability = is_ability
                                project.is_business = is_business
                            } else
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }
        }
    }

    private fun doAdd() {
        var titleStr = ""
        when (type) {
            ProjectListFragment.TYPE_DY -> titleStr = "是否添加到储备"
            ProjectListFragment.TYPE_CB -> titleStr = "是否添加到立项"
            ProjectListFragment.TYPE_LX -> titleStr = "是否添加到已投"
            ProjectListFragment.TYPE_YT -> titleStr = "是否添加到退出"
        }
        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog1, null)
        val dialog = MaterialDialog.Builder(this)
                .customView(inflate, false)
                .cancelable(true)
                .build()
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val veto = inflate.find<TextView>(R.id.veto_comment)
        val confirm = inflate.find<TextView>(R.id.confirm_comment)
        val title = inflate.find<TextView>(R.id.approval_comments_title)
        title.text = titleStr
        veto.text = "取消"
        confirm.text = "确定"
        veto.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        confirm.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            val status = when (type) {
                ProjectListFragment.TYPE_DY -> 1
                ProjectListFragment.TYPE_CB -> 2
                ProjectListFragment.TYPE_LX -> 3
                else -> return@setOnClickListener
            }
            SoguApi.getService(application, NewService::class.java)
                    .changeStatus(project.company_id!!, status)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            when (type) {
                                ProjectListFragment.TYPE_DY -> showCustomToast(R.drawable.icon_toast_success, "成功添加到储备")
                                ProjectListFragment.TYPE_CB -> showCustomToast(R.drawable.icon_toast_success, "成功添加到立项")
                                ProjectListFragment.TYPE_LX -> showCustomToast(R.drawable.icon_toast_success, "成功添加到已投")
                                ProjectListFragment.TYPE_YT -> showCustomToast(R.drawable.icon_toast_success, "成功添加到退出")
                            }
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        when (type) {
                            ProjectListFragment.TYPE_DY -> showCustomToast(R.drawable.icon_toast_fail, "添加到储备失败")
                            ProjectListFragment.TYPE_CB -> showCustomToast(R.drawable.icon_toast_fail, "添加到立项失败")
                            ProjectListFragment.TYPE_LX -> showCustomToast(R.drawable.icon_toast_fail, "添加到已投失败")
                            ProjectListFragment.TYPE_YT -> showCustomToast(R.drawable.icon_toast_fail, "添加到退出失败")
                        }
                    })
        }
        dialog.show()
    }

    private fun doDel() {
        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog1, null)
        val dialog = MaterialDialog.Builder(this)
                .customView(inflate, false)
                .cancelable(true)
                .build()
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val veto = inflate.find<TextView>(R.id.veto_comment)
        val confirm = inflate.find<TextView>(R.id.confirm_comment)
        val title = inflate.find<TextView>(R.id.approval_comments_title)
        title.text = "是否删除该项目?"
        veto.text = "取消"
        confirm.text = "确定"
        veto.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        confirm.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            SoguApi.getService(application, NewService::class.java)
                    .delProject(project.company_id!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            showCustomToast(R.drawable.icon_toast_success, "删除成功")
                            val intent1 = Intent()
                            intent1.putExtra(Extras.FLAG, "DELETE")
                            setResult(Activity.RESULT_OK, intent1)
                            finish()
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "删除失败")
                    })
        }
        dialog.show()
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (!isClickEnable(200)) {
            Toast.makeText(this, "请勿重复点击", Toast.LENGTH_SHORT).show()
            return
        }
        if (view.tag in 1..68) {
            SoguApi.getService(application, NewService::class.java)
                    .saveClick(view.tag as Int)
                    .execute {}
        }

        val detailSmallBean = detailModules[position].t
        when (view.tag) {
            38 -> StockInfoActivity.start(this@ProjectDetailActivity, project)//股票行情
            39 -> CompanyInfoActivity.start(this@ProjectDetailActivity, project)//企业简介
            40 -> GaoGuanActivity.start(this@ProjectDetailActivity, project)//高管信息
            41 -> CanGuActivity.start(this@ProjectDetailActivity, project)//参股控股
            42 -> AnnouncementActivity.start(this@ProjectDetailActivity, project)//上市公告
            43 -> ShiDaGuDongActivity.start(this@ProjectDetailActivity, project)//十大股东
            44 -> ShiDaLiuTongGuDongActivity.start(this@ProjectDetailActivity, project)//十大流通
            45 -> IssueRelatedActivity.start(this@ProjectDetailActivity, project)//发行相关
            47 -> EquityChangeActivity.start(this@ProjectDetailActivity, project)//股本变动
            48 -> BonusInfoActivity.start(this@ProjectDetailActivity, project)//分红情况
            49 -> AllotmentListActivity.start(this@ProjectDetailActivity, project)//配股情况
            46 -> GuBenJieGouActivity.start(this@ProjectDetailActivity, project)//股本结构

            1 -> BizInfoActivity.start(this@ProjectDetailActivity, project)//工商信息
            3 -> ShareHolderInfoActivity.start(this@ProjectDetailActivity, project)//股东信息
            8 -> QiYeLianBaoActivity.start(this@ProjectDetailActivity, project)//企业年报
            7 -> ChangeRecordActivity.start(this@ProjectDetailActivity, project)//变更记录
            6 -> InvestmentActivity.start(this@ProjectDetailActivity, project)//对外投资
            5 -> KeyPersonalActivity.start(this@ProjectDetailActivity, project)//主要人员
            4 -> {
                val bean = EquityListBean()
                bean.hid = project.company_id
                EquityStructureActivity.start(this@ProjectDetailActivity, bean, false)
            }//股权结构
            9 -> BranchListActivity.start(this@ProjectDetailActivity, project)//分支机构
            10 -> CompanyInfo2Activity.start(this@ProjectDetailActivity, project)//公司简介

            11 -> FinanceHistoryActivity.start(this@ProjectDetailActivity, project)//融资历史
            12 -> InvestEventActivity.start(this@ProjectDetailActivity, project)//投资事件
            13 -> CoreTeamActivity.start(this@ProjectDetailActivity, project)//核心团队
            14 -> BusinessEventsActivity.start(this@ProjectDetailActivity, project)//企业业务
            15 -> ProductInfoActivity.start(this@ProjectDetailActivity, project)//竞品信息
            32 -> BrandListActivity.start(this@ProjectDetailActivity, project)//商标信息

            16 -> RecruitActivity.start(this@ProjectDetailActivity, project)//招聘信息
            17 -> BondActivity.start(this@ProjectDetailActivity, project)//债券信息
            18 -> TaxRateActivity.start(this@ProjectDetailActivity, project)//税务评级
            19 -> LandPurchaseActivity.start(this@ProjectDetailActivity, project)//购地信息
            20 -> BidsActivity.start(this@ProjectDetailActivity, project)//招投标
            21 -> QualificationListActivity.start(this@ProjectDetailActivity, project)//资质证书
            22 -> CheckListActivity.start(this@ProjectDetailActivity, project)//抽查检查
            23 -> AppListActivity.start(this@ProjectDetailActivity, project)//产品信息

            33 -> PatentListActivity.start(this@ProjectDetailActivity, project)//专利信息
            34 -> CopyrightListActivity.start(this@ProjectDetailActivity, project, 2)//软著权
            35 -> CopyrightListActivity.start(this@ProjectDetailActivity, project, 1)//著作权
            36 -> ICPListActivity.start(this@ProjectDetailActivity, project)//网站备案

            52 -> {
                val stage = when (project.type) {//（4是储备，1是立项，3是关注，5是退出，6是调研）
                    6 -> "调研"
                    4 -> "储备"
                    1 -> "立项"
                    2 -> "已投"
                    5, 7 -> "退出"
                    else -> ""
                }
                BookListActivity.start(context, project.company_id!!, 1, null, "项目文书", project.name!!, stage)
            }
            54 -> StoreProjectAddActivity.startView(this@ProjectDetailActivity, project)//储备信息
            51 -> {
                XmlDb.open(context).set("INNER", "TRUE")
                val first = XmlDb.open(context).get("FIRST", "TRUE")
                if (first == "FALSE") {
                    ShareholderCreditActivity.start(this@ProjectDetailActivity, project)
                } else if (first == "TRUE") {
                    ShareHolderDescActivity.start(this@ProjectDetailActivity, project, "INNER")
                    XmlDb.open(context).set("FIRST", "FALSE")
                }
            }

            // 跟踪记录,尽调数据,投决数据,投后管理数据
            55 -> RecordTraceActivity.start(this@ProjectDetailActivity, project)//跟踪记录
            56 -> ManagerActivity.start(this@ProjectDetailActivity, project, 1, "尽调数据")//尽调数据
            57 -> ManagerActivity.start(this@ProjectDetailActivity, project, 8, "投决数据")//投决数据
            58 -> ManagerActivity.start(this@ProjectDetailActivity, project, 10, "投后管理")//投后管理

            59 -> ApproveListActivity.start(this@ProjectDetailActivity, 5, project.company_id)//审批历史

            60 -> EquityListActivity.start(this@ProjectDetailActivity, project)

            61 -> FinanceListActivity.start(this@ProjectDetailActivity, project)

//            62 -> IncomeCurveActivity.start(this@ProjectDetailActivity, project)//收益曲线
            62 -> startActivity<NewOriginProjectActivity>(Extras.DATA to project) //新建项目
            63 -> startActivity<ProjectApprovalShowActivity>(Extras.DATA to project, Extras.FLAG to detailSmallBean.floor) //立项申请
            64 -> startActivity<ProjectUploadShowActivity>(Extras.DATA to project, Extras.FLAG to detailSmallBean.floor) //预审会
            65 -> startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to detailSmallBean.floor,
                    Extras.TITLE to "投决会") //投决会
            66 -> startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to detailSmallBean.floor,
                    Extras.TITLE to "签约付款")//签约付款
            67 -> startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to detailSmallBean.floor,
                    Extras.TITLE to "投后管理")//投后管理
            68 -> startActivity<OtherProjectShowActivity>(Extras.DATA to project, Extras.FLAG to detailSmallBean.floor,
                    Extras.TITLE to "退出管理")//退出
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra(Extras.DATA, project)
        intent.putExtra(Extras.CODE, position)
        setResult(Activity.RESULT_CANCELED, intent)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001 && resultCode == Activity.RESULT_OK) {
            val step = data?.getIntExtra(Extras.TYPE, 0)
            if (step == 1) {

            } else if (step == 2) {
                //全部退出才会到下一个
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}
