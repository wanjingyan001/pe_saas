package com.sogukj.pe.module.im

import android.content.Context
import android.graphics.Color
import android.os.Environment
import com.netease.nimlib.sdk.SDKOptions
import com.netease.nimlib.sdk.StatusBarNotificationConfig
import com.netease.nimlib.sdk.mixpush.MixPushConfig
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.module.main.MainActivity
import java.io.IOException

/**
 * Created by admin on 2018/1/10.
 */
class NimSDKOptionConfig {
    companion object {
        fun getSDKOptions(context: Context): SDKOptions {
            val options = SDKOptions()
            // 如果将新消息通知提醒托管给SDK完成，需要添加以下配置。
            val config = loadStatusBarNotificationConfig()
            options.statusBarNotificationConfig = config
            // 配置 APP 保存图片/语音/文件/log等数据的目录
//            options.sdkStorageRootPath = getAppCacheDir(context) + "/sjkg"// 可以不设置，那么将采用默认路径
            // 配置数据库加密秘钥
//            options.databaseEncryptKey = "NETEASE"
            // 配置是否需要预下载附件缩略图
            options.preloadAttach = true
            // 配置附件缩略图的尺寸大小
            options.thumbnailSize = (165.0 / 320.0 * Utils.getAndroidScreenProperty(context)[0]).toInt()
            // 通知栏显示用户昵称和头像
//            options.userInfoProvider = NimUserInfoProvider(DemoCache.getContext())
            // 定制通知栏提醒文案（可选，如果不定制将采用SDK默认文案）
//            options.messageNotifierCustomization = messageNotifierCustomization
            // 在线多端同步未读数
            options.sessionReadAck = true
            // 动图的缩略图直接下载原图
            options.animatedImageThumbnailEnabled = true
            // 采用异步加载SDK
            options.asyncInitSDK = true
            // 是否是弱IM场景
            options.reducedIM = false
            // 是否检查manifest 配置，调试阶段打开，调试通过之后请关掉
            options.checkManifestConfig = BuildConfig.DEBUG
            // 是否启用群消息已读功能，默认关闭
            options.enableTeamMsgAck = true
            // 在线多端同步未读数
            options.sessionReadAck = true
            options.mixPushConfig = buildMixPushConfig()
            return options
        }

        private fun loadStatusBarNotificationConfig(): StatusBarNotificationConfig {
            val config = StatusBarNotificationConfig()
            config.notificationEntrance = MainActivity::class.java
            config.notificationSmallIconId = R.drawable.ic_launcher_small
//            config.notificationColor = Color.parseColor("#5785f3")
            config.notificationFolded = true
            // 呼吸灯配置
            config.ledARGB = Color.GREEN
            config.ledOnMs = 1000
            config.ledOffMs = 1500
            // 是否APP ICON显示未读数红点(Android O有效)
            config.showBadge = true
            return config
        }

        private fun getAppCacheDir(context: Context): String? {
            var storageRootPath: String? = null
            // SD卡应用扩展存储区(APP卸载后，该目录下被清除，用户也可以在设置界面中手动清除)，请根据APP对数据缓存的重要性及生命周期来决定是否采用此缓存目录.
            // 该存储区在API 19以上不需要写权限，即可配置 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="18"/>
            try {
                if (context.externalCacheDir != null) {
                    storageRootPath = context.externalCacheDir.canonicalPath
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            storageRootPath?.let {
                if (it.isEmpty()) {
                    storageRootPath = "${Environment.getExternalStorageDirectory()}/${context.packageName}"
                }
            }
            return storageRootPath
        }

        private fun buildMixPushConfig(): MixPushConfig {
            // 第三方推送配置
            val config = MixPushConfig()
            // 小米推送
            config.xmAppId = "2882303761517845387"
            config.xmAppKey = "5491784551387"
            config.xmCertificateName = "XPEMIPUSH"
            // 华为推送
            config.hwCertificateName = "XPEHWPUSH"
            // 魅族推送
//            config.mzAppId = ""
//            config.mzAppKey = ""
//            config.mzCertificateName = ""
            return config
        }

//        private val messageNotifierCustomization = object : MessageNotifierCustomization {
//            override fun makeRevokeMsgTip(p0: String?, p1: IMMessage?): String = "TIP"
//            override fun makeNotifyContent(p0: String?, p1: IMMessage?): String = "TIP"
//            override fun makeTicker(p0: String?, p1: IMMessage?): String = "TIP"
//        }
    }
}