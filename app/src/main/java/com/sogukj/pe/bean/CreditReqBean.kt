package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/1.
 */
class CreditReqBean : Serializable {
    var id: Int? = null
    var company_id = 0//公司id
    var name = ""//姓名
    var position :String? = null//职位
    var phone:String?=null//电话
    var idCard:String?=null//身份证号
    var type = 0//级别(1=>董监高，2=>股东）
}