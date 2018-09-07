package com.sogukj.pe.module.dataSource

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.edit
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.arrayFromJson
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.DownloadUtil
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.CusShareBean
import com.sogukj.pe.peUtils.BASE64Encoder
import com.sogukj.pe.peUtils.ShareUtils
import com.sogukj.pe.service.FundService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_pdf_preview.*
import java.io.UnsupportedEncodingException

class PdfPreviewActivity : ToolbarActivity() {

    companion object {
        fun start(context: Context, pdfUrl: String, title: String, hasDownloaded: Boolean) {
            val intent = Intent(context, PdfPreviewActivity::class.java)
            intent.putExtra(Extras.URL, pdfUrl)
            intent.putExtra(Extras.TITLE, title)
            intent.putExtra(Extras.FLAG, hasDownloaded)
            context.startActivity(intent)
        }
    }

    override val menuId: Int
        get() = R.menu.menu_pdf_preview

    private var url: String? = null
    private var transUrl: String? = null
    private var flag: Boolean = false
    private val downloaded= mutableSetOf<String>()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_preview)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        Utils.setWindowStatusBarColor(this, R.color.white)
        title = intent.getStringExtra(Extras.TITLE)
        url = intent.getStringExtra(Extras.URL)
        flag = intent.getBooleanExtra(Extras.FLAG, false)
        flag.yes {
            supportInvalidateOptionsMenu()
        }
        getShareUrl()
        pdfWeb.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
        val settings = pdfWeb.settings
        settings.savePassword = false
        settings.javaScriptEnabled = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true
        //设置此属性，可任意比例缩放
        settings.useWideViewPort = true
        //支持屏幕缩放
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        //不显示webview缩放按钮
        settings.displayZoomControls = false
        pdfWeb.webChromeClient = WebChromeClient()
        if (!url.isNullOrEmpty()) {
            var bytes: ByteArray? = null
            try {// 获取以字符编码为utf-8的字符
                bytes = url!!.toByteArray(Charsets.UTF_8)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            if (bytes != null) {
                url = BASE64Encoder().encode(bytes)
            }
        }
        pdfWeb.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file=" + url)
        Gson().fromJson<Array<String>>(sp.getString(Extras.DOWNLOADED_PDF, ""),Array<String>::class.java)?.let {
            it.isNotEmpty().yes {
                downloaded.addAll(it)
            }
        }
    }

    private fun getShareUrl() {
        url?.let {
            val map = HashMap<String, String>()
            map.put("url", it)
            SoguApi.getService(application, FundService::class.java)
                    .previewFile(map)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            transUrl = payload.payload
                        }
                    }, { e ->
                        Trace.e(e)
                    })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.download -> {
                download()
            }
            R.id.share -> {
                share()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if(flag){
            menu.findItem(R.id.download).isVisible = false
        }
        return super.onPrepareOptionsMenu(menu)
    }


    private fun download() {
        showProgress("正在下载")
        DownloadUtil.getInstance().download(url, externalCacheDir.toString(), intent.getStringExtra(Extras.TITLE), object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(path: String?) {
                downloaded.add(intent.getStringExtra(Extras.TITLE))
                hideProgress()
                flag = true
                supportInvalidateOptionsMenu()
            }

            override fun onDownloading(progress: Int) {

            }

            override fun onDownloadFailed() {
                hideProgress()
            }

        })
    }

    private fun share() {
        if (transUrl.isNullOrEmpty())
            return
        val shareBean = CusShareBean()
        shareBean.shareTitle = intent.getStringExtra(Extras.TITLE)
        shareBean.shareContent = ""
        shareBean.shareUrl = transUrl!!
        ShareUtils.share(shareBean, this)
    }

    override fun onPause() {
        super.onPause()
        downloaded.isNotEmpty().yes {
            sp.edit { putString(Extras.DOWNLOADED_PDF, downloaded.jsonStr) }
        }
    }

}
