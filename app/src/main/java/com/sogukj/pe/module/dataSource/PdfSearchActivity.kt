package com.sogukj.pe.module.dataSource

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.edit
import com.alipay.sdk.app.PayTask
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.DownloadUtil
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.PayResultInfo
import com.sogukj.pe.bean.PdfBook
import com.sogukj.pe.module.receipt.AllPayCallBack
import com.sogukj.pe.module.receipt.PayDialog
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.activity_pdf_search.*
import kotlinx.android.synthetic.main.search_header.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.dip
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

class PdfSearchActivity : BaseActivity(), AllPayCallBack {

    companion object {
        fun start(context: Context, @DocumentType type: Int, category: Int? = null) {
            val intent = Intent(context, PdfSearchActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.TYPE1, category)
            context.startActivity(intent)
        }
    }

    private val type: Int by extraDelegate(Extras.TYPE, -1)
    private var page = 1
    private lateinit var listAdapter: BookListAdapter
    private val documents = ArrayList<PdfBook>()
    private val downloaded = mutableSetOf<String>()//已下载文件的名称的缓存,用于处理下载按钮是否显示

    private lateinit var historyAdapter: TagAdapter<String>
    private val historyList = mutableSetOf<String>()
    private var book : PdfBook ? = null
    private var dialog : Dialog? = null
    private var mHandler : Handler = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg!!.what){
                DocumentsListActivity.SDK_PAY_FLAG -> {
                    val payResult = PayResultInfo(msg.obj as Map<String, String>)
                    /**
                    对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    val resultInfo = payResult.result// 同步返回需要验证的信息
                    val resultStatus = payResult.resultStatus
                    Log.e("TAG"," resultStatus ===" + resultStatus)
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        showSuccessToast("支付成功")
                        refreshIntelligentData()
                        if (null != dialog && dialog!!.isShowing){
                            dialog!!.dismiss()
                        }
                        PdfPreviewActivity.start(this@PdfSearchActivity,book!!, downloaded.contains(book!!.pdf_name))
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
        setContentView(R.layout.activity_pdf_search)
        Gson().fromJson<Array<String>>(sp.getString(Extras.DOWNLOADED_PDF, ""), Array<String>::class.java)?.let {
            it.isNotEmpty().yes {
                downloaded.addAll(it)
            }
        }
        listAdapter = BookListAdapter(documents, downloaded.toList(), type)
        listAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val book = documents[position]
            if (type == DocumentType.INTELLIGENT) {
                if (book.status == 1) {
                    //已购买
                    PdfPreviewActivity.start(this, book, downloaded.contains(book.pdf_name))
                } else {
//                    showCommonToast("请先购买智能文书")
                    PayDialog.showPayBookDialog(this, 1, this,
                            book.pdf_name, book.price!!, 1, book.id.toString(), book)
                }
            } else {
                PdfPreviewActivity.start(this, book, downloaded.contains(book.pdf_name))
            }
        }
        listAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            val book = documents[position]
            showProgress("正在下载")
            DownloadUtil.getInstance().download(book.pdf_path, externalCacheDir.toString(), book.pdf_name, object : DownloadUtil.OnDownloadListener {
                override fun onDownloadSuccess(path: String?) {
                    downloaded.add(book.pdf_name)
                    hideProgress()
                    listAdapter.downloaded = downloaded.toList()
                    listAdapter.notifyItemChanged(position)
                }

                override fun onDownloading(progress: Int) {
                    progressDialog?.apply {
                        this.progress = progress
                    }
                }

                override fun onDownloadFailed() {
                    hideProgress()
                }

            })
        }
        searchResultList.apply {
            layoutManager = LinearLayoutManager(ctx)
            addItemDecoration(SpaceItemDecoration(dip(10)))
            adapter = listAdapter
        }
        initData()
        initListener()

        listAdapter.setClickPayListener(object : BookListAdapter.ClickPayCallBack {
            override fun clickPay(title: String, price: String, count: Int, id: String, book: PdfBook) {
                PayDialog.showPayBookDialog(this@PdfSearchActivity, 1, this@PdfSearchActivity, title, price, count, id, book)
            }

        })
    }

    private fun initData() {
        val historyStr = sp.getString(Extras.SOURCE_PDF_HISTORY + type, "")
        val localHistory = historyStr.split(",").filter { it.isNotEmpty() }.toMutableList()
        localHistory.isNotEmpty().yes {
            historyList.addAll(localHistory)
            ll_empty_his.setVisible(false)
        }.otherWise {
            ll_empty_his.setVisible(true)
        }
        historyAdapter = PdfHistoryAdapter()
        tfl.adapter = historyAdapter

        refresh.isEnableRefresh = true
        refresh.isEnableLoadMore = true
        refresh.isEnableAutoLoadMore = true
        refresh.setRefreshHeader(ClassicsHeader(this))
        val footer = BallPulseFooter(ctx)
        footer.setIndicatorColor(Color.parseColor("#7BB4FC"))
        footer.setAnimatingColor(Color.parseColor("#7BB4FC"))
        refresh.setRefreshFooter(footer)
        refresh.setOnRefreshListener {
            page = 1
            et_search.textStr.isNotEmpty().yes {
                getPdfList(et_search.textStr)
            }.otherWise {
                getPdfList()
            }
        }
        refresh.setOnLoadMoreListener {
            page += 1
            et_search.textStr.isNotEmpty().yes {
                getPdfList(et_search.textStr)
            }.otherWise {
                getPdfList()
            }
        }
    }

    override fun pay(order_type: Int, count: Int, pay_type: Int, fee: String, tv_per_balance: TextView,
                     iv_pre_select: ImageView, tv_bus_balance: TextView, iv_bus_select: ImageView,
                     tv_per_title: TextView, tv_bus_title: TextView, dialog: Dialog) {


    }

    override fun payForOther(id: String, order_type: Int, count: Int, pay_type: Int, fee: String,
                             tv_per_balance: TextView, iv_pre_select: ImageView, tv_bus_balance: TextView,
                             iv_bus_select: ImageView, tv_per_title: TextView, tv_bus_title: TextView, dialog: Dialog, book: PdfBook?) {

        SoguApi.getStaticHttp(application)
                .getAccountPayInfo(order_type,count,pay_type,fee,id, Store.store.getUser(this)!!.accid)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if (pay_type == 1 || pay_type == 2){
                                showSuccessToast("支付成功")
                                refreshIntelligentData()
                                if (dialog.isShowing){
                                    dialog.dismiss()
                                }
                                PdfPreviewActivity.start(this@PdfSearchActivity,book!!, downloaded.contains(book.pdf_name))
                            }else{
                                if (pay_type == 3){
                                    //支付宝
                                    sendToZfbRequest(payload.payload as String?,dialog,book!!)
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
     * 刷新智能文书
     */
    private fun refreshIntelligentData() {
        page = 1
        et_search.textStr.isNotEmpty().yes {
            getPdfList(et_search.textStr)
        }.otherWise {
            getPdfList()
        }
    }

    /**
     * 支付宝支付
     */
    private fun sendToZfbRequest(commodityInfo: String?,dialog: Dialog,book:PdfBook) {
        this.book = book
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

    private fun initListener() {
        tv_cancel.clickWithTrigger {
            finish()
        }
        iv_del.clickWithTrigger {
            et_search.setText("")
            if (null != historyAdapter){
                historyAdapter = PdfHistoryAdapter()
                tfl.adapter = historyAdapter
                historyAdapter.notifyDataChanged()
                historyLayout.setVisible(true)
                historyList.isNotEmpty().yes {
                    ll_empty_his.setVisible(false)
                }.otherWise {
                    ll_empty_his.setVisible(true)
                }
            }
        }
        tv_his.clickWithTrigger {
            sp.edit { putString(Extras.SOURCE_PDF_HISTORY + type, "") }
            historyList.clear()
            historyAdapter = PdfHistoryAdapter()
            tfl.adapter = historyAdapter
            ll_empty_his.setVisible(true)
        }
        tfl.setOnTagClickListener { view, position, parent ->
            historyList.toMutableList().let {
                it.isNotEmpty().yes {
                    val str = it[position]
                    et_search.setText(str)
                    et_search.setSelection(str.length)
                }
            }
            true
        }
        et_search.textChangedListener {
            onTextChanged { charSequence, start, before, count ->
                et_search.textStr.isNotEmpty().yes {
                    getPdfList(et_search.textStr)
                }
            }
            afterTextChanged {
                if (et_search.text.isNotEmpty()) {
                    iv_del.setVisible(true)
                } else {
                    iv_del.setVisible(false)
                    et_search.setHint(R.string.search)
                    refresh.setVisible(false)

                    if (null != historyAdapter){
                        historyAdapter = PdfHistoryAdapter()
                        tfl.adapter = historyAdapter
                        historyAdapter.notifyDataChanged()
                        historyLayout.setVisible(true)
                        historyList.isNotEmpty().yes {
                            ll_empty_his.setVisible(false)
                        }.otherWise {
                            ll_empty_his.setVisible(true)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun getPdfList(searchKey: String? = null) {
        SoguApi.getService(application, DataSourceService::class.java)
                .getSourceBookList(page = page, keywords = searchKey, type = type)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                if (page == 1) {
                                    documents.clear()
                                }
                                documents.addAll(it)
                                listAdapter.notifyDataSetChanged()
                                ifNotNull(searchKey, it, { str, list ->
                                    list.isNotEmpty().yes {
                                        historyList.add(str)
                                    }
                                })
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                            search_iv_empty.setVisible(true)
                            refresh.setVisible(false)
                            historyLayout.setVisible(false)
                        }
                    }

                    onComplete {
                            (page == 1).yes {
                                refresh.finishRefresh()
                            }.otherWise {
                                refresh.finishLoadMore(0)
                            }
                            documents.isNotEmpty().yes {
                                search_iv_empty.setVisible(false)
                                refresh.setVisible(true)
                                historyLayout.setVisible(false)
                            }.otherWise {
                                search_iv_empty.setVisible(true)
                                refresh.setVisible(false)
                                historyLayout.setVisible(false)
                            }
                        }
                    onError {
                        showErrorToast("获取数据失败")
                    }

                }
    }

    override fun onPause() {
        super.onPause()
        downloaded.isNotEmpty().yes {
            sp.edit { putString(Extras.DOWNLOADED_PDF, downloaded.jsonStr) }
        }
    }

    override fun onStop() {
        super.onStop()
        historyList.isNotEmpty().yes {
            sp.edit { putString(Extras.SOURCE_PDF_HISTORY + type, historyList.joinToString(",")) }
        }
    }

    inner class PdfHistoryAdapter : TagAdapter<String>(historyList.toMutableList()) {
        override fun getView(parent: FlowLayout?, position: Int, t: String?): View {
            val itemView = View.inflate(this@PdfSearchActivity, R.layout.search_his_item, null)
            val history = itemView.findViewById<TextView>(R.id.tv_item)
            history.text = t
            return itemView
        }
    }
}
