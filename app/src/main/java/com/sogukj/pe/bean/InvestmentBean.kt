package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/14.
 */
class InvestmentBean : Serializable {
    var outcompanyName: String? = null//	对外投资企业名称
    var regNum: String? = null//		注册号
    var creditCode: String? = null//		统一信用代码

   var name:String?=null//	Varchar	公司名
   var legalPersonName:String?=null//	Varchar	法人
   var regCapital:String?=null//	Varchar	注册资本
   var amount:String?=null//	Varchar	投资数额
   var pencertileScore:String?=null//	Int	评分
   var regStatus:String?=null//	varchar	经营状态
}