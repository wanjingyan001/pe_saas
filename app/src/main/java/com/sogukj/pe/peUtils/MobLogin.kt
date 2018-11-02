package com.sogukj.pe.peUtils

import android.content.Context
import android.os.Build
import cn.sharesdk.framework.Platform
import cn.sharesdk.framework.PlatformActionListener
import cn.sharesdk.framework.ShareSDK
import cn.sharesdk.tencent.qq.QQ
import com.sogukj.pe.baselibrary.R
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.HashMap
import cn.sharesdk.wechat.friends.Wechat
import com.tencent.mm.opensdk.diffdev.OAuthListener
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory


/**
 * Created by admin on 2018/10/29.
 */
class MobLogin {

    companion object : AnkoLogger {
        const val WeChatID = "wxda5922908a178f1c"

        fun QQLogin(context: Context, block: (account: String) -> Unit) {
            val platform = ShareSDK.getPlatform(QQ.NAME)
            platform.platformActionListener = object : PlatformActionListener {
                override fun onComplete(p0: Platform, p1: Int, p2: HashMap<String, Any>) {
                    info { "授权成功" }
                    p2.iterator().forEach {
                        info { "${it.key} ===> ${it.value}" }
                    }
                    block.invoke(platform.db.userId)
                }

                override fun onCancel(p0: Platform, p1: Int) {
                    ToastUtil.showCustomToast(R.drawable.icon_toast_common, "已取消QQ登录", context)
                }

                override fun onError(p0: Platform, p1: Int, p2: Throwable) {
                    ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "QQ登录失败", context)
                }

            }
            platform.showUser(null)
        }

        fun Logout(name: String) {
            val platform = ShareSDK.getPlatform(name)
            platform.removeAccount(true)
        }


        fun WeChatLogin(context: Context, block: (account: String) -> Unit) {
            //MobSDK的微信登录
            val wechat = ShareSDK.getPlatform(Wechat.NAME)
            wechat.platformActionListener = object : PlatformActionListener {
                override fun onComplete(p0: Platform?, p1: Int, p2: HashMap<String, Any>?) {
                    info { "微信授权成功" }
                    p2?.iterator()?.forEach {
                        info { "${it.key} ===> ${it.value}" }
                    }
                    info { "所有数据:${p0?.db?.exportData()}" }
                    block.invoke(wechat.db.userId)
                }

                override fun onCancel(p0: Platform?, p1: Int) {
                    ToastUtil.showCustomToast(R.drawable.icon_toast_common, "已取消微信登录", context)
                }

                override fun onError(p0: Platform?, p1: Int, p2: Throwable?) {
                    ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "微信登录失败", context)
                }

            }
            wechat.authorize()

            //微信SDK的登录
//            val wxapi = WXAPIFactory.createWXAPI(context, WeChatID, true)
//            wxapi.registerApp(WeChatID)
//            val req = SendAuth.Req()
//            req.scope = "snsapi_userinfo"
//            wxapi.sendReq(req)
        }


    }
}