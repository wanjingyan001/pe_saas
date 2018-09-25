package com.sogukj.pe.module.im

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.sogukj.pe.baselibrary.Extended.jsonStr
import org.json.JSONObject

/**
 * Created by admin on 2018/9/21.
 */
class DefaultCustomAttachment : MsgAttachment {
    lateinit var content:String
    override fun toJson(send: Boolean): String {
        val json = JSONObject()
        json.put("type",0)
        json.put("data",content)
        return json.jsonStr
    }
}