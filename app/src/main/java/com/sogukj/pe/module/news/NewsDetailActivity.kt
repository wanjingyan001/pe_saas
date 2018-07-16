package com.sogukj.pe.module.news

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebSettings.LayoutAlgorithm
import android.widget.TextView
import android.widget.Toast
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
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.service.NewService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_news_detail.*
import kotlinx.android.synthetic.main.item_main_news.*
import org.jetbrains.anko.find
import java.io.*
import java.util.*

/**
 * Created by qinfei on 17/8/11.
 */
class NewsDetailActivity : ToolbarActivity(), PlatformActionListener {
    override fun onComplete(platform: Platform, i: Int, hashMap: HashMap<String, Any>) {
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

    override fun onError(platform: Platform, i: Int, throwable: Throwable) {
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

    override fun onCancel(platform: Platform, i: Int) {
        mHandler.sendEmptyMessage(7)
    }

    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> Toast.makeText(this@NewsDetailActivity, "朋友圈分享成功", Toast.LENGTH_LONG).show()
                2 -> Toast.makeText(this@NewsDetailActivity, "微信分享成功", Toast.LENGTH_LONG).show()
                3 -> Toast.makeText(this@NewsDetailActivity, "新浪微博分享成功", Toast.LENGTH_LONG).show()
                4 -> Toast.makeText(this@NewsDetailActivity, "QQ分享成功", Toast.LENGTH_LONG).show()
                5 -> Toast.makeText(this@NewsDetailActivity, "QQ空间分享成功", Toast.LENGTH_LONG).show()
                6 -> Toast.makeText(this@NewsDetailActivity, "短信分享成功", Toast.LENGTH_LONG).show()
                7 -> Toast.makeText(this@NewsDetailActivity, "取消分享", Toast.LENGTH_LONG).show()
                8 -> Toast.makeText(this@NewsDetailActivity, "请安装微信", Toast.LENGTH_LONG).show()
                9 -> Toast.makeText(this@NewsDetailActivity, "请安装微博", Toast.LENGTH_LONG).show()
                10 -> Toast.makeText(this@NewsDetailActivity, "请安装QQ", Toast.LENGTH_LONG).show()
                11 -> Toast.makeText(this@NewsDetailActivity, "分享失败", Toast.LENGTH_LONG).show()
                else -> {
                }
            }
        }

    }

    fun appendLine(k: String, v: Any?) {
        buff.append("$k：<font color='#666666'>${if (v == null) "" else v}</font><br/>")
    }

    override val menuId: Int
        get() = R.menu.menu_share

    override fun setTitle(titleRes: CharSequence?) {
        super.setTitle(titleRes)
        news?.shareTitle = titleRes.toString()
    }

    fun share() {
        if (news == null)
            return
        val dialog = Dialog(this, R.style.AppTheme_Dialog)
        dialog.setContentView(R.layout.dialog_share)
        val lay = dialog.window!!.attributes
        lay.height = WindowManager.LayoutParams.WRAP_CONTENT
        lay.width = WindowManager.LayoutParams.MATCH_PARENT
        lay.gravity = Gravity.BOTTOM
        dialog.window!!.attributes = lay
        dialog.show()

        val tvWexin = dialog.findViewById<TextView>(R.id.tv_wexin) as TextView
        val tvQq = dialog.findViewById<TextView>(R.id.tv_qq) as TextView
        val tvCopy = dialog.findViewById<TextView>(R.id.tv_copy) as TextView
        val shareUrl = news!!.shareUrl
        val shareTitle = news!!.shareTitle
        val shareSummry = news!!.title
        //val shareImgUrl = File(Environment.getExternalStorageDirectory(), "img_logo.png").toString()
        val shareImgUrl:String
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
            else -> {
                shareImgUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_pe.png").toString()
            }
        }
        tvCopy.setOnClickListener {
            dialog.dismiss()
            Utils.copy(this, shareUrl)
            showCustomToast(R.drawable.icon_toast_common, "已复制")
        }
        tvQq.setOnClickListener {
            dialog.dismiss()
            val sp = Platform.ShareParams()
            sp.title = shareTitle
            sp.text = shareSummry
            sp.imageUrl = shareImgUrl//网络图片rul
            sp.titleUrl = shareUrl
            val qq = ShareSDK.getPlatform(QQ.NAME)
            qq.platformActionListener = this
            qq.share(sp)
        }
        tvWexin.setOnClickListener {
            dialog.dismiss()
            val sp = cn.sharesdk.framework.Platform.ShareParams()
            sp.shareType = cn.sharesdk.framework.Platform.SHARE_WEBPAGE//非常重要：一定要设置分享属性
            sp.title = shareTitle  //分享标题
            sp.text = shareSummry   //分享文本
            if (null != news!!.imgUrl) {
                sp.imageUrl = shareImgUrl//网络图片rul
            } else {
                sp.imagePath = shareImgUrl//
            }
            sp.url = shareUrl
            val wechat = ShareSDK.getPlatform(Wechat.NAME)
            wechat.platformActionListener = this
            wechat.share(sp)
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share -> {
                share()
            }
        }
        return false
    }

    var news: NewsBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)
        title = "资讯详情"
        setBack(true)

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
//        webSettings.setBlockNetworkImage(true)
//      webSettings.setUseWideViewPort(true);
//      webSettings.setLoadWithOverviewMode(true);
//        webSettings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS)
        webSettings.layoutAlgorithm = LayoutAlgorithm.SINGLE_COLUMN


        news = intent.getSerializableExtra(Extras.DATA) as NewsBean?
        news?.apply {
            if (table_id == 13) {
                webview.visibility = View.VISIBLE
                scroll_plain.visibility = View.GONE
            } else {
                webview.visibility = View.GONE
                scroll_plain.visibility = View.VISIBLE
                setSubview(this)
            }
            if (null != table_id && null != data_id)
                doRequest(table_id!!, data_id!!, this)
        }
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
                copyAssets("img_logo.png")
            }
        }
    }

    fun doRequest(table_id: Int, data_id: Int, data: NewsBean) {
        Trace.i(TAG, "tableId:dataId=$table_id:$data_id")
        SoguApi.getService(application,NewService::class.java)
                .newsInfo(table_id, data_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val map = payload.payload
                        map?.apply {
                            this["shareUrl"]?.toString()?.apply { news?.shareUrl = this }
                            setContent(table_id, this, data)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                })
    }

    fun setSubview(data: NewsBean) {
        tv_summary.text = data.title
        val strTime = data.time
        tv_time.visibility = View.GONE
        if (!TextUtils.isEmpty(strTime)) {
            val strs = strTime!!.trim().split(" ")
            if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                tv_time.visibility = View.VISIBLE
            }
            tv_date.text = strs
                    .getOrNull(0)
            tv_time.text = strs
                    .getOrNull(1)
        }
        tv_from.text = data.source
        tags.removeAllViews()
        data.tag?.split("#")
                ?.forEach { str ->
                    if (!TextUtils.isEmpty(str)) {
                        val itemTag = View.inflate(this, R.layout.item_tag_news, null)
                        val tvTag = itemTag.find<TextView>(R.id.tv_tag)
                        tvTag.text = str
                        tags.addView(itemTag)
                        data.setTags(this, tags)
                    }
                }

        if (data.url.isNullOrEmpty()) {
            var bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_icon)
            var draw = RoundedBitmapDrawableFactory.create(resources, bitmap)
            draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
            imageIcon.setBackgroundDrawable(draw)
        } else {
            Glide.with(context).asBitmap().load(data.url).into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(bitmap: Bitmap, glideAnimation: Transition<in Bitmap>?) {
                    var draw = RoundedBitmapDrawableFactory.create(resources, bitmap)
                    draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
                    imageIcon.setBackgroundDrawable(draw)
                }
            })
        }
    }


    val buff = StringBuffer()
    fun setContent(table_id: Int, map: Map<String, Any?>, data: NewsBean) {
        buff.setLength(0)
        when (table_id) {
            1 -> set1(map, data)
            2 -> set2(map, data)
            3 -> set3(map, data)
            4 -> set4(map, data)
            5 -> set5(map, data)
            6 -> set6(map, data)
            6 -> set6(map, data)
            7 -> set7(map, data)
            8 -> set8(map, data)
            9 -> set9(map, data)
            10 -> set10(map, data)
            11 -> set11(map, data)
            12 -> set12(map, data)
            13 -> set13(map, data)
        }

        tv_content.text = Html.fromHtml(buff.toString())
    }

    internal val fontSize = 18


    fun set1(map: Map<String, Any?>, data: NewsBean) {
        setTitle("法律诉讼")
        val content = map["content"]?.toString()
        if (!content.isNullOrEmpty()) {
            webview.visibility = View.VISIBLE
            scroll_plain.visibility = View.GONE
            val head = "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                    "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
                    "</head>"
            val html = "<html>${head}<body style='margin:0px;'>" +
                    "<span style='color:#333;font-size:16px;line-height:30px;'>$content</span></div>" +
                    "</body></html>"
            webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
        } else {
            webview.visibility = View.GONE
            scroll_plain.visibility = View.VISIBLE
            setSubview(data)

            appendLine("提交时间", map["submittime"])
            appendLine("标题", map["title"])
            appendLine("案件类型", map["casetype"])
            appendLine("案件号", map["caseno"])
            appendLine("法院", map["court"])
            appendLine("文书类型", map["doctype"])
            appendLine("原文链接地址", map["url"])
        }
        //                appendLine("唯一标识符", map["uuid"])
    }

    fun set13(map: Map<String, Any?>, data: NewsBean) {
        news?.shareTitle = "新闻舆情"
        val title = if (TextUtils.isEmpty(data.title)) "" else "<h1 style='color:#333;font-size:18px;'>${data.title}</h1>"
        val head = "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; height:auto;} .reduce-font p{font-size:" + fontSize + "px!important;}</style>" +
                "</head>"
        val content = map["format_content"] as String?
        val html = "<html>${head}<body style='margin:0px;'>" +
                "<div style='padding:10px;'>${title}" +
                "<h5 style='color:#999;font-size:12px;'>${data.time}    ${data.source}</h5>" +
                "<span style='color:#333;font-size:16px;line-height:30px;'>$content</span></div>" +
                "</body></html>"
        webview.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null)
    }

    fun set12(map: Map<String, Any?>, data: NewsBean) {
        setTitle("司法拍卖")
        tv_title.text = "${data.title}"
        tv_title.visibility = View.VISIBLE
        appendLine("委托法院拍卖时间", map["auction_time"])
        appendLine("委托法院内容", map["entrusted_court"])
        appendLine("内容", map["content"])
    }

    fun set11(map: Map<String, Any?>, data: NewsBean) {
        title = "开庭公告"

        tv_title.visibility = View.GONE
        appendLine("案由", map["case_name"])
        appendLine("案号", map["caseno"])
        appendLine("开庭日期", map["court_date"])
        appendLine("排期日期", map["schedu_date"])
        appendLine("承办部门", map["undertake_department"])
        appendLine("审判长/主审人", map["presiding_judge"])
        appendLine("上诉人", map["appellant"])
        appendLine("被上诉人", map["appellee"])
        appendLine("法院", map["court"])
        appendLine("法庭", map["courtroom"])
        appendLine("地区", map["area"])
    }

    fun set10(map: Map<String, Any?>, data: NewsBean) {
        title = "经营异常"
        appendLine("列入日期", map["putDate"])
        appendLine("列入经营异常名录原因", map["putReason"])
        appendLine("列入部门", map["putDepartment"])
        appendLine("移出日期", map["removeDate"])
        appendLine("移出原因", map["removeReason"])
        appendLine("移出部门", map["removeDepartment"])
    }

    fun set9(map: Map<String, Any?>, data: NewsBean) {
        title = "欠税公告"
        appendLine("纳税人名称", map["name"])
        appendLine("欠税税种", map["taxCategory"])
        appendLine("证件号码", map["personIdNumber"])
        appendLine("法人或负责人名称", map["legalpersonName"])
        appendLine("经营地点", map["location"])
        appendLine("当前新发生欠税余额", map["recentOwnTaxBalance"])
        appendLine("欠税余额", map["ownTaxBalance"])
        appendLine("纳税人识别号", map["taxIdNumber"])
        appendLine("类型", map["type"])
        appendLine("发布时间", map["time"])
        appendLine("税务机关", map["tax_authority"])
    }

    fun set8(map: Map<String, Any?>, data: NewsBean) {
        title = "动产抵押"
        appendLine("登记日期", map["regDate"])
        appendLine("登记编号", map["regNum"])
        appendLine("被担保债权种类", map["type"])
        appendLine("被担保债权数额", map["amount"])
        appendLine("登记机关", map["regDepartment"])
        appendLine("债务人履行债务的期限", map["term"])
        appendLine("担保范围", map["scope"])
        appendLine("备注", map["remark"])
        appendLine("概况种类", map["overviewType"])
        appendLine("概况数额", map["overviewAmount"])
        appendLine("概况担保的范围", map["overviewScope"])
        appendLine("概况债务人履行债务的期限", map["overviewTerm"])
        appendLine("概况备注", map["overviewRemark"])
//        appendLine("总抵押变更 json数据", map["changeInfoList"])
//        appendLine("抵押物信息", map["pawnInfoList"])
//        appendLine("抵押人信息", map["peopleInfoList"])
        appendLine("抵押物信息", "")
        try {
            val list = map["pawnInfoList"] as List<Map<String, String>>
            list.forEach { map ->
                appendLine("  名称", map["pawnName"])
                appendLine("  数量、质量、状况、所在地等情况", map["detail"])
                appendLine("  所有权归属", map["ownership"])
            }
        } catch (e: Exception) {

        }
        appendLine("抵押人信息", "")
        try {
            val list = map["peopleInfoList"] as List<Map<String, String>>
            list.forEach { map ->
                appendLine("  抵押权人名称", map["peopleName"])
                appendLine("  抵押权人证照/证件类型", map["liceseType"])
                appendLine("  证照/证件号码", map["licenseNum"])
            }
        } catch (e: Exception) {

        }
        val json1 = map["pawnInfoList"]
        json1?.apply {

        }
        val json2 = map["peopleInfoList"]
        json2.apply {

        }
    }

    fun set7(map: Map<String, Any?>, data: NewsBean) {
        title = "股权出质"
        appendLine("登记编号", map["regNumber"])
        appendLine("出质人", map["pledgor"])
        appendLine("质权人", map["pledgee"])
        appendLine("状态", map["state"])
        appendLine("出质股权数额", map["equityAmount"])
        appendLine("质权人证照/证件号码", map["certifNumberR"])
        appendLine("股权出质设立登记日期", map["regDate"])
    }

    fun set6(map: Map<String, Any?>, data: NewsBean) {
        title = "严重违法"
        appendLine("列入日期", map["putDate"])
        appendLine("列入原因", map["putReason"])
        appendLine("决定列入部门(作出决定机关", map["putDepartment"])
        appendLine("移除原因", map["removeReason"])
        appendLine("决定移除部门", map["removeDepartment"])
    }

    fun set5(map: Map<String, Any?>, data: NewsBean) {
        setTitle("行政处罚")
        appendLine("行政处罚日期", map["decisionDate"])
        appendLine("行政处罚决定书文号", map["punishNumber"])
        appendLine("违法行为类型", map["type"])
        appendLine("作出行政处罚决定机关名称", map["departmentName"])
        appendLine("行政处罚内容", map["content"])
    }

    fun set4(map: Map<String, Any?>, data: NewsBean) {
        setTitle("被执行人")
        appendLine("立案时间", map["caseCreateTime"])
        appendLine("执行标的", map["execMoney"])
        appendLine("案号", map["caseCode"])
        appendLine("执行法院", map["execCourtName"])
    }

    fun set3(map: Map<String, Any?>, data: NewsBean) {
        title = "失信人"
        appendLine("失信人名或公司名称", map["iname"])
        appendLine("执行依据文号", map["casecode"])
        appendLine("身份证号／组织机构代码", map["cardnum"])
        appendLine("省份", map["areaname"])
        appendLine("执行法院", map["courtname"])
        appendLine("案号", map["gistid"])
        appendLine("立案时间", map["regdate"])
        appendLine("做出执行依据单位", map["gistunit"])
        appendLine("法律生效文书确定的义务", map["duty"])
        appendLine("被执行人的履行情况", map["performance"])
        appendLine("发布时间", map["publishdate"])
    }

    fun set2(map: Map<String, Any?>, data: NewsBean) {
        title = "法院公告"
        appendLine("刊登日期", map["publishdate"])
        appendLine("原告", map["party1"])
        appendLine("当事人", map["party2"])
        appendLine("公告类型名称", map["bltntypename"])
        appendLine("法院名", map["courtcode"])
        appendLine("案件内容", map["content"])
    }


    companion object {
        fun start(ctx: Activity?, news: NewsBean) {
            val intent = Intent(ctx, NewsDetailActivity::class.java)
            intent.putExtra(Extras.DATA, news)
            ctx?.startActivity(intent)
        }
    }
}
