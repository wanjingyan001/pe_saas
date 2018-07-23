package com.sogukj.pe.module.other

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.peUtils.BASE64Encoder
import com.sogukj.pe.peUtils.FileUtil
import kotlinx.android.synthetic.main.activity_online_preview.*
import java.io.File
import java.io.UnsupportedEncodingException

class OnlinePreviewActivity : ToolbarActivity() {

    companion object {

        fun start(ctx: Activity?, url: String, title: String) {
            val intent = Intent(ctx, OnlinePreviewActivity::class.java)
            intent.putExtra(Extras.URL, url)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }
    }

    var url = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_preview)

        setBack(true)
        title = intent.getStringExtra(Extras.TITLE)
        url = intent.getStringExtra(Extras.URL)

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }
        }

        val settings = web.getSettings()
        settings.setSavePassword(false)
        settings.setJavaScriptEnabled(true)
        settings.setAllowFileAccessFromFileURLs(true)
        settings.setAllowUniversalAccessFromFileURLs(true)

        //设置此属性，可任意比例缩放
        settings.setUseWideViewPort(true)
        //支持屏幕缩放
        settings.setSupportZoom(true)
        settings.setBuiltInZoomControls(true)
        //不显示webview缩放按钮
        settings.setDisplayZoomControls(false)

        web.setWebChromeClient(WebChromeClient())

        if (url.toLowerCase().contains("pdf")) {
            if (!"".equals(url)) {
                var bytes: ByteArray? = null
                try {// 获取以字符编码为utf-8的字符
                    bytes = url.toByteArray(Charsets.UTF_8)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace();
                }
                if (bytes != null) {
                    url = BASE64Encoder().encode(bytes)
                }
            }
            web.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file=" + url)
        } else {
            if (FileUtil.getFileType(File(url.split("?")[0])) == FileUtil.FileType.DOC) {
                web.loadUrl("https://view.officeapps.live.com/op/view.aspx?src=" + url)
            } else if (FileUtil.getFileType(File(url.split("?")[0])) == FileUtil.FileType.IMAGE) {
                web.loadUrl(url)
            }
        }
    }
}
