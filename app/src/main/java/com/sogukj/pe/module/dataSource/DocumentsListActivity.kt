package com.sogukj.pe.module.dataSource

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.FileProvider
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.edit
import com.alibaba.android.arouter.facade.annotation.Route
import com.alipay.sdk.app.PayTask
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.DownloadUtil
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.PayResultInfo
import com.sogukj.pe.bean.PdfBook
import com.sogukj.pe.module.receipt.AllPayCallBack
import com.sogukj.pe.module.receipt.PayDialog
import com.sogukj.pe.service.DataSourceService
import com.sogukj.pe.widgets.indexbar.RecycleViewDivider
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_prospectus_list.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.singleLine
import org.jetbrains.anko.startActivityForResult
import java.io.File

@Route(path = ARouterPath.DocumentsListActivity)
class DocumentsListActivity : BaseRefreshActivity(), AllPayCallBack {
    companion object {
        val SDK_PAY_FLAG = 1001
        fun start(context: Context, @DocumentType type: Int, category: Int? = null) {
            val intent = Intent(context, DocumentsListActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.TYPE1, category)
            context.startActivity(intent)
        }
    }

    private val type: Int by extraDelegate(Extras.TYPE, -1)
    private val category: Int? by extraDelegate(Extras.TYPE1, null)
    private var page = 1
    private lateinit var listAdapter: BookListAdapter
    private val documents = ArrayList<PdfBook>()
    private val downloaded = mutableSetOf<String>()//已下载文件的名称的缓存,用于处理下载按钮是否显示
    private var book : PdfBook ? = null
    private var dialog : Dialog? = null
    private var mHandler : Handler = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg!!.what){
                SDK_PAY_FLAG -> {
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
                        refreshIntelligentData()
                        if (null != dialog && dialog!!.isShowing){
                            dialog!!.dismiss()
                        }
                        PdfPreviewActivity.start(this@DocumentsListActivity,book!!, downloaded.contains(book!!.pdf_name))
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
    override val menuId: Int
        get() = R.menu.menu_select_industry

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prospectus_list)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        Utils.setWindowStatusBarColor(this, R.color.white)
        setBack(true)
        Glide.with(ctx)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        when (type) {
            DocumentType.INTELLIGENT -> {
                title = "智能文书"
                intelligentHeader.setVisible(true)
                filterCondition.setVisible(false)
//                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            DocumentType.EQUITY -> {
                title = "招股书"
                intelligentHeader.setVisible(false)
                filterCondition.setVisible(false)
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            DocumentType.INDUSTRY_REPORTS -> {
                val name = intent.getStringExtra(Extras.NAME)
                val titleTv = toolbar!!.findViewById<TextView>(R.id.toolbar_title)
                titleTv.maxEms = 9
                titleTv.singleLine = true
                titleTv.ellipsize = TextUtils.TruncateAt.END
                title = name.isNullOrEmpty().yes { "行业研报" }.otherWise { name }
                intelligentHeader.setVisible(false)
                filterCondition.setVisible(false)
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            else -> {
                throw Exception("类型错误")
            }
        }
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
                    PayDialog.showPayBookDialog(this@DocumentsListActivity, 1, this@DocumentsListActivity,
                            book.pdf_name, book.price!!, 1, book.id.toString(), book)
                }
            } else {
                PdfPreviewActivity.start(this, book, downloaded.contains(book.pdf_name))
            }
        }
        listAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            val book = documents[position]
            if (type == DocumentType.INTELLIGENT) {
                if (book.status == 1) {
                    showProgress("正在下载")
                    DownloadUtil.getInstance().download(book.pdf_path.substring(0, book.pdf_path.indexOf("?")),
                            externalCacheDir.toString(),
                            book.pdf_name,
                            object : DownloadUtil.OnDownloadListener {
                                override fun onDownloadSuccess(path: String) {
                                    downloaded.add(book.pdf_name)
                                    hideProgress()
                                    listAdapter.downloaded = downloaded.toList()
                                    listAdapter.notifyItemChanged(position)
                                    opedPdf(path)
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
                } else {
                    showCommonToast("需先购买智能文书才能下载")
                }
            } else {
                showProgress("正在下载")
                DownloadUtil.getInstance().download(book.pdf_path.substring(0, book.pdf_path.indexOf("?")),
                        externalCacheDir.toString(),
                        book.pdf_name,
                        object : DownloadUtil.OnDownloadListener {
                            override fun onDownloadSuccess(path: String) {
                                downloaded.add(book.pdf_name)
                                hideProgress()
                                listAdapter.downloaded = downloaded.toList()
                                listAdapter.notifyItemChanged(position)
                                opedPdf(path)
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
        }

        listAdapter.setClickPayListener(object : BookListAdapter.ClickPayCallBack {
            override fun clickPay(title: String, price: String, count: Int, id: String, book: PdfBook) {
                PayDialog.showPayBookDialog(this@DocumentsListActivity, 1, this@DocumentsListActivity, title, price, count, id, book)
            }

        })
        filterResultList.apply {
            layoutManager = LinearLayoutManager(ctx)
//            addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
            addItemDecoration(RecycleViewDivider(ctx, LinearLayoutManager.VERTICAL))
            adapter = listAdapter
        }
        setLoadding()
        getPdfList()
    }

    override fun pay(order_type: Int, count: Int, pay_type: Int, fee: String, tv_per_balance: TextView, iv_pre_select: ImageView,
                     tv_bus_balance: TextView, iv_bus_select: ImageView,
                     tv_per_title:TextView,tv_bus_title:TextView,dialog: Dialog) {

    }
    override fun payForOther(id:String,order_type: Int, count: Int, pay_type: Int, fee: String, tv_per_balance: TextView,
                     iv_pre_select: ImageView, tv_bus_balance: TextView, iv_bus_select: ImageView,
                             tv_per_title:TextView,tv_bus_title:TextView,dialog: Dialog,book:PdfBook?) {
        SoguApi.getStaticHttp(application)
                .getAccountPayInfo(order_type,count,pay_type,fee,id)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if (pay_type == 1 || pay_type == 2){
                                showSuccessToast("支付成功")
                                refreshIntelligentData()
                                if (dialog.isShowing){
                                    dialog.dismiss()
                                }
                                PdfPreviewActivity.start(this@DocumentsListActivity,book!!, downloaded.contains(book.pdf_name))
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
            msg.what = SDK_PAY_FLAG
            msg.obj = result
            mHandler.sendMessage(msg)
        }
        val payThread = Thread(payRunnable)
        payThread.start()
    }

    /**
     * 刷新智能文书
     */
    private fun refreshIntelligentData() {
        page = 1
        getPdfList()
    }

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
                                filterResultTv.text = Html.fromHtml(getString(R.string.tv_title_result_search2, (payload.total as Double).toInt()))
                            }
                        }.otherWise {
                            if (page > 1){
                                showErrorToast("已经全部加载完成")
                            }
                        }
                        goneLoadding()
                    }
                    onComplete {
                        emptyImg.setVisible(documents.isEmpty())
                        filterResultList.setVisible(documents.isNotEmpty())
                        goneLoadding()
                    }
                    onError {
                        it.printStackTrace()
                        goneLoadding()
                    }
                }
    }

    private fun opedPdf(path:String){
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //设置intent的Action属性
        intent.action = Intent.ACTION_VIEW
        try {
            val data: Uri
            val file = File(path)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = FileProvider.getUriForFile(context, context.applicationInfo.packageName + ".generic.file.provider", file)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                //设置intent的data和Type属性
                data = Uri.fromFile(file)
            }
            intent.setDataAndType(data,"application/pdf")
            //跳转
            context.startActivity(intent)
        } catch (e: Exception) { //当系统没有携带文件打开软件，提示
            showErrorToast( "没有可以打开该文件的软件")
            e.printStackTrace()
        }
    }

    override fun doRefresh() {
        page = 1
        getPdfList()
    }

    override fun doLoadMore() {
        page += 1
        getPdfList()
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        if (type == DocumentType.INDUSTRY_REPORTS) {
//            menuInflater.inflate(menuId, menu)
//        }
//        return super.onCreateOptionsMenu(menu)
//    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(menuId, menu)
        menu.findItem(R.id.industrySelect).isVisible = type == DocumentType.INDUSTRY_REPORTS
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.industrySelect -> {
                startActivityForResult<IndustrySubActivity>(Extras.REQUESTCODE)
            }
            R.id.search -> {
                PdfSearchActivity.start(this, type, category = category)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        downloaded.isNotEmpty().yes {
            sp.edit { putString(Extras.DOWNLOADED_PDF, downloaded.jsonStr) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE) {
            getPdfList()
        }
    }

    fun setLoadding() {
        if (null != view_recover) {
            view_recover.visibility = View.VISIBLE
        }
    }

    fun goneLoadding() {
        if (null != view_recover) {
            view_recover.visibility = View.INVISIBLE
        }
    }
}
