package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/23.
 */
class PatentBean : Serializable {
    var patentName: String? = null//	Varchar	专利名称
    var applicationPublishNum: String? = null//	Varchar	申请公布号
    var patentNum: String? = null//	Varchar	申请号/专利号
    var applicationTime: String? = null//	Varchar	申请日
    var applicationPublishTime: String? = null//	Varchar	申请公布日
    var inventor: String? = null//	Varchar	发明人
    var applicantName: String? = null//	Varchar	申请人
    var agency: String? = null//	Varchar	代理机构
    var address: String? = null//Varchar	地址
    var agent: String? = null//	Varchar	代理人
    var abstracts: String? = null//	Text	摘要
}