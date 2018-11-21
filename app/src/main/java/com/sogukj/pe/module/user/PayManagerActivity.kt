package com.sogukj.pe.module.user

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.alipay.sdk.app.PayTask
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.dataSource.DocumentsListActivity
import com.sogukj.pe.module.receipt.AllPayCallBack
import com.sogukj.pe.module.receipt.PayDialog
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import com.tencent.mm.sdk.constants.Build
import com.tencent.mm.sdk.openapi.IWXAPI
import com.tencent.mm.sdk.openapi.WXAPIFactory
import kotlinx.android.synthetic.main.activity_pay_manager.*
import org.jetbrains.anko.find

/**
 * Created by CH-ZH on 2018/10/30.
 * 账户管理
 */
class PayManagerActivity : BaseRefreshActivity(), AllPayCallBack {
    private lateinit var adapter : RecyclerAdapter<UserManagerBean.ManagerInfo>
    private lateinit var alreadySelected: MutableSet<UserManagerBean.ManagerInfo>
    private var selectCount = 0
    private var price = "99"
    private var user : UserBean? = null
    private var userAccount = ""
    private var dialog : Dialog? = null
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
                        payRefresh()
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
    companion object {
        val PAYMANAGER_ACTION = "pay_manager_action"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_manager)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle("账户管理")
        rv_manager.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rv_manager.layoutManager = LinearLayoutManager(this)
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        alreadySelected = ArrayList<UserManagerBean.ManagerInfo>().toMutableSet()
        user = Store.store.getUser(this)
        user?.let {
            userAccount = user!!.phone
        }
        initWXAPI()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(PAYMANAGER_ACTION))
    }

    private val receiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            showSuccessToast("支付成功")
            payRefresh()
            if (null != dialog && dialog!!.isShowing){
                dialog!!.dismiss()
            }
        }
    }

    private fun initWXAPI() {
        if (null == api){
            api = WXAPIFactory.createWXAPI(this, Extras.WEIXIN_APP_ID)
            api!!.registerApp(Extras.WEIXIN_APP_ID)
        }
    }

    private fun initData() {
        adapter = RecyclerAdapter(this){_adapter,parent,_ ->
            PayManagerHolder(_adapter.getView(R.layout.pay_manager_item,parent))
        }
        rv_manager.adapter = adapter
        setLoadding()
        getManagerData()
    }

    private fun getManagerData() {
        SoguApi.getService(application,OtherService::class.java)
                .getManagerData()
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val bean = payload.payload
                            if (null != bean){
                                setManagerData(bean)
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                        dofinishRefresh()
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        dofinishRefresh()
                    }
                }
    }

    private fun setManagerData(bean: UserManagerBean) {
        val message = bean.message
        if (null != message){
            price = message.price
            tv_2.text = message.price
            tv_yqcount.text = message.max_yuqing_count+"个"
            tv_zxcount.text = "${message.max_credit_count}个"
            if (null != message.list && message.list.size > 0){
                goneEmpty()
                adapter.dataList.clear()
                adapter.dataList.addAll(message.list)
                adapter.notifyDataSetChanged()
            }else{
                showEmpty()
            }
        }
    }

    inner class PayManagerHolder(itemView: View) : RecyclerHolder<UserManagerBean.ManagerInfo>(itemView) {
        val iv_select = itemView.find<ImageView>(R.id.iv_select)
        val tv_buy = itemView.find<TextView>(R.id.tv_buy)
        val tv_delete = itemView.find<TextView>(R.id.tv_delete)
        val ll_content = itemView.find<LinearLayout>(R.id.ll_content)
        val iv_image = itemView.find<ImageView>(R.id.iv_image)
        val tv_name = itemView.find<TextView>(R.id.tv_name)
        val tv_phone = itemView.find<TextView>(R.id.tv_phone)
        val tv_data = itemView.find<TextView>(R.id.tv_data)
        override fun setData(view: View, data: UserManagerBean.ManagerInfo, position: Int) {
            if (null == data) return
            Glide.with(this@PayManagerActivity)
                    .load(data.url)
                    .apply(RequestOptions.circleCropTransform()
                            .placeholder(R.drawable.default_head)
                            .error(R.drawable.default_head))
                    .into(iv_image)
            iv_select.isSelected = data.isSelect
            tv_name.text = data.name
            tv_phone.text = data.phone
            if (data.expiry_time.isNullOrEmpty()){
                tv_data.text = "到期时间：- -  - -"
            }else{
                tv_data.text = "到期时间：${data.expiry_time}"
            }
            itemView.clickWithTrigger {
                if (alreadySelected.contains(data)){
                    alreadySelected.remove(data)
                    selectCount --
                    if (selectCount <= 0){
                        selectCount = 0
                    }
                }else{
                    alreadySelected.add(data)
                    selectCount++
                }
                data.isSelect = !data.isSelect
                iv_select.isSelected = data.isSelect
                tv_pay_count.text = "已选择账号：${selectCount}个"
            }

            tv_buy.clickWithTrigger {
                //购买
                PayDialog.showPayBookDialog(this@PayManagerActivity,2,this@PayManagerActivity,
                        userAccount,price,1,data.user_id,PdfBook(0,"","","","","","",1))
            }
        }

    }

    override fun pay(order_type: Int, count: Int, pay_type: Int, fee: String, tv_per_balance: TextView,
                     iv_pre_select: ImageView, tv_bus_balance: TextView, iv_bus_select: ImageView,
                     tv_per_title:TextView,tv_bus_title:TextView,dialog: Dialog) {

    }

    override fun payForOther(id: String, order_type: Int, count: Int, pay_type: Int, fee: String,
                             tv_per_balance: TextView, iv_pre_select: ImageView, tv_bus_balance: TextView, iv_bus_select: ImageView,
                             tv_per_title:TextView,tv_bus_title:TextView,dialog: Dialog,book: PdfBook?) {
        this.dialog = dialog
        SoguApi.getStaticHttp(application)
                .getAccountPayInfo(order_type,count,pay_type,fee,id,Store.store.getUser(this)!!.accid)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if (pay_type == 1 || pay_type == 2){
                                showSuccessToast("支付成功")
                                payRefresh()
                                if (dialog.isShowing){
                                    dialog.dismiss()
                                }
                            }else{
                                if (pay_type == 3){
                                    //支付宝
                                    sendToZfbRequest(payload.payload as String?,dialog)
                                }else if (pay_type == 4){
                                    //微信
                                    val orderInfo = payload.payload as String
                                    if (null != orderInfo){
                                        sendToWxRequest(orderInfo)
                                    }else{
                                        showErrorToast("获取订单失败")
                                    }
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
        }else{
            XmlDb.open(this).set("invokeType",2)
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

    private fun setLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    private fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }

    private fun showEmpty(){
        fl_empty.visibility = View.VISIBLE
    }
    private fun goneEmpty(){
        fl_empty.visibility = View.INVISIBLE
    }

    private fun bindListener() {
        tv_pay.clickWithTrigger {
            //批量购买
            var ids = ""
            if (selectCount > 0){
                alreadySelected.forEachIndexed { index, managerInfo ->
                    if (index == alreadySelected.size - 1){
                        ids += managerInfo.user_id
                    }else{
                        ids += managerInfo.user_id + ","
                    }

                }
                PayDialog.showPayBookDialog(this,2,this,userAccount,price,selectCount,ids,PdfBook(0,"","","","","","",1))
            }else{
                showCommonToast("请至少选择一个账号")
            }
        }

        tv_delete.clickWithTrigger {
            //批量删除
        }
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = false
        config.autoLoadMoreEnable = false
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    fun dofinishRefresh() {
        if (this::refresh.isLateinit && refresh.isRefreshing) {
            refresh.finishRefresh()
        }
        goneLoadding()
    }

    override fun doRefresh() {
        getManagerData()
    }

    private fun payRefresh(){
        getManagerData()
        alreadySelected.clear()
        selectCount = 0
        tv_pay_count.text = "已选择账号：${selectCount}个"
    }
    override fun doLoadMore() {

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        }catch (e : Exception){
            e.printStackTrace()
        }
    }
}