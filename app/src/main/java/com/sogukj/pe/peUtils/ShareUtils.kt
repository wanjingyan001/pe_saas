package com.sogukj.pe.peUtils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.WindowManager
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
import com.android.dingtalk.share.ddsharemodule.DDShareApiFactory
import com.android.dingtalk.share.ddsharemodule.IDDShareApi
import com.android.dingtalk.share.ddsharemodule.message.*
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.CusShareBean
import com.sogukj.pe.ddshare.DDShareActivity
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.ToastUtil.Companion.showCustomToast
import java.io.*
import java.util.*

/**
 * Created by CH-ZH on 2018/8/18.
 */
class ShareUtils {
    companion object : PlatformActionListener {
        var context: Context? = null
        var iddShareApi: IDDShareApi? = null
        fun share(bean: CusShareBean, context: Context) {
            copyAssets("ic_launcher_pe.png")
            iddShareApi = DDShareApiFactory.createDDShareApi(context, DDShareActivity.Companion.DDApp_Id, true)
            this.context = context
            val dialog = Dialog(context, R.style.AppTheme_Dialog)
            dialog.setContentView(R.layout.dialog_share_custom)
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
            val tv_dd = dialog.findViewById<TextView>(R.id.tv_dd) as TextView
            val tv_sms = dialog.findViewById<TextView>(R.id.tv_sms) as TextView
            val shareUrl = bean.shareUrl//news!!.shareUrl
            val shareTitle = bean.shareTitle//news!!.shareTitle
            val shareSummry = bean.shareContent//news!!.title
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
                showCustomToast(R.drawable.icon_toast_success, "已复制", context)
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

            tv_dd.setOnClickListener {
                showSendMessage(shareTitle, shareSummry, shareImgUrl, shareUrl)
                if (iddShareApi!!.isDDAppInstalled()) {
                    if (iddShareApi!!.isDDSupportAPI) {
                        showSendMessage(shareTitle, shareSummry, shareImgUrl, shareUrl)
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, "您的钉钉不支持分享到好友", context)
                    }
                } else {
                    showCustomToast(R.drawable.icon_toast_fail, "请先安装钉钉再分享", context)
                }
            }

            tv_sms.setOnClickListener {
           val smsBody = "【" + shareTitle + "】" + shareSummry + shareUrl
            val smsToUri = Uri.parse("smsto:");
            val sendIntent = Intent(Intent.ACTION_VIEW, smsToUri)
            //短信内容
            sendIntent.putExtra("sms_body", smsBody)
            sendIntent.setType("vnd.android-dir/mms-sms")
            if (context !is Activity) {
                sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(sendIntent)
            }

        }
        private fun copyAssets(filename: String) {
            val assetManager = context!!.assets
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

        private fun showSendMessage(shareTitle: String?, shareSummry: String, shareImgUrl: String, shareUrl: String?) {
//            sendTextMessage(false, shareSummry)
//            sendImage(false,shareImgUrl,shareUrl)
            sendWebPageMessage(false,shareTitle,shareSummry,shareImgUrl,shareUrl)
        }

        private fun sendWebPageMessage(isSendDing: Boolean, shareTitle: String?, shareSummry: String, shareImgUrl: String, shareUrl: String?) {
             //初始化一个DDWebpageMessage并填充网页链接地址
        val webPageObject = DDWebpageMessage()
        webPageObject.mUrl = shareUrl

        //构造一个DDMediaMessage对象
        val webMessage = DDMediaMessage()
        webMessage.mMediaObject = webPageObject

        //填充网页分享必需参数，开发者需按照自己的数据进行填充
        webMessage.mTitle = shareTitle
        webMessage.mContent = shareSummry
        webMessage.mThumbUrl = File(Environment.getExternalStorageDirectory(), "ic_launcher_pe.png").toString()
//        webMessage.mThumbUrl = "http://img.qdaily.com/uploads/20160606152752iqaH5t4KMvn18BZo.gif"
//        webMessage.mThumbUrl = "http://static.dingtalk.com/media/lAHPBY0V4shLSVDMlszw_240_150.gif"
        // 网页分享的缩略图也可以使用bitmap形式传输
//         webMessage.setThumbImage(BitmapFactory.decodeResource(context!!.getResources(), R.mipmap.ic_launcher_pe))

        //构造一个Req
        val webReq = SendMessageToDD.Req()
        webReq.mMediaMessage = webMessage
//        webReq.transaction = buildTransaction("webpage");

        if(isSendDing){
            iddShareApi!!.sendReqToDing(webReq)
        } else {
            iddShareApi!!.sendReq(webReq)
        }
        }

        private fun sendImage(isSendDing: Boolean, shareImgUrl: String, shareUrl: String?) {
            //初始化一个DDImageMessage
            val imageObject = DDImageMessage()
            imageObject.mImagePath = shareImgUrl
            imageObject.mImageUrl = shareUrl

            //构造一个mMediaObject对象
            val mediaMessage = DDMediaMessage()
            mediaMessage.mMediaObject = imageObject

            //构造一个Req
            val req = SendMessageToDD.Req()
            req.mMediaMessage = mediaMessage

            //调用api接口发送消息到钉钉
            if (isSendDing) {
                iddShareApi!!.sendReqToDing(req)
            } else {
                iddShareApi!!.sendReq(req)
            }
        }

        private fun sendTextMessage(isSendDing: Boolean, shareSummry: String) {
            //初始化一个DDTextMessage对象
            val textObject = DDTextMessage()
            textObject.mText = shareSummry

            //用DDTextMessage对象初始化一个DDMediaMessage对象
            val mediaMessage = DDMediaMessage()
            mediaMessage.mMediaObject = textObject

            //构造一个Req
            val req = SendMessageToDD.Req()
            req.mMediaMessage = mediaMessage

            //调用api接口发送消息到钉钉
            if (isSendDing) {
                iddShareApi!!.sendReqToDing(req)
            } else {
                iddShareApi!!.sendReq(req)
            }
        }

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
                    1 -> showCustomToast(R.drawable.icon_toast_success, "朋友圈分享成功", context!!)
                    2 -> showCustomToast(R.drawable.icon_toast_success, "微信分享成功", context!!)
                    3 -> showCustomToast(R.drawable.icon_toast_success, "新浪微博分享成功", context!!)
                    4 -> showCustomToast(R.drawable.icon_toast_success, "QQ分享成功", context!!)
                    5 -> showCustomToast(R.drawable.icon_toast_success, "QQ空间分享成功", context!!)
                    6 -> showCustomToast(R.drawable.icon_toast_success, "短信分享成功", context!!)
                    7 -> showCustomToast(R.drawable.icon_toast_common, "取消分享", context!!)
                    8 -> showCustomToast(R.drawable.icon_toast_common, "请安装微信", context!!)
                    9 -> showCustomToast(R.drawable.icon_toast_common, "请安装微博", context!!)
                    10 -> showCustomToast(R.drawable.icon_toast_common, "请安装QQ", context!!)
                    11 -> showCustomToast(R.drawable.icon_toast_fail, "分享失败", context!!)
                    else -> {
                    }
                }
            }

        }

    }
}