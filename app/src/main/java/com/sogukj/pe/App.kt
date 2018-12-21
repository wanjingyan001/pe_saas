package com.sogukj.pe

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.preference.PreferenceManager
import android.support.multidex.MultiDexApplication
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.edit
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.Gson
import com.mob.MobSDK
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.AuthServiceObserver
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.nimlib.sdk.util.NIMUtil
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.no
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.database.SgDatabase
import com.sogukj.pe.module.approve.LeaveBusinessApproveActivity
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import com.sogukj.pe.module.calendar.ModifyTaskActivity
import com.sogukj.pe.module.calendar.TaskDetailActivity
import com.sogukj.pe.module.im.NimDemoLocationProvider
import com.sogukj.pe.module.im.NimSDKOptionConfig
import com.sogukj.pe.module.im.SessionHelper
import com.sogukj.pe.module.news.NewsDetailActivity
import com.sogukj.pe.module.register.LoginActivity
import com.sogukj.pe.module.weekly.PersonalWeeklyActivity
import com.sogukj.pe.peExtended.defaultIc
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import com.squareup.leakcanary.LeakCanary
import com.tencent.bugly.crashreport.CrashReport
import com.umeng.commonsdk.UMConfigure
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.MsgConstant
import com.umeng.message.PushAgent
import com.umeng.message.UmengMessageHandler
import com.umeng.message.entity.UMessage
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import me.leolin.shortcutbadger.ShortcutBadger
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.json.JSONObject


/**
 * Created by qinfei on 17/7/18.
 */
class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
//        MobSDK.init(this, "137b5c5ce8f55", "b28db523803b31a66b590150cb96c4fd")
        doAsync {
            //ARouter阿里路由
            if (BuildConfig.DEBUG) {
                ARouter.openLog()
                ARouter.openDebug()
            }
            ARouter.init(INSTANCE)
            BuildConfig.DEBUG.no {
                CrashReport.initCrashReport(INSTANCE, "49fb9e37b7", BuildConfig.DEBUG)
            }
            SgDatabase.getInstance(INSTANCE)
            MobSDK.init(INSTANCE, "214eaf8217e6c", "c1ddfcaa333020a5a06812bc745d508c")
            //友盟初始化
            val secret = if (getEnvironment() == "ht") {
                "8a3cd80e07007a0463e9b12f758b727b"
            } else {
                "a4bb361310d9aee916f2cc43c5dbda5c"
            }
            UMConfigure.init(INSTANCE, UMConfigure.DEVICE_TYPE_PHONE, secret)
            UMConfigure.setLogEnabled(true)
            val mPushAgent = PushAgent.getInstance(INSTANCE)
            mPushAgent.displayNotificationNumber = 5
            mPushAgent.notificationPlayLights = MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE
            mPushAgent.register(object : IUmengRegisterCallback {

                override fun onSuccess(deviceToken: String) {
                    //注册成功会返回device token
                    Log.d("WJY", "IUmengRegisterCallback:$deviceToken")
                    Store.store.setUToken(this@App, deviceToken)
                }

                override fun onFailure(s: String, s1: String) {
                    println("IUmengRegisterCallback:$s=>$s1")
                }
            })
            PushAgent.getInstance(INSTANCE).messageHandler = object : UmengMessageHandler() {
                override fun getNotification(p0: Context?, p1: UMessage?): Notification {
                    Log.d("WJY", "推送==>" + Gson().toJson(p1))
                    val builder = Notification.Builder(this@App)
                    val myNotificationView = RemoteViews(this@App.packageName, R.layout.upush_notification)
                    myNotificationView.setTextViewText(R.id.notification_title, p1?.title)
                    myNotificationView.setTextViewText(R.id.notification_text, p1?.text)
                    myNotificationView.setImageViewBitmap(R.id.notification_large_icon1, getLargeIcon(this@App, p1))
                    myNotificationView.setImageViewResource(R.id.notification_large_icon1, getSmallIconId(this@App, p1))
                    builder.setContent(myNotificationView)
                            .setSmallIcon(defaultIc())
                            .setTicker(p1?.ticker)
                            .setAutoCancel(true)
                    val notification = builder.notification
                    notification.defaults = Notification.DEFAULT_LIGHTS
                    try {
                        p1?.let {
                            if (it.extra != null && it.extra.containsKey("badge")) {
                                it.extra["badge"]?.let {
                                    ShortcutBadger.applyCount(this@App, it.toInt())
                                }
                            }
                        }
                    } catch (e: Exception) {
                    }
                    return notification
                }
            }
            PushAgent.getInstance(INSTANCE).setNotificationClickHandler { context, uMessage ->
                try {
                    ShortcutBadger.removeCount(context)
                    uMessage.custom?.apply {
                        val json = JSONObject(this)
                        val type = json.getInt("type")
                        val data = json.getJSONObject("data")
                        //1.负面信息  2.任务  3.日程 4签字,用印 5.周报
                        when (type) {
                            1 -> handle(context, json)
                            2 -> {
                                val data_id = data.getInt("data_id")
                                TaskDetailActivity.start(context, data_id, uMessage.title, ModifyTaskActivity.Task)
                            }
                            3 -> {
                                val data_id = data.getInt("data_id")
                                TaskDetailActivity.start(context, data_id, uMessage.title, ModifyTaskActivity.Schedule)
                            }
                            4 -> {
                                val approval_id = data.getInt("approval_id")
                                val is_mine = data.getInt("is_mine")
                                var title = ""
                                if (data.has("qs") && data.getInt("qs") == 1) {
                                    title = "签字审批"
                                    SignApproveActivity.start(context, approval_id, is_mine, "签字审批")
                                } else if (data.has("qs") && data.getInt("qs") == 2) {
                                    title = when (data.getInt("tid")) {
                                        10 -> "出差"
                                        11 -> "请假"
                                        else -> ""
                                    }
                                    LeaveBusinessApproveActivity.start(context, approval_id, is_mine, title)
                                } else {
                                    title = "用印审批"
                                    SealApproveActivity.start(context, approval_id, is_mine, "用印审批")
                                }
                            }
                            5 -> {
                                val weekId = data.getInt("week_id")
                                val userId = data.getInt("user_id")
                                val postName = data.getString("postName")
                                val intent = Intent(context, PersonalWeeklyActivity::class.java)
                                intent.putExtra(Extras.ID, weekId)
                                intent.putExtra(Extras.NAME, "Push")
                                intent.putExtra(Extras.TYPE1, userId)
                                intent.putExtra(Extras.TYPE2, postName)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                            else -> {
                            }
                        }
                    }
                } catch (e: Exception) {
                    Trace.e(e)
                }
            }
        }
        initNIM()
//        initLeakCanary()
    }

    private fun initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) return
        LeakCanary.install(this)
    }


    val GSON = Gson()
    fun handle(context: Context, json: JSONObject) {
        try {
            val dataJson = json.getJSONObject("data")
            val data = GSON.fromJson(dataJson.toString(), NewsBean::class.java)
            val intent: Intent = if (ActivityHelper.count === 0) {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent.`package` = packageName
                intent.putExtra("uPush.target", NewsDetailActivity::class.java)
                intent
            } else {
                val intent = Intent()
                intent.setClass(context, NewsDetailActivity::class.java)
                intent
            }

            intent.putExtra(Extras.DATA, data)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

        } catch (e: Exception) {
            Trace.e(e)
        }
    }

    fun resetPush(enable: Boolean) {
        val user = Store.store.getUser(this)
        val uToken = Store.store.getUToken(this)
        if (user?.uid != null && uToken != null) {
            val token = if (enable) uToken else ""
            SoguApi.getService(INSTANCE, UserService::class.java)
                    .saveUser(uid = user.uid!!, advice_token = token)
                    .execute {
                        onNext { payload ->
                            Trace.i("pushToken", "${payload.isOk}")
                        }
                    }
        }
    }

    /**
     *网易云信IM初始化
     */
    private fun initNIM() {
        // 在初始化SDK的时候，传入 loginInfo()， 其中包含用户信息，用以自动登录
        NIMClient.init(this, getIMLoginInfo(), NimSDKOptionConfig.getSDKOptions(this))
        // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
        NimUIKit.setLocationProvider(NimDemoLocationProvider())
        // 以下逻辑只在主进程初始化时执行
        if (NIMUtil.isMainProcess(this)) {
            SessionHelper.init()
            NIMClient.toggleNotification(true)
            NimUIKit.init(this)
            NIMClient.getService(AuthServiceObserver::class.java).observeOnlineStatus({ statusCode ->
                when (statusCode) {
                    StatusCode.UNLOGIN -> Log.d("WJY", "未登录/登录失败")
                    StatusCode.NET_BROKEN -> Log.d("WJY", "网络连接已断开")
                    StatusCode.CONNECTING -> Log.d("WJY", "正在连接服务器")
                    StatusCode.LOGINING -> Log.d("WJY", "正在登录中")
                    StatusCode.SYNCING -> Log.d("WJY", "正在同步数据")
                    StatusCode.LOGINED -> Log.d("WJY", "已成功登录")
                    StatusCode.KICKOUT, StatusCode.KICK_BY_OTHER_CLIENT, StatusCode.FORBIDDEN -> {
                        Log.d("WJY", "被其他端的登录踢掉")
                        RetrofitUrlManager.getInstance().removeGlobalDomain()
                        PreferenceManager.getDefaultSharedPreferences(this).edit { putString(Extras.HTTPURL, "") }
                        resetPush(false)
                        IMLogout()
                        Store.store.clearUser(this)
                        ActivityHelper.exit(this)
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra(Extras.FLAG, statusCode)
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    StatusCode.VER_ERROR -> Log.d("WJY", "客户端版本错误")
                    StatusCode.PWD_ERROR -> Log.d("WJY", "用户名或密码错误")
                    else -> Log.d("WJY", "未知错误")
                }
            }, true)
        }

    }

    private fun getIMLoginInfo(): LoginInfo? {
        val xmlDb = XmlDb.open(applicationContext)
        val account = xmlDb.get(Extras.NIMACCOUNT, "")
        val token = xmlDb.get(Extras.NIMTOKEN, "")
        AnkoLogger("WJY").info { "account:$account===>token:$token" }
        return if (account.isNotEmpty() && token.isNotEmpty()) {
            NimUIKit.setAccount(account)//必须做这一步
            LoginInfo(account, token)
        } else {
            null
        }
    }

    /**
     * 网易云信IM注销
     */
    fun IMLogout() {
        val xmlDb = XmlDb.open(INSTANCE)
        xmlDb.set(Extras.NIMACCOUNT, "")
        xmlDb.set(Extras.NIMTOKEN, "")
        NIMClient.getService(AuthService::class.java).logout()
    }

    companion object {
        lateinit var INSTANCE: App
    }
}