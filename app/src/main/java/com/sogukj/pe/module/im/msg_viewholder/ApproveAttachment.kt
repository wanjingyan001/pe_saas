package com.sogukj.pe.module.im.msg_viewholder

import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.bean.NewApprovePush

/**
 * Created by admin on 2018/9/21.
 */
class ApproveAttachment : CustomAttachment() {
    lateinit var messageBean: NewApprovePush

    override fun toJson(send: Boolean): String {
        return messageBean.jsonStr
    }
}