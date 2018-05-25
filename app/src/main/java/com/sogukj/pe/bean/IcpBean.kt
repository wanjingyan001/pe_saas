package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/23.
 */
class IcpBean : Serializable {
    var webName: String = ""//	Varchar	网站名称
    var webSite: String = ""//	Varchar	网站首页
    var examineDate: String = ""//	Date	审核时间
    var liscense: String = ""//	Varchar	备案号
    var companyType: String = ""//	Varchar	主办单位性质
}