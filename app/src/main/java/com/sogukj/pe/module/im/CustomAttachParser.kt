package com.sogukj.pe.module.im

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser
import com.sogukj.pe.bean.MessageBean
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
        when (type) {
            in 100..199 -> {
                //审批
            }
            in 200..299 -> {
                //系统
            }
            in 300..399 -> {
                //点对点
            }
        }

        val attachment = ApproveAttachment()
        val bean = Gson().fromJson<MessageBean>(attach)
        attachment.messageBean = bean
        return attachment
    }
}