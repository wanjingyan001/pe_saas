package com.sogukj.pe.module.im

import android.util.Log
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.im.msg_viewholder.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.json.JSONObject

/**
 * Created by admin on 2018/9/21.
 */
class CustomAttachParser : MsgAttachmentParser {
    override fun parse(attach: String): MsgAttachment {
        AnkoLogger("WJY").info { "自定义消息:$attach" }
        val obj = JSONObject(attach)
        val type = obj.get("type") as Int
        var attachment: CustomAttachment? = null
        when (type) {
            100 -> {
                //新项目流程审批
                attachment = ProcessAttachment()
                val bean = Gson().fromJson<NewProjectProcess>(attach)
                attachment.bean = bean
            }
            in 101..199 -> {

                //点对点
            }
            in 200..299 -> {
                //系统( 206打卡提醒)
                attachment = SystemAttachment()
                if (type == 206) {
                    val remind = Gson().fromJson<ReminderPush>(attach)
                    attachment.remindBean = remind
                } else {
                    val sysBean = Gson().fromJson<SystemPushBean>(attach)
                    attachment.systemBean = sysBean
                }
                print(attachment.toString())
            }
            in 300..399 -> {
                //审批
                attachment = ApproveAttachment()
                val bean = Gson().fromJson<NewApprovePush>(attach)
                attachment.messageBean = bean
            }
            in 400..499 -> {
                //支付
                attachment = PayPushAttachment()
                val bean = Gson().fromJson<PayHistory>(attach)
                attachment.payPushBean = bean
            }
            else -> {
                throw  Exception("数据错误")
            }
        }
        return attachment!!
    }
}