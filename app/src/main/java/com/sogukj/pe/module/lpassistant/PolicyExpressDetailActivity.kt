package com.sogukj.pe.module.lpassistant

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.widget.NestedScrollView
import android.view.View
import android.webkit.WebSettings
import com.bumptech.glide.Glide
import com.sogukj.pe.App
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.CusShareBean
import com.sogukj.pe.bean.PlDetailInfo
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.ShareUtils
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_pl_deatil.*
import java.io.*

/**
 * Created by CH-ZH on 2018/9/6.
 */
class PolicyExpressDetailActivity : BaseActivity() {
    private var news_id : Int ? = null
    internal val fontSize = 18
    private var news: PlDetailInfo ? = null
    var shareTitle: String = "政策速递"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pl_deatil)
        Utils.setWindowStatusBarColor(this, R.color.white)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        webview.setBackgroundColor(0)
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

        news_id = intent.getIntExtra("news_id",-1)


        verifyStoragePermissions(this)
        when (getEnvironment()) {
            "civc" -> {
                copyAssets("ic_launcher_zd.png")
            }
            "ht" -> {
                copyAssets("ic_launcher_ht.png")
            }
            "kk" -> {
                copyAssets("ic_launcher_kk.png")
            }
            "yge" -> {
                copyAssets("ic_launcher_yge.png")
            }
            else -> {
                copyAssets("ic_launcher_pe.png")
            }
        }
    }

    private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    fun verifyStoragePermissions(activity: Activity) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    1)

        }

    }

    private fun copyAssets(filename: String) {
        val assetManager = assets
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            `in` = assetManager.open(filename)

            val outFile = File(Environment.getExternalStorageDirectory(), filename)
            out = FileOutputStream(outFile)
            copyFile(`in`, out)
            `in`!!.close()
            out.flush()
            out.close()
        } catch (e: IOException) {
            Trace.e("tag", "Failed to copy asset file: " + filename, e)
        }

    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (true) {
            read = `in`.read(buffer)
            if (read == -1) break
            out.write(buffer, 0, read)
        }
    }

    private fun initData() {
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif")).into(iv_loading)
        toolbar_title.text="政策速递"
        getPlNewsDetailData()
    }

    private fun getPlNewsDetailData() {
        showLoadding()
        SoguApi.getService(App.INSTANCE, OtherService::class.java)
                .getPolicyExpressDetail(news_id!!)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                if (null != it){
                                    news = it
                                    setDetailData(it)
                                    goneLoadding()
                                }
                            }

                        }.otherWise {
                            showErrorToast( payload.message)
                        }
                    }
                    onComplete {
                        goneLoadding()
                    }

                    onError {
                        showErrorToast("获取数据失败")
                        goneLoadding()
                    }
                }
    }

    private fun setDetailData(detailInfo: PlDetailInfo) {
        tv_title.text = detailInfo.title
        tv_tag.text = detailInfo.source
        tv_time.text = detailInfo.time
        val head = "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
                "</head>"
        val html = "<html>${head}<body style='margin:0px;'>" +
                "<span style='color:#333;font-size:20px;line-height:30px;'>${detailInfo.content}</span>" +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    private fun bindListener() {
        toolbar_back.clickWithTrigger {
            onBackPressed()
        }

        iv_share.clickWithTrigger {
            //分享
            var shareBean = CusShareBean()
            shareBean.shareTitle = shareTitle
            shareBean.shareContent = news!!.title!!
            shareBean.shareUrl = news!!.shareUrl
            ShareUtils.share(shareBean, this)
        }

        scroll_view.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY >= 150){
                toolbar_title.visibility = View.VISIBLE
            }

            if (scrollY <= 50){
                toolbar_title.visibility = View.INVISIBLE
            }
        }
    }

    fun showLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }
    companion object {
        fun invoke(context: Context,news_id:Int){
            var intent = Intent(context, PolicyExpressDetailActivity::class.java)
            intent.putExtra("news_id",news_id)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}