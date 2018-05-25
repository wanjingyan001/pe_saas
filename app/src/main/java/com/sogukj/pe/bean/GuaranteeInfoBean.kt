package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/14.
 */
class GuaranteeInfoBean : Serializable {
    var creditor: String? = null//	债权人
    var obligor: String? = null//    债务人
    var creditoType: String? = null//    主债权种类
    var creditoAmount: String? = null//    主债权数额
    var creditoTerm: String? = null//    履行债务的期限
    var guaranteeTerm: String? = null//    保证的期间
    var guaranteeWay: String? = null//    保证的方式
    var guaranteeScope: String? = null//    保证担保的范围
}