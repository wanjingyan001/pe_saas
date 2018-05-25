package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/17.
 */
class ProductBean : Serializable {
    var product: String? = null//	Varchar	产品名
    var yewu: String? = null//	Varchar	类型
    var hangye: String? = null//	Varchar	描述
    var logo: String? = null//	Varchar	logo来源

    var jingpinProduct: String? = null//Varchar	产品名
    //   var yewu: String? = null//	Varchar	业务
    var date: String=""//	Date	成立日期
    var round: String? = null//	Varchar	轮次
    var companyName: String=""//	Varchar	关联公司
    var icon: String? = null//	Varchar	logo来源
}