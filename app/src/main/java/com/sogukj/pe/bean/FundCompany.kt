package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by sogubaby on 2018/5/23.
 */
class FundCompany : Serializable {
    //    1	had_invest	string	投资金额
//    2	invest_time	string	投资时间
//    3	company_name	string	项目名称
//    4	logo	string	logo
//    5	manager_name	string	投资经理
//    6	type	int	投资状态	1立项,2已投,4储备,5部分退出,6调研,7全部退出
    var projId: Int? = null
    var had_invest: String? = null//投资金额
    var invest_time: String? = null//投资时间
    var company_name: String? = null//项目名称
    var logo: String? = null//logo
    var manager_name: String? = null//投资经理
    var type: Int? = null//投资状态	1立项,2已投,4储备,5部分退出,6调研,7全部退出
}