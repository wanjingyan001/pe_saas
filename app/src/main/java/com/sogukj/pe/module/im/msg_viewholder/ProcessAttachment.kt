package com.sogukj.pe.module.im.msg_viewholder

import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.bean.NewProjectProcess

/**
 * Created by admin on 2018/11/2.
 */
class ProcessAttachment : CustomAttachment() {
    lateinit var bean: NewProjectProcess
    override fun toJson(send: Boolean): String {
      return bean.jsonStr
    }
}