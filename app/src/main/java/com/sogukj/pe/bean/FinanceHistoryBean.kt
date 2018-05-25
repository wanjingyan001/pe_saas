package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/16.
 */
class FinanceHistoryBean : Serializable {
    var date: String? = null//	Date	融资日期
    var round: String? = null//	Varchar	轮次
    var value: String? = null//	Varchar	融资金额	若空显示未披露
    var organizationName: String? = null//	Text	投资方
}