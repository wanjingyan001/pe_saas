package com.sogukj.pe.bean

import java.io.File
import java.io.Serializable

open class UserBean : Serializable {
    var uid: Int? = null//用户主键ID
    var name: String = ""//姓名
    var phone: String = ""//手机号码
    var depart_name: String = ""//部门名称
    var position: String = ""//职位
    var email: String = ""//邮件
    var project: String = ""//项目
    var memo: String = ""//备注
    var url: String? = null
    var depart_id: Int? = null//部门ID
    var company: String? = null//所属公司
    var numberOfShares: Int = 0
    var is_admin: Int = 0
    var user_id: Int? = null
    var full: String? = null
    var token: String? = null
    var accid: String? = null//网易云id
    var address: String? = null//公司地址


    fun headImage(): String? {
        if (null == url) return null
        val file = File(url)
        if (file.exists()) return url
        return "${url}"
    }

    fun imageId(): String {
        url?.let {
            return if (it.contains("?")) {
                it.substring(0, it.indexOf("?"))
            } else {
                it
            }
        }
        return ""
    }
}
