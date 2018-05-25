package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/16.
 */
class InvestEventBean : Serializable {
    var tzdate: String? = null//	Date	融资时间
    var money: String? = null//	Varchar	融资金额
    var lunci: String? = null//	Varchar	轮次
    var organization_name: String? = null//	Text	投资方
    var product: String? = null//	Varchar	产品名
    var location: String? = null//	Varchar	地区
    var yewu: String? = null//Varchar	业务范围
    var hangye1: String? = null//	Varchar	行业
    var iconOssPath: String? = null//	Varchar	天眼查logo路径
}