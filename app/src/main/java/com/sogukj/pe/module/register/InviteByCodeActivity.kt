package com.sogukj.pe.module.register

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.widget.Toast
import androidx.core.view.doOnLayout
import cn.sharesdk.framework.Platform
import cn.sharesdk.framework.PlatformActionListener
import cn.sharesdk.framework.ShareSDK
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QQClientNotExistException
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.utils.WechatClientNotExistException
import com.android.dingtalk.share.ddsharemodule.DDShareApiFactory
import com.android.dingtalk.share.ddsharemodule.IDDShareApi
import com.android.dingtalk.share.ddsharemodule.message.DDMediaMessage
import com.android.dingtalk.share.ddsharemodule.message.DDWebpageMessage
import com.android.dingtalk.share.ddsharemodule.message.SendMessageToDD
import com.android.dingtalk.share.ddsharemodule.plugin.SignatureCheck
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.extraDelegate
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.ddshare.DDShareActivity
import com.sogukj.pe.module.main.MainActivity
import com.sogukj.pe.peUtils.ZxingUtils
import com.tencent.wework.api.IWWAPI
import com.tencent.wework.api.WWAPIFactory
import com.tencent.wework.api.model.BaseMessage
import com.tencent.wework.api.model.WWAuthMessage
import com.tencent.wework.api.model.WWMediaLink
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_invite_by_code.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class InviteByCodeActivity : ToolbarActivity(), PlatformActionListener {
    private lateinit var outputPic: File
    private var invite: String? = null
    private val invitePath: String? by extraDelegate(Extras.DATA2)
    private var stringId: Int by Delegates.notNull()
    private lateinit var iwwapi: IWWAPI
    private lateinit var QRPath: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_by_code)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)

        invite = intent.getStringExtra(Extras.DATA)
        inviteCode.text = invite
        val userId = sp.getInt(Extras.SaasUserId, 0)
        QRPath = invitePath + "?userId=$userId"
        outputPic = makeTempFile(Environment.getExternalStorageDirectory().path + "/Sogu/Saas/", "qr_", ".jpg")
        parentLayout.doOnLayout {
            Observable.just(QRPath)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .map { ZxingUtils.createQRImage(it, QRCodeImg.width, QRCodeImg.height, null, outputPic.absolutePath) }
                    .subscribe { b ->
                        if (b) {
                            Glide.with(this)
                                    .load(BitmapFactory.decodeFile(outputPic.absolutePath))
                                    .into(QRCodeImg)
                        }
                    }
        }


        inviteCodeLayout.clickWithTrigger {
            if (inviteCode.text.isNotEmpty()) {
                val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                manager.primaryClip = ClipData.newPlainText(null, inviteCode.text)
                toast("已复制到剪贴板")
            }
        }
        weChatLayout.clickWithTrigger {
            shareWeChat()
        }
        copyAssets("img_logo.png")
        QQLayout.clickWithTrigger {
            shareQQ()
        }
//        dingLayout.clickWithTrigger {
//            shareDD()
//        }
    }


    private fun initCWeChat() {
        stringId = applicationInfo.labelRes
        iwwapi = WWAPIFactory.createWWAPI(this)
        iwwapi.registerApp(SCHEMA)
        val link = WWMediaLink()
        link.webpageUrl = QRPath
        link.title = "邀请码"
        link.description = "分享邀请码"
        link.appPkg = packageName
        link.appName = getString(stringId)
        link.appId = APPID
        link.agentId = AGENTID
        iwwapi.sendMessage(link) { resp: BaseMessage ->
            if (resp is WWAuthMessage.Resp) {
                when {
                    resp.errCode == WWAuthMessage.ERR_CANCEL -> Toast.makeText(this, "登陆取消", Toast.LENGTH_SHORT).show()
                    resp.errCode == WWAuthMessage.ERR_FAIL -> Toast.makeText(this, "登陆失败", Toast.LENGTH_SHORT).show()
                    resp.errCode == WWAuthMessage.ERR_OK -> Toast.makeText(this, "登陆成功：" + resp.code,
                            Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun shareWeChat() {
        val sp = cn.sharesdk.framework.Platform.ShareParams()
        sp.shareType = cn.sharesdk.framework.Platform.SHARE_WEBPAGE//非常重要：一定要设置分享属性
        sp.title = "邀请码"  //分享标题
        sp.text = invite   //分享文本
        sp.url = QRPath
        val wechat = ShareSDK.getPlatform(Wechat.NAME)
        wechat.platformActionListener = this
        wechat.share(sp)
    }

    private fun shareQQ() {
        val sp = Platform.ShareParams()
        sp.titleUrl = QRPath
        sp.title = "邀请码"
        sp.text = invite
        val logoUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_pe.png").toString()
        sp.imageUrl = logoUrl
        info { logoUrl }
        val qq = ShareSDK.getPlatform(QQ.NAME)
        qq.platformActionListener = this
        qq.share(sp)
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

    private lateinit var ddShareApi: IDDShareApi

    private fun shareDD() {
        if (checkShareToDingDingValid()) {
            ddShareApi = DDShareApiFactory.createDDShareApi(this, DDShareActivity.DDApp_Id, true)
            val installed = ddShareApi.isDDAppInstalled//钉钉是否安装
            val isSupport = ddShareApi.isDDSupportDingAPI//是否支持分享到钉钉
//        if (installed) {
//            showErrorToast("没有检测到钉钉")
//            return
//        }
//        if (isSupport) {
//            showErrorToast("不支持分享到钉钉")
//            return
//        }
            //初始化一个DDWebpageMessage并填充网页链接地址
            val webPageObject = DDWebpageMessage()
            webPageObject.mUrl = QRPath
            //构造一个DDMediaMessage对象
            val webMessage = DDMediaMessage()
            webMessage.mMediaObject = webPageObject
            //填充网页分享必需参数，开发者需按照自己的数据进行填充
            webMessage.mTitle = "邀请码"
            webMessage.mContent = "邀请码"
            //构造一个Req
            val webReq = SendMessageToDD.Req()
            webReq.mMediaMessage = webMessage
            ddShareApi.sendReq(webReq)
        }

    }


    override fun onComplete(p0: Platform, p1: Int, p2: HashMap<String, Any>) {
        when (p0.name) {
            Wechat.NAME -> showSuccessToast("微信分享成功")
            QQ.NAME -> showSuccessToast("QQ分享成功")

        }
    }

    override fun onCancel(p0: Platform, p1: Int) {
        showCommonToast("取消分享")
    }

    override fun onError(p0: Platform, p1: Int, p2: Throwable) {
        when (p2) {
            is WechatClientNotExistException -> showErrorToast("请安装微信")
            is QQClientNotExistException, is cn.sharesdk.tencent.qq.QQClientNotExistException -> showErrorToast("请安装QQ")
            else -> showErrorToast("分享失败")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        outputPic.delete()
    }

    /**
     * 校验分享到钉钉的参数是否有效
     * @return
     */
    private fun checkShareToDingDingValid(): Boolean {
        if (!TextUtils.equals(ONLINE_PACKAGE_NAME, packageName)) {
            Toast.makeText(this, "包名与线上申请的不匹配", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!TextUtils.equals(ONLINE_APP_ID, CURRENT_USING_APP_ID)) {
            Toast.makeText(this, "APP_ID 与生成的不匹配", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!TextUtils.equals(ONLINE_SIGNATURE, SignatureCheck.getMD5Signature(this, packageName))) {
            Toast.makeText(this, "签名与线上生成的不符", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    companion object {
        const val APPID = "ww8deb216b63789fc0"
        const val AGENTID: String = "1000003"
        const val SCHEMA: String = "5ZMZ-NENzzZcdoHzRR0b-L26XtqYxPcpTuD3c5Aqcqc"

        private val ONLINE_PACKAGE_NAME = "com.sogukj.pe"//将值替换为在钉钉开放平台上申请时的packageName
        private val ONLINE_APP_ID = "dingoabwf5s1xqurajtbjw"//将值替换为在钉钉开放平台上申请时平台生成的appId
        private val ONLINE_SIGNATURE = "FF:2C:A6:EB:80:52:E0:B3:2C:CD:18:53:73:57:8E:53:0F:17:91:6E"//将值替换为在钉钉开放平台上申请时的signature
        private val CURRENT_USING_APP_ID = "dingoabwf5s1xqurajtbjw"//将值替换为你使用的APP_ID

        fun makeTempFile(saveDir: String, prefix: String, extension: String): File {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val dir = File(saveDir)
            dir.mkdirs()
            return File(dir, prefix + timeStamp + extension)
        }
    }
}
