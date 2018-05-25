package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/21.
 */
class TaxRateBean : Serializable {
    var name: String? = null//	Varchar	纳税人名称
    var year: Int? = null//	Int	年份
    var idNumber: String? = null//	Varchar	纳税人识别号
    var grade: String? = null//		Varchar	纳税信用级别
}