package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/11.
 */
class BizInfoBean : Serializable {
    var legalPersonName: String? = null//	Varchar	法定代表人
    var estiblishTime: String? = null//    Datetime    成立日期
    var regCapital: String? = null//  Varchar    注册资本
    var regNumber: String? = null//   Varchar    工商注册号
    var orgNumber: String? = null//  Varchar    组织机构代码
    var creditCode: String? = null//   Varchar    统一信用代码
    var idNumber: String? = null// Varchar    纳税人识别号
    var regStatus: String? = null//  Varchar    经营状态
    var companyOrgType: String? = null//   Varchar    企业类型
    var industry: String? = null//   Varchar    行业
    var toTime: String? = null//  Datetime    营业限制
    var regLocation: String? = null//  Varchar    注册地址
    var approvedTime: String? = null//   Datetime    核准日期
    var regInstitute: String? = null//  Varchar
    var businessScope: String? = null
}