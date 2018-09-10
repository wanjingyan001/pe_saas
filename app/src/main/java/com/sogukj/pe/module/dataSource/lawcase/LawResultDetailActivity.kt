package com.sogukj.pe.module.dataSource.lawcase

import android.content.Context
import android.os.Bundle
import android.webkit.WebSettings
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_law_detail.*
/**
 * Created by CH-ZH on 2018/9/10.
 */
class LawResultDetailActivity : ToolbarActivity() {
    private var href : String = ""
    internal val fontSize = 18
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_law_detail)
        initView()
        initData()
    }

    private fun initView() {
        setTitle("法律文书")
        href = intent.getStringExtra(Extras.DATA)

        val webSettings = webview.settings
        webSettings.savePassword = false
        webSettings.javaScriptEnabled = true
        webSettings.displayZoomControls = false
        webSettings.builtInZoomControls = false
        webSettings.setSupportZoom(false)
        webSettings.domStorageEnabled = true
        webSettings.textZoom = 75

        val appCacheDir = this.applicationContext.getDir("cache", Context.MODE_PRIVATE).path
        webSettings.setAppCachePath(appCacheDir)
        webSettings.allowFileAccess = true
        webSettings.setAppCacheEnabled(true)
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.defaultTextEncodingName = "utf-8"

        webSettings.loadsImagesAutomatically = true
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
    }

    private fun initData() {
        getLawDetailData()
    }

    private fun getLawDetailData() {
        SoguApi.getService(App.INSTANCE, OtherService::class.java)
                .getLawResultDetail(href)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                setLawDetailData(it)
                            }

                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {

                    }

                    onError {
                        showErrorToast("获取数据失败")
                    }
                }
    }

    private fun setLawDetailData(content: String) {
        val head = "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
                "</head>"
        val html = "<html>${head}<body style='margin:0px;'>" +
                "<span style='color:#333;font-size:20px;line-height:30px;'>${content}</span>" +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

}