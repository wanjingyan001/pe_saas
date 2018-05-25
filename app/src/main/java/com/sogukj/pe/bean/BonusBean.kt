package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/9.
 */
class BonusBean : Serializable {
    var boardDate: String? = null//	Date	董事会日期
    var shareholderDate: String? = null//	Varchar	股东大会日期
    var implementationDate: String? = null//	Varchar	实施日期
    var introduction: String? = null//	Varchar	分红方案说明
    var asharesDate: String? = null//	Varchar	A股股权登记日
    var acuxiDate: String? = null//	Varchar	A股除权除息日
    var adividendDate: String? = null//	Varchar	A股派息日
    var progress: String? = null//	Varchar	方案进度
    var dividendRate: String? = null//	Varchar	分红率(%)
}