package com.sogukj.pe.module.creditCollection

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.alipay.sdk.app.PayTask
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.RxBus
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.dataSource.DocumentsListActivity
import com.sogukj.pe.module.receipt.AllPayCallBack
import com.sogukj.pe.module.receipt.PayDialog
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.CreditService
import com.sogukj.service.SoguApi
import com.tencent.mm.sdk.openapi.IWXAPI
import com.tencent.mm.sdk.openapi.WXAPIFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_shareholder_credit.*
import org.jetbrains.anko.find

class ShareholderCreditActivity : BaseActivity(), View.OnClickListener, AllPayCallBack {
    private lateinit var bean: ProjectBean
    private lateinit var mAdapter: RecyclerAdapter<CreditInfo.Item>

    companion object {
        val TAG = ShareholderCreditActivity::class.java.simpleName
        val SHARE_CREDIT_ACTION = "share_credit_action"
        fun start(ctx: Context?, project: ProjectBean) {
            val intent = Intent(ctx, ShareholderCreditActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (RxBus.getIntanceBus().hasObservers()) {
            return
        }
        var dispose = RxBus.getIntanceBus().doSubscribe(MessageEvent::class.java, { bean ->
            mAdapter.dataList.add(0, bean.message)
            mAdapter.notifyDataSetChanged()
            iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
            Log.e("success", bean.message.toString())
        }, { t ->
            Log.e("success", t.message)
        })
        RxBus.getIntanceBus().addSubscription(this, dispose)
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.getIntanceBus().unSubscribe(this)
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    private var creditCount = 0
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
        setContentView(R.layout.activity_shareholder_credit)
        bean = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        initAdapter()
        initSearchView()
        back.setOnClickListener(this)
        inquireBtn.setOnClickListener(this)
        initWXAPI()
        Glide.with(context).asGif().load(R.drawable.dynamic).into(gif)

        AppBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            if (gif.height > 0) {
                var appBarHeight = AppBarLayout.height
                var toolbarHeight = toolbar.height
                var gifH = gif.height

                var finalGifH = Utils.dpToPx(context, 30)

                //初始距离（git.top）,终点距离0
                //计算移动距离Y
                val distanceHeadImg = gif.top * 1.0f// - (toolbarHeight - finalGifH) / 2
                var mHeadImgScale = distanceHeadImg / (appBarHeight - toolbarHeight) * (-Math.abs(verticalOffset))
                gif.translationY = mHeadImgScale

                //计算移动距离X
                //gif原来显示的长宽（不是ImageView的长宽）是84*56，现在45*30，需要移动(84-45)/2=20(其实是19.5)
                var xDis = Math.abs(verticalOffset) * 1.0f / (appBarHeight - toolbarHeight)
                gif.translationX = xDis * Utils.dpToPx(context, 20)

                //图片默认一开始高度为56dp(gifH),最后变成30dp
                //放大缩小
                var scale = 1.0f - (gifH - finalGifH) * Math.abs(verticalOffset) * 1.0f / (appBarHeight - toolbarHeight) / gifH
                gif.scaleX = scale
                gif.scaleY = scale

                if (appBarHeight - toolbarHeight - Math.abs(verticalOffset).toFloat() < 5) {
                    collapse.title = ""
                    toolbar_title.visibility = View.VISIBLE
                } else {
                    collapse.title = "让不良记录无处遁形"
                    toolbar_title.visibility = View.GONE
                }
            }
        }

        offset = 0
        doRequest(bean.company_id)

        mAdapter.onItemClick = { v, p ->
            var cell = mAdapter.dataList.get(p)
            if (cell.status == 2) {
                SensitiveInfoActivity.start(context, cell)
            } else if (cell.status == 3) {
                AddCreditActivity.start(context, "EDIT", cell, 0x002)
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(SHARE_CREDIT_ACTION))
        getCreditTimes()
    }

    private val receiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            showSuccessToast("支付成功")
            getCreditTimes()
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

    override fun pay(order_type: Int, count: Int, pay_type: Int, fee: String, tv_per_balance: TextView,
                     iv_pre_select: ImageView, tv_bus_balance: TextView, iv_bus_select: ImageView, tv_per_title: TextView,
                     tv_bus_title: TextView, dialog: Dialog) {
        this.dialog = dialog
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
            XmlDb.open(this).set("invokeType",6)
        }
        Log.e("TAG", "sendReq返回值=" + b)
    }

    private fun inspectWx(): Boolean {
        val sIsWXAppInstalledAndSupported = api!!.isWXAppInstalled() && api!!.isWXAppSupportAPI()
        if (!sIsWXAppInstalledAndSupported) {
            showCommonToast("您未安装微信")
            return false
        } else {
            val isPaySupported = api!!.getWXAppSupportAPI() >= com.tencent.mm.sdk.constants.Build.PAY_SUPPORTED_SDK_INT
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
    var searchKey: String? = null

    private fun initSearchView() {
        search_edt.filters = Utils.getFilter(context)
        search_edt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                search_hint.visibility = View.GONE
                search_icon.visibility = View.VISIBLE
            } else {
                search_hint.visibility = View.VISIBLE
                search_icon.visibility = View.GONE
                search_edt.clearFocus()
            }
        }
        search_edt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchKey = search_edt.text.toString()
                offset = 0
                doRequest(bean.company_id)
                true
            } else {
                false
            }
        }
        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search_edt.text.toString().isEmpty()) {
                    searchKey = ""
                    offset = 0
                    doRequest(bean.company_id)
                }
                if (!search_edt.text.isNullOrEmpty()) {
                    delete1.visibility = View.VISIBLE
                    delete1.setOnClickListener {
                        search_edt.setText("")
                    }
                } else {
                    delete1.visibility = View.GONE
                }
            }
        })
    }

    private fun initAdapter() {
        mAdapter = RecyclerAdapter(this, { _adapter, parent, _ ->
            val convertView = _adapter.getView(R.layout.item_shareholder_credit, parent)
            ShareHolder(convertView)
        })
        lister.layoutManager = LinearLayoutManager(this)
        lister.adapter = mAdapter

        refresh.setOnRefreshListener {
            offset = 0
            doRequest(bean.company_id)
            refresh.finishRefresh(1000)
        }
        refresh.setOnLoadMoreListener {
            offset = mAdapter.dataList.size
            doRequest(bean.company_id)
            refresh.finishLoadMore(1000)
        }
    }

    var offset = 0

    fun doRequest(companyId: Int?) {
        SoguApi.getService(application, CreditService::class.java)
                .showCreditList(company_id = companyId, offset = offset, fuzzyQuery = searchKey)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (offset == 0)
                            mAdapter.dataList.clear()
                        payload.payload?.forEach {
                            mAdapter.dataList.add(it)
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                }, {
                    mAdapter.notifyDataSetChanged()
                    iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                })
    }

    inner class ShareHolder(convertView: View) : RecyclerHolder<CreditInfo.Item>(convertView) {

        private val directorName = convertView.find<TextView>(R.id.directorName)
        private val directorPosition = convertView.find<TextView>(R.id.directorPosition)
        private val inquireStatus = convertView.find<ImageView>(R.id.inquireStatus)//失败
        private val phoneNumberTv = convertView.find<TextView>(R.id.phoneNumberTv)
        private val IDCardTv = convertView.find<TextView>(R.id.IDCardTv)
        private val companyTv = convertView.find<TextView>(R.id.companyTv)
        private val quireTimeTv = convertView.find<TextView>(R.id.quireTime)
        private val edit = convertView.find<ImageView>(R.id.edit)
        private val number = convertView.find<TextView>(R.id.numberTv)//失败
        private val fail = convertView.find<FrameLayout>(R.id.fail)//失败
        private val success = convertView.find<ImageView>(R.id.success)//成功

        override fun setData(view: View, data: CreditInfo.Item, position: Int) {
            directorName.text = data.name
            directorPosition.text = when (data.type) {
                1 -> "董监高"
                2 -> "股东"
                else -> ""
            }
            if (data.type == 0) {
                directorPosition.visibility = View.GONE
            } else {
                directorPosition.visibility = View.VISIBLE
            }
            phoneNumberTv.text = data.phone
            IDCardTv.text = data.idCard
            if (data.company.isNullOrEmpty()) {
                companyTv.visibility = View.GONE
            } else {
                companyTv.visibility = View.VISIBLE
                companyTv.text = data.company
            }
            if (data.queryTime.isNullOrEmpty()) {
                quireTimeTv.visibility = View.GONE
            } else {
                quireTimeTv.visibility = View.VISIBLE
                quireTimeTv.text = "最新查询时间：${data.queryTime}"
            }
            when (data.status) {
                1 -> {
                    fail.visibility = View.GONE
                    success.visibility = View.VISIBLE
                    success.setImageResource(R.drawable.zhengxin_chaxunzhong)
                }
                2 -> {
                    success.visibility = View.GONE
                    fail.visibility = View.VISIBLE
                    if (data.sum == null || data.sum == 0) {
                        number.visibility = View.GONE
                        inquireStatus.setImageResource(R.drawable.zhengxin_zhengchang)
                    } else {
                        number.visibility = View.VISIBLE
                        number.text = "${data.sum}"
                        inquireStatus.setImageResource(R.drawable.zhengxin_fail)
                    }
                }
                3 -> {
                    fail.visibility = View.GONE
                    success.visibility = View.VISIBLE
                    success.setImageResource(R.drawable.zhengxin_chaxunshibai)
                }
            }
            edit.setOnClickListener {
                AddCreditActivity.start(context, "EDIT", data, 0x002)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> finish()
            R.id.inquireBtn -> {
                if (creditCount <= 0){
                    PayDialog.showPayCreditDialog(this,1,this)
                    return
                }
                var str = XmlDb.open(context).get("INNER", "TRUE")
                if (str == "TRUE") {
                    var item = CreditInfo.Item()
                    item.company = bean.name
                    item.company_id = bean.company_id!!
                    AddCreditActivity.start(context, "ADD", item, 0x001)
                } else if (str == "FALSE") {
                    ShareHolderStepActivity.start(context, 1, 0, "")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001) {
            data?.apply {
                //                var bean = this.getSerializableExtra(Extras.DATA) as CreditInfo.Item
//                mAdapter.dataList.add(0, bean)
//                mAdapter.notifyDataSetChanged()
//                iv_empty.visibility = if (mAdapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                offset = 0
                doRequest(bean.company_id)
            }
        } else if (requestCode == 0x002) {
            data?.apply {
                var bean = this.getSerializableExtra(Extras.DATA) as CreditInfo.Item
                var type = ""
                try {
                    type = this.getSerializableExtra(Extras.TYPE) as String
                } catch (e: Exception) {
                }
                var list = ArrayList<CreditInfo.Item>(mAdapter.dataList)
                for (index in list.indices) {
                    if (list[index].id == bean.id) {
                        if (type.equals("DELETE")) {
                            list.removeAt(index)
                        } else {
                            //list[index] = bean
                            list.removeAt(index)
                            list.add(0, bean)
                        }
                        break
                    }
                }
                mAdapter.dataList.clear()
                mAdapter.dataList.addAll(list)
                mAdapter.notifyDataSetChanged()
            }
        }
    }
}
