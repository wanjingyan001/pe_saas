package com.sogukj.pe.module.project.operate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.BidBean
import com.sogukj.pe.bean.ProjectBean
import kotlinx.android.synthetic.main.activity_news_detail.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class BidInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    lateinit var data: BidBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        data = intent.getSerializableExtra(BidBean::class.java.simpleName) as BidBean
        setContentView(R.layout.activity_bid_info)
        setBack(true)
        title = "招投标"

        val webSettings = webview.settings
        webSettings.savePassword = false
        webSettings.javaScriptEnabled = true
        webSettings.displayZoomControls = false
        webSettings.builtInZoomControls = false
        webSettings.setSupportZoom(false)
        webSettings.domStorageEnabled = true

        val appCacheDir = this.applicationContext.getDir("cache", Context.MODE_PRIVATE).path
        webSettings.setAppCachePath(appCacheDir)
        webSettings.allowFileAccess = true
        webSettings.setAppCacheEnabled(true)
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.defaultTextEncodingName = "utf-8"

        webSettings.loadsImagesAutomatically = true
//        webSettings.setBlockNetworkImage(true)
//      webSettings.setUseWideViewPort(true);
//      webSettings.setLoadWithOverviewMode(true);
//        webSettings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS)
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        set(data)
    }

    internal val fontSize = 18
    fun head(): String = "<head>" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
            "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
            "</head>"

    fun set(data: BidBean) {
        val content = data.content
        val html = "<html>${head()}<body style='margin:10px;'>" +
                "<div style='padding:10px;'>${data.title}" +
                "<h5 style='color:#999;font-size:12px;'>发布日期:${data.publishTime}  </h5>" +
                "<span style='color:#333;font-size:16px;line-height:30px;'>" + content + "</span></div>" +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, data: BidBean) {
            val intent = Intent(ctx, BidInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(BidBean::class.java.simpleName, data)
            ctx?.startActivity(intent)
        }
    }
}