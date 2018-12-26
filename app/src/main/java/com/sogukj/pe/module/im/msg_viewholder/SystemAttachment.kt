package com.sogukj.pe.module.im.msg_viewholder

import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.bean.ReminderPush
import com.sogukj.pe.bean.SystemPushBean
import com.sogukj.pe.module.im.msg_viewholder.CustomAttachment

/**
 * Created by admin on 2018/11/2.
 */
class SystemAttachment : CustomAttachment() {
    var systemBean: SystemPushBean? = null
    var remindBean: ReminderPush? = null
    override fun toJson(send: Boolean): String {
        return when {
            systemBean != null -> systemBean.jsonStr
            remindBean != null -> remindBean.jsonStr
            else -> throw  Exception("类型错误")
        }
    }

    override fun toString(): String {
        val s1 = if (systemBean != null) systemBean.toString() else "systemBean null"
        val s2 = if (remindBean != null) remindBean.toString() else "remindBean null"
        return "$s1\n$s2"
    }
}