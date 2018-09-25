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
        val attachment = ApproveAttachment()
        val bean = Gson().fromJson<MessageBean>(attach)
        attachment.messageBean = bean
        return attachment
    }
}