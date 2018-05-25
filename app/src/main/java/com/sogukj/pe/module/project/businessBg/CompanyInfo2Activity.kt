package com.sogukj.pe.module.project.businessBg

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_company_info2.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class CompanyInfo2Activity : ToolbarActivity() {

    lateinit var project: ProjectBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setContentView(R.layout.activity_company_info2)
        setBack(true)
        title = "公司简介"
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

        SoguApi.getService(application,ProjectService::class.java)
                .companyInfo2(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        set13(payload.payload)
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })

    }

    internal val fontSize = 18
    fun set13(content: String?) {
        if (content == null) return
        val title = ""
        val head = "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
                "</head>"
        val html = "<html>${head}<body style='margin:0px;'>" +
                "<div style='padding:10px;'><span style='color:#333;font-size:16px;line-height:30px;'>$content</span></div>" +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, CompanyInfo2Activity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}