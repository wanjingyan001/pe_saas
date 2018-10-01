package com.sogukj.pe.module.im

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.bean.MessageBean
import org.json.JSONObject

/**
 * Created by admin on 2018/9/21.
 */
class ApproveAttachment : MsgAttachment {
    lateinit var messageBean: MessageBean

    override fun toJson(send: Boolean): String {
        return messageBean.jsonStr
    }
}