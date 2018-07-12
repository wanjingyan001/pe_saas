package com.sogukj.pe.module.other

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.service.FundService
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_online_preview.*

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
        // webview必须设置支持Javascript才可打开
        web.settings.javaScriptEnabled = true
        // 设置此属性,可任意比例缩放
        web.settings.useWideViewPort = true

        web.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        web.settings.blockNetworkImage = false
        //通过在线预览office文档的地址加载
        //web.loadUrl(url)

//        if (FileUtil.isCorrectType(FileUtil.getExtension(url), FileUtil.FileType.DOC)) {
//            SoguApi.getService(application)
//                    .previewFile(url)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeOn(Schedulers.io())
//                    .subscribe({ payload ->
//                        if (payload.isOk) {
//                            web.loadUrl(payload.payload!!)
//                        }
//                    }, { e ->
//                        Trace.e(e)
//                    })
//        } else {
//            web.loadUrl(url)
//        }

        var map = HashMap<String, String>()
        map.put("url", url)
        SoguApi.getService(application,FundService::class.java)
                .previewFile(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        web.loadUrl(payload.payload!!)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }
}
