package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/8.
 */
class CompanyBean : Serializable {
    var companyName: String? = null//	Varchar	公司名
    var engName: String? = null//	Varchar	英文名
    var industry: String? = null//	Varchar	行业
    var usedName: String? = null//Varchar	曾用名
    var mainBusiness: String? = null//	Varchar	主营业务
    var registeredCapital: String? = null//	Varchar	注册资金
    var employeesNum: String? = null//	mediumint 员工数量
    var controllingShareholder: String? = null//	Char	控股股东
    var actualController: String? = null//	Char	实际控股
    var finalController: String? = null//	Char	最终控股

    var legal:String?=null
    var chairman:String?=null
    var generalManager:String?=null
    var secretaries:String?=null

}

class Data:Serializable{
    var id:String?=null
    var cType:Int?=null
    var name:String?=null
}