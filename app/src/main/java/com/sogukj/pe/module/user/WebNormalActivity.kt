package com.sogukj.pe.module.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.peUtils.BASE64Encoder
import com.sogukj.pe.peUtils.FileUtil
import kotlinx.android.synthetic.main.activity_web_normal.*
import java.io.File
import java.io.UnsupportedEncodingException

/**
 * Created by CH-ZH on 2018/10/16.
 */
class WebNormalActivity  : BaseActivity() {

    private var title = ""
    private var url = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_normal)
        Utils.setWindowStatusBarColor(this, R.color.white)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        title = intent.getStringExtra("title")
        url = intent.getStringExtra("url")
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

        }

        val settings = web.settings
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

        web.webChromeClient = WebChromeClient()


    }


    private fun initData() {
        toolbar_title.text = title

        if (url.toLowerCase().contains("pdf")) {
            if ("" != url) {
                var bytes: ByteArray? = null
                try {// 获取以字符编码为utf-8的字符
                    bytes = url.toByteArray(Charsets.UTF_8)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                if (bytes != null) {
                    url = BASE64Encoder().encode(bytes)
                }
            }
            web.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file=" + url)
        } else {
            if (FileUtil.getFileType(File(url.split("?")[0])) == FileUtil.FileType.DOC) {
                if (url.contains("txt")) {
                    web.loadUrl(url)
                } else {
                    web.loadUrl("https://view.officeapps.live.com/op/view.aspx?src=" + url)
                }
            } else if (FileUtil.getFileType(File(url.split("?")[0])) == FileUtil.FileType.IMAGE) {
                web.loadUrl(url)
            }else{
                web.loadUrl(url)
            }
        }
    }

    private fun bindListener() {
        toolbar_back.clickWithTrigger {
            onBackPressed()
        }

    }

    companion object {
        fun invoke(context: Context, title:String,url:String){
            var intent = Intent(context, WebNormalActivity::class.java)
            intent.putExtra("title",title)
            intent.putExtra("url",url)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}