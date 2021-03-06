package com.sogukj.pe.module.dataSource.lawcase

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import android.webkit.WebSettings
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.LawNewsDetailBean
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_law_detail.*
import kotlinx.android.synthetic.main.white_back_toolbar.*

/**
 * Created by CH-ZH on 2018/9/10.
 */
class LawResultDetailActivity : ToolbarActivity() {
    private var href : String = ""
    internal val fontSize = 30
    private var title : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_law_detail)
        initView()
        initData()
    }

    private fun initView() {
        href = intent.getStringExtra(Extras.DATA)
        title = intent.getStringExtra(Extras.TITLE)
        if (null != title && !("".equals(title))){
            setTitle(title)
        }else{
            setTitle("法律文书")
        }
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

        toolbar_back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initData() {
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        getLawDetailData()
    }

    private fun getLawDetailData() {
        showLoadding()
        SoguApi.getService(App.INSTANCE, DataSourceService::class.java)
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
                        goneLoadding()
                    }

                    onError {
                        goneLoadding()
                        showErrorToast("获取数据失败")
                    }
                }
    }

    private fun showLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    private fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }

    private fun setLawDetailData(content: LawNewsDetailBean) {
        if (null == content) return
        tv_title.text = content.center
        if (null != content.type && content.type!!.size > 0){
            ll_content.visibility = View.VISIBLE
            fitContentData(content.type!!)
        }else{
            ll_content.visibility = View.GONE
        }
        val head = "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
                "</head>"
        val html = "<html style='color:#808080;font-size:25px;'>${head}<body style='margin:0px;'>" +
                "<span line-height:30px;'>${content.content}</span>" +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    private fun fitContentData(tags: List<String>) {
        for (title in tags){
            val tag = View.inflate(this, R.layout.law_tag_item, null) as TextView
            tag.text = Html.fromHtml(title)
            ll_content.addView(tag)
        }
    }

}