package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/8.
 */
class CanGuBean : Serializable {
    var name: String = ""//	Varchar	公司名
    var relationship: String = ""//	Varchar	参股关系
    var participationRatio: String = ""//	Float	参股比例（%）
    var investmentAmount: String = ""//	Mediumint	投资金额（万元）
    var profit: String = ""//	Varchar	被参股公司 净利润(元)
    var reportMerge: String = ""//	Enum	是否报表合并
    var mainBusiness: String = ""//	Text	被参股公司主营业务
}