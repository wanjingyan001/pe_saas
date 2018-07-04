package com.sogukj.pe.module.register

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.core.view.doOnLayout
import cn.sharesdk.framework.Platform
import cn.sharesdk.framework.PlatformActionListener
import cn.sharesdk.framework.ShareSDK
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QQClientNotExistException
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.utils.WechatClientNotExistException
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.peUtils.ZxingUtils
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_invite_by_code.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class InviteByCodeActivity : ToolbarActivity(), PlatformActionListener {
    private lateinit var outputPic: File
    private var invite: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_by_code)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)

        invite = intent.getStringExtra(Extras.DATA)
        inviteCode.text = invite
        outputPic = makeTempFile(Environment.getExternalStorageDirectory().path + "/Sogu/Saas/", "qr_", ".jpg")
        parentLayout.doOnLayout {
            Observable.just("www.baidu.com")
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
        QQLayout.clickWithTrigger {
            shareQQ()
        }
        dingLayout.clickWithTrigger {

        }
        cWeChatLayout.clickWithTrigger {

        }
    }


    private fun shareWeChat() {
        val sp = cn.sharesdk.framework.Platform.ShareParams()
        sp.shareType = cn.sharesdk.framework.Platform.SHARE_WEBPAGE//非常重要：一定要设置分享属性
        sp.title = "邀请码"  //分享标题
        sp.text = invite   //分享文本
        sp.url = "http://preht.pewinner.com/#/login"
        val wechat = ShareSDK.getPlatform(Wechat.NAME)
        wechat.platformActionListener = this
        wechat.share(sp)
    }

    private fun shareQQ(){
        val sp = Platform.ShareParams()
        sp.title = "邀请码"
        sp.text = invite
        sp.titleUrl = "http://preht.pewinner.com/#/login"
        val qq = ShareSDK.getPlatform(QQ.NAME)
        qq.platformActionListener = this
        qq.share(sp)
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


    companion object {
        fun makeTempFile(saveDir: String, prefix: String, extension: String): File {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val dir = File(saveDir)
            dir.mkdirs()
            return File(dir, prefix + timeStamp + extension)
        }
    }
}
