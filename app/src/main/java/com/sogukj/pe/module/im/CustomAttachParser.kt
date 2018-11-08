package com.sogukj.pe.module.im

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser
import com.sogukj.pe.bean.NewApprovePush
import com.sogukj.pe.bean.NewProjectProcess
import com.sogukj.pe.bean.SystemPushBean
import com.sogukj.pe.module.im.msg_viewholder.ApproveAttachment
import com.sogukj.pe.module.im.msg_viewholder.CustomAttachment
import com.sogukj.pe.module.im.msg_viewholder.ProcessAttachment
import com.sogukj.pe.module.im.msg_viewholder.SystemAttachment
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
                //系统
                attachment = SystemAttachment()
                val sysBean = Gson().fromJson<SystemPushBean>(attach)
                attachment.systemBean = sysBean
            }
            in 300..399 -> {
                //审批
                attachment = ApproveAttachment()
                val bean = Gson().fromJson<NewApprovePush>(attach)
                attachment.messageBean = bean
            }
            else -> {
                throw  Exception("数据错误")
            }
        }
        return attachment!!
    }
}