/**
 * Copyright (C), 2018-2019, 搜股科技有限公司
 * FileName: HWSupportPushReceiver
 * Author: admin
 * Date: 2019/1/23 上午11:44
 * Description: 华为推送支持
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
package com.sogukj.pe.service;

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.huawei.hms.support.api.push.PushReceiver

/**
 * @ClassName: HWSupportPushReceiver
 * @Description: 类描述
 * @Author: admin
 * @Date: 2019/1/23 上午11:44
 */
class HWSupportPushReceiver : PushReceiver() {
    interface IPushCallback {
        fun onReceive(intent: Intent)
    }

    companion object {
        const val TAG = "HWSupportPushReceiver"
        const val ACTION_UPDATEUI = "action.updateUI"
        const val ACTION_TOKEN = "action.updateToken"
        private val pushCallbacks: ArrayList<IPushCallback> = ArrayList()
        private val CALLBACK_LOCK = Any()

        fun registerPushCallback(callback: IPushCallback) {
            synchronized(CALLBACK_LOCK) {
                pushCallbacks.add(callback)
            }
        }

        fun unRegisterPushCallback(callback: IPushCallback) {
            synchronized(CALLBACK_LOCK) {
                pushCallbacks.remove(callback)
            }
        }

        private fun callBack(intent: Intent) {
            synchronized(CALLBACK_LOCK) {
                for (callback in pushCallbacks) {
                    callback.onReceive(intent)
                }
            }
        }
    }

    override fun onToken(context: Context, tokenIn: String, extras: Bundle) {
        super.onToken(context, tokenIn, extras)
        val belongId = extras.getString("belongId")

        var intent = Intent()
        intent.action = ACTION_TOKEN
        intent.putExtra(ACTION_TOKEN, tokenIn)
        callBack(intent)

        intent = Intent()
        intent.action = ACTION_UPDATEUI
        intent.putExtra("log", "belongId is:$belongId Token is:$tokenIn")
        callBack(intent)
    }

    override fun onPushMsg(context: Context, msg: ByteArray, extras: Bundle): Boolean {
        try {
            //CP可以自己解析消息内容，然后做相应的处理 | CP can parse message content on its own, and then do the appropriate processing
            val content = String(msg)
            val intent = Intent()
            intent.action = ACTION_UPDATEUI
            intent.putExtra("log", "Receive a push pass message with the message:$content")
            callBack(intent)
        } catch (e: Exception) {
            val intent = Intent()
            intent.action = ACTION_UPDATEUI
            intent.putExtra("log", "Receive push pass message, exception:" + e.message)
            callBack(intent)
        }
        return false
    }

    override fun onEvent(context: Context, event: Event, extras: Bundle) {
        val intent = Intent()
        intent.action = ACTION_UPDATEUI

        var notifyId = 0
        if (PushReceiver.Event.NOTIFICATION_OPENED == event || PushReceiver.Event.NOTIFICATION_CLICK_BTN == event) {
            notifyId = extras.getInt(PushReceiver.BOUND_KEY.pushNotifyId, 0)
            if (0 != notifyId) {
                val manager = context
                        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(notifyId)
            }
        }
        val message = extras.getString(PushReceiver.BOUND_KEY.pushMsgKey)
        intent.putExtra("log", "Received event,notifyId:$notifyId msg:$message")
        callBack(intent)
        super.onEvent(context, event, extras)
    }

    override fun onPushState(context: Context, pushState: Boolean) {
        super.onPushState(context, pushState)
        val intent = Intent()
        intent.action = ACTION_UPDATEUI
        intent.putExtra("log", "The Push connection status is:$pushState")
        callBack(intent)
    }
}