package com.sogukj.pe.module.other

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.support.annotation.RequiresApi
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.TextView
import cn.sharesdk.framework.Platform
import cn.sharesdk.framework.PlatformActionListener
import cn.sharesdk.framework.ShareSDK
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.system.text.ShortMessage
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QQClientNotExistException
import cn.sharesdk.tencent.qzone.QZone
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.moments.WechatMoments
import cn.sharesdk.wechat.utils.WechatClientNotExistException
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.CusShareBean
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.BASE64Encoder
import com.sogukj.pe.peUtils.FileUtil
import com.sogukj.pe.peUtils.ShareUtils
import com.sogukj.pe.service.FundService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_online_preview.*
import kotlinx.android.synthetic.main.toolbar_custom.*
import java.io.File
import java.io.UnsupportedEncodingException
import java.util.*

class OnlinePreviewActivity : ToolbarActivity(), PlatformActionListener {

    companion object {

        fun start(ctx: Activity?, url: String, title: String) {
            val intent = Intent(ctx, OnlinePreviewActivity::class.java)
            intent.putExtra(Extras.URL, url)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, url: String, title: String,isCanShare:Boolean) {
            val intent = Intent(ctx, OnlinePreviewActivity::class.java)
            intent.putExtra(Extras.URL, url)
            intent.putExtra(Extras.TITLE, title)
            intent.putExtra("isCanShare",isCanShare)
            ctx?.startActivity(intent)
        }
    }

    var url = ""
    private var isCanShare = true
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_preview)

        setBack(true)
        title = intent.getStringExtra(Extras.TITLE)
        url = intent.getStringExtra(Extras.URL)
        isCanShare = intent.getBooleanExtra("isCanShare",true)
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view!!.loadUrl(request!!.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
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
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        web.setWebChromeClient(WebChromeClient())

        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        var map = HashMap<String, String>()
        map.put("url", url)
        SoguApi.getService(application, FundService::class.java)
                .previewFile(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        transUrl = payload.payload!!
                    }
                }, { e ->
                    Trace.e(e)
                })

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
            if (FileUtil.getFileType(File(url.split("?")[0])) == FileUtil.FileType.ONLYDOC){
                view_recover.visibility = View.VISIBLE
                parseDocToPdf()
            } else if (FileUtil.getFileType(File(url.split("?")[0])) == FileUtil.FileType.DOC) {
                if (url.contains("txt")) {
                    web.loadUrl(url)
                } else {
                    web.loadUrl("https://view.officeapps.live.com/op/view.aspx?src=" + url)
                }
            } else if (FileUtil.getFileType(File(url.split("?")[0])) == FileUtil.FileType.IMAGE) {
                web.loadUrl(url)
            }
        }

        if (isCanShare){
            toolbar_menu.text = "分享"
            toolbar_menu.visibility = View.VISIBLE
        }else{
            toolbar_menu.visibility = View.INVISIBLE
        }

        toolbar_menu.clickWithTrigger {
            cusShare()
        }
    }

    private fun parseDocToPdf() {
        SoguApi.getStaticHttp(application)
                .docParsePdfFile(url)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val jsonObject = payload.payload
                            jsonObject?.let {
                                var previewUrl = it.get("preview_url").asString
                                if ("" != previewUrl) {
                                    var bytes: ByteArray? = null
                                    try {// 获取以字符编码为utf-8的字符
                                        bytes = previewUrl.toByteArray(Charsets.UTF_8)
                                    } catch (e: UnsupportedEncodingException) {
                                        e.printStackTrace()
                                    }
                                    if (bytes != null) {
                                        previewUrl = BASE64Encoder().encode(bytes)
                                    }
                                }
                                web.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file=" + previewUrl)
                            }
                        }else{
                            if (url.contains("txt")) {
                                web.loadUrl(url)
                            } else {
                                web.loadUrl("https://view.officeapps.live.com/op/view.aspx?src=" + url)
                            }
                        }

                        view_recover.visibility = View.INVISIBLE
                    }

                    onError {
                        it.printStackTrace()
                        if (url.contains("txt")) {
                            web.loadUrl(url)
                        } else {
                            web.loadUrl("https://view.officeapps.live.com/op/view.aspx?src=" + url)
                        }
                        view_recover.visibility = View.INVISIBLE
                    }
                }
    }

//    override val menuId: Int
//        get() = R.menu.menu_mark
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val flag = super.onCreateOptionsMenu(menu)
//        val menuMark = menu.findItem(R.id.action_mark) as MenuItem
//        if (isCanShare){
//            menuMark.title = "分享"
//        }else{
//            menuMark.title = ""
//        }
//        return flag
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when (item?.itemId) {
//            R.id.action_mark -> {
////                share()
//                if (isCanShare){
//                    cusShare()
//                }
//            }
//        }
//        return false
//    }

    private fun cusShare() {
        if (transUrl.isNullOrEmpty())
            return
        var shareBean = CusShareBean()
        shareBean.shareTitle = intent.getStringExtra(Extras.TITLE)
        shareBean.shareContent = ""
        shareBean.shareUrl = transUrl
        ShareUtils.share(shareBean,this)
    }

    var transUrl = ""

    fun share() {
        if (transUrl.isNullOrEmpty())
            return
        val dialog = Dialog(context, R.style.AppTheme_Dialog)
        dialog.setContentView(R.layout.dialog_share)
        val lay = dialog.getWindow()!!.getAttributes()
        lay.height = WindowManager.LayoutParams.WRAP_CONTENT
        lay.width = WindowManager.LayoutParams.MATCH_PARENT
        lay.gravity = Gravity.BOTTOM
        dialog.getWindow()!!.setAttributes(lay)
        dialog.show()

        val tvHead = dialog.findViewById<TextView>(R.id.head) as TextView
        tvHead.text = "分享网址到电脑端即可访问"
        val tvWexin = dialog.findViewById<TextView>(R.id.tv_wexin) as TextView
        val tvQq = dialog.findViewById<TextView>(R.id.tv_qq) as TextView
        val tvCopy = dialog.findViewById<TextView>(R.id.tv_copy) as TextView
        val shareUrl = transUrl//news!!.shareUrl
        val shareTitle = intent.getStringExtra(Extras.TITLE)//news!!.shareTitle
        val shareSummry = ""//news!!.title
        val shareImgUrl: String
        when (getEnvironment()) {
            "civc" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_zd.png").toString()
            }
            "ht" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_ht.png").toString()
            }
            "kk" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_kk.png").toString()
            }
            "yge" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_yge.png").toString()
            }
            "sr" -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_sr.png").toString()
            }
            else -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "img_logo.png").toString()
            }
        }
        tvCopy.setOnClickListener {
            dialog.dismiss()
            Utils.copy(context, shareUrl)
            showCustomToast(R.drawable.icon_toast_success, "已复制")
        }
        tvQq.setOnClickListener {
            dialog.dismiss()
            val sp = Platform.ShareParams()
            sp.setTitle(shareTitle)
            sp.setText(shareSummry)
            sp.imagePath = shareImgUrl
            //sp.setImageUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1528281900948&di=edeb19905f4920430f816d917c7b24fe&imgtype=0&src=http%3A%2F%2Fimg.zcool.cn%2Fcommunity%2F010f87596f13e6a8012193a363df45.jpg%401280w_1l_2o_100sh.jpg")//网络图片rul
            sp.setTitleUrl(shareUrl)
            val qq = ShareSDK.getPlatform(QQ.NAME)
            qq.platformActionListener = this
            qq.share(sp)
        }
        tvWexin.setOnClickListener {
            dialog.dismiss()
            val sp = cn.sharesdk.framework.Platform.ShareParams()
            sp.setShareType(cn.sharesdk.framework.Platform.SHARE_WEBPAGE)//非常重要：一定\要设置分享属性
            sp.setTitle(shareTitle)  //分享标题
            sp.setText(shareSummry)   //分享文本
//            if (null != news!!.imgUrl) {
//                sp.imageUrl = shareImgUrl//网络图片rul
//            } else {
//                sp.imagePath = shareImgUrl//
//            }
            sp.imagePath = shareImgUrl
            sp.setUrl(shareUrl)
            val wechat = ShareSDK.getPlatform(Wechat.NAME)
            wechat.platformActionListener = this
            wechat.share(sp)
        }

    }

    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> showCustomToast(R.drawable.icon_toast_success, "朋友圈分享成功")
                2 -> showCustomToast(R.drawable.icon_toast_success, "微信分享成功")
                3 -> showCustomToast(R.drawable.icon_toast_success, "新浪微博分享成功")
                4 -> showCustomToast(R.drawable.icon_toast_success, "QQ分享成功")
                5 -> showCustomToast(R.drawable.icon_toast_success, "QQ空间分享成功")
                6 -> showCustomToast(R.drawable.icon_toast_success, "短信分享成功")
                7 -> showCustomToast(R.drawable.icon_toast_common, "取消分享")
                8 -> showCustomToast(R.drawable.icon_toast_common, "请安装微信")
                9 -> showCustomToast(R.drawable.icon_toast_common, "请安装微博")
                10 -> showCustomToast(R.drawable.icon_toast_common, "请安装QQ")
                11 -> showCustomToast(R.drawable.icon_toast_fail, "分享失败")
                else -> {
                }
            }
        }

    }

    override fun onComplete(platform: Platform, p1: Int, p2: java.util.HashMap<String, Any>?) {
        if (platform.name == WechatMoments.NAME) {// 判断成功的平台是不是朋友圈
            mHandler.sendEmptyMessage(1)
        } else if (platform.name == Wechat.NAME) {
            mHandler.sendEmptyMessage(2)
        } else if (platform.name == SinaWeibo.NAME) {
            mHandler.sendEmptyMessage(3)
        } else if (platform.name == QQ.NAME) {
            mHandler.sendEmptyMessage(4)
        } else if (platform.name == QZone.NAME) {
            mHandler.sendEmptyMessage(5)
        } else if (platform.name == ShortMessage.NAME) {
            mHandler.sendEmptyMessage(6)
        }
    }

    override fun onCancel(p0: Platform?, p1: Int) {
        mHandler.sendEmptyMessage(7)
    }

    override fun onError(p0: Platform?, p1: Int, throwable: Throwable) {
        throwable.printStackTrace()

        if (throwable is WechatClientNotExistException) {
            mHandler.sendEmptyMessage(8)
        } else if (throwable is PackageManager.NameNotFoundException) {
            mHandler.sendEmptyMessage(9)
        } else if (throwable is QQClientNotExistException || throwable is cn.sharesdk.tencent.qq.QQClientNotExistException) {
            mHandler.sendEmptyMessage(10)
        } else {
            mHandler.sendEmptyMessage(11)
        }
    }
}
