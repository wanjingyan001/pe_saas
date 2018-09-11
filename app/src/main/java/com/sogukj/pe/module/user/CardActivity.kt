package com.sogukj.pe.module.user

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import cn.sharesdk.framework.Platform
import cn.sharesdk.framework.PlatformActionListener
import cn.sharesdk.framework.ShareSDK
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QQClientNotExistException
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.utils.WechatClientNotExistException
import com.android.dingtalk.share.ddsharemodule.DDShareApiFactory
import com.android.dingtalk.share.ddsharemodule.message.DDImageMessage
import com.android.dingtalk.share.ddsharemodule.message.DDMediaMessage
import com.android.dingtalk.share.ddsharemodule.message.SendMessageToDD
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setOnClickFastListener
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.ddshare.DDShareActivity
import com.sogukj.pe.peUtils.ShareUtils
import kotlinx.android.synthetic.main.layout_card_window.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class CardActivity : Activity(), PlatformActionListener {
    private lateinit var userBean:UserBean

    companion object {
        val PATH  = Environment.getExternalStorageDirectory().absolutePath + "/BusinessCard.png"
        fun start(ctx: Activity?, bean: UserBean) {
            val intent = Intent(ctx, CardActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_card_window)
        userBean = intent.getSerializableExtra(Extras.DATA) as UserBean
        userBean.let {
            setData(it)
        }
        downloadCard.clickWithTrigger {
            if (Utils.saveImage(this@CardActivity, businessCard, PATH)) {
                Utils.saveImageToGallery(ctx, PATH)
                doAsync {
                    val stream = FileInputStream(File(PATH))
                    val data = ByteArray(1024)
                    val outputStream = FileOutputStream(File(Environment.getExternalStorageDirectory().absolutePath
                            + "/tencent/MicroMsg/WeiXin/SouGuKJ.png"))
                    while (stream.read(data) != -1) {
                        outputStream.write(data)
                    }
                    stream.close()
                    outputStream.close()
                }
                toast("名片已经保存至本地")
                finish()
            } else {
                toast("权限不足")
                finish()
            }
        }
        shareCard.clickWithTrigger {
            share()
        }
        card_main.setOnTouchListener { v, event ->
            if (!inRangeOfView(businessCard, event) && !inRangeOfView(downloadCard, event) && !inRangeOfView(shareCard,event)) {
                finish()
                true
            } else {
                false
            }
        }
    }


    private fun share(){
        val dialog = Dialog(ctx, R.style.AppTheme_Dialog)
        dialog.setContentView(R.layout.dialog_share_custom)
        val lay = dialog.window!!.attributes
        lay.height = WindowManager.LayoutParams.WRAP_CONTENT
        lay.width = WindowManager.LayoutParams.MATCH_PARENT
        lay.gravity = Gravity.BOTTOM
        dialog.window.attributes = lay
        dialog.show()
        dialog.find<TextView>(R.id.tv_copy).visibility = View.GONE
        dialog.find<TextView>(R.id.tv_sms).visibility = View.GONE
        dialog.find<TextView>(R.id.tv_wexin).clickWithTrigger {
            dialog.dismiss()
            val sp = cn.sharesdk.framework.Platform.ShareParams()
            sp.shareType = cn.sharesdk.framework.Platform.SHARE_IMAGE
            sp.title = "我的名片"
            sp.text = "这是${userBean.name}的名片"
            sp.imagePath = PATH
            val wechat = ShareSDK.getPlatform(Wechat.NAME)
            wechat.platformActionListener = this@CardActivity
            wechat.share(sp)
        }
        dialog.find<TextView>(R.id.tv_qq).setVisible(false)
//                clickWithTrigger {
//            dialog.dismiss()
//            val sp = Platform.ShareParams()
//            sp.title = "我的名片"
//            sp.text = "这是${userBean.name}的名片"
//            sp.imagePath = PATH
//            val qq = ShareSDK.getPlatform(QQ.NAME)
//            qq.platformActionListener = this@CardActivity
//            qq.share(sp)
//        }
        dialog.find<TextView>(R.id.tv_dd).clickWithTrigger {
            val iddShareApi = DDShareApiFactory.createDDShareApi(ctx, DDShareActivity.Companion.DDApp_Id, true)
            //初始化一个DDImageMessage
            val imageObject = DDImageMessage(BitmapFactory.decodeFile(PATH))

            //构造一个mMediaObject对象
            val mediaMessage = DDMediaMessage()
            mediaMessage.mMediaObject = imageObject
            //构造一个Req
            val req = SendMessageToDD.Req()
            req.mMediaMessage = mediaMessage
            //调用api接口发送消息到钉钉
            iddShareApi.sendReq(req)
        }
    }

    fun setData(bean: UserBean) {
        if (bean.url.isNullOrEmpty()) {
            val ch = bean.name.first()
            headerImage.setChar(ch)
        } else {
            Glide.with(this)
                    .load(bean.headImage())
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            headerImage.setChar(userBean.name.first())
                            return false
                        }

                    })
                    .into(headerImage)
        }
        cardName.text = bean.name
        cardPosition.text = bean.position
        cardCompanyName.text = bean.company
        cardPhone.text = bean.phone
        cardEmail.text = bean.email
        cardAddress.text = bean.address
    }

    private fun inRangeOfView(view: View, event: MotionEvent): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        return !(event.x < x || event.x > (x + view.width) || event.y < y || event.y > (y + view.height))
    }

    override fun onComplete(p0: Platform?, p1: Int, p2: HashMap<String, Any>?) {
        runOnUiThread {
            p0?.let {
                when(it.name){
                    Wechat.NAME -> toast("微信分享成功")
                    QQ.NAME -> toast("QQ分享成功")
                    else -> {
                    }
                }
            }
        }
    }

    override fun onCancel(p0: Platform?, p1: Int) {
        runOnUiThread {
            toast("取消分享")
        }
    }

    override fun onError(p0: Platform?, p1: Int, p2: Throwable?) {
        runOnUiThread {
            p2?.let {
                it.printStackTrace()
                if (it is WechatClientNotExistException){
                    toast("请安装微信")
                } else if (it is QQClientNotExistException || it is cn.sharesdk.tencent.qq.QQClientNotExistException){
                    toast("请安装QQ")
                }else{
                    toast("分享失败")
                }
            }
        }
    }
}
