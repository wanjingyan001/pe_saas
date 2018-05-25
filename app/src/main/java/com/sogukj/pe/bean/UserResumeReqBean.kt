package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/4.
 */
class UserResumeReqBean : Serializable {
    var position: String? = null//职位
    var sex: Int? = 0//性别（1=>男，2=>女，3=>未知）
    var language: String? = null//语言
    var workYear: Int? = 0//工作年限
    var city: Int? = 0//所在城市
    var educationLevel: String? = null//最高学历
    var email: String? = null//联系邮箱
}