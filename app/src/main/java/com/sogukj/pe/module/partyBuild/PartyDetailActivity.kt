package com.sogukj.pe.module.partyBuild

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setOnClickFastListener
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.service.PartyBuildService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_party_detail.*
import kotlin.properties.Delegates

class PartyDetailActivity : BaseActivity() {
    var id: Int by Delegates.notNull()

    companion object {
        fun start(context: Context, id: Int, title: String) {
            val intent = Intent(context, PartyDetailActivity::class.java)
            intent.putExtra(Extras.ID, id)
            intent.putExtra(Extras.NAME, title)
            context.startActivity(intent)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_detail)
        Utils.setWindowStatusBarColor(this, R.color.party_toolbar_red)
        toolbar_title.text = intent.getStringExtra(Extras.NAME)
        id = intent.getIntExtra(Extras.ID, 0)
        val settings = contentWeb.settings
        settings.javaScriptEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(false)
        settings.loadsImagesAutomatically = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        contentWeb.webViewClient = object : WebViewClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                request?.let {
                    view?.loadUrl(it.url.toString())
                }
                return true
            }
        }
        back.setOnClickFastListener {
            finish()
        }
        doRequest(id)
    }

    fun doRequest(id: Int) {
        SoguApi.getService(application,PartyBuildService::class.java)
                .articleInfo(id)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                Log.d("WJY", Gson().toJson(it))
                                detailTitle.text = it.title
                                articleAuthor.text = "作者:${it.author}"
                                articleSource.text = "来源:${it.source}"
                                articleTime.text = "时间:${it.time}"
                                contentWeb.loadDataWithBaseURL(null, getHtmlData(it.contents!!),
                                        "text/html", "utf-8", null)
                            }
                        }
                    }
                }
    }

    private fun getHtmlData(bodyHTML: String): String {
        val head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>html{padding:15px;} body{word-wrap:break-word;font-size:13px;padding:0px;margin:0px} p{padding:0px;margin:0px;font-size:13px;color:#222222;line-height:1.3;} img{padding:0px,margin:0px;max-width:100%; width:auto; height:auto;}</style>" +
                "</head>"
        return "<html>$head<body>$bodyHTML</body></html>"
    }

}
