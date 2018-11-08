package com.sogukj.pe.module.im.msg_viewholder

import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.bean.SystemPushBean
import com.sogukj.pe.module.im.msg_viewholder.CustomAttachment

/**
 * Created by admin on 2018/11/2.
 */
class SystemAttachment : CustomAttachment()  {
    lateinit var systemBean: SystemPushBean
    override fun toJson(send: Boolean): String {
        return systemBean.jsonStr
    }
}