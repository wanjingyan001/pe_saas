package com.sogukj.pe.bean

/**
 * Created by qinfei on 17/10/17.
 */
class StoreProjectBean {
    var name: String? = null//	varchar	公司名称
    var shortName: String? = null//	varchar	公司名称
    //    var uid	int	用户id
    var info: String? = null//   text    相关概念
    var estiblishTime: String? = null//yyyy-MM-dd    date    成立时间
    var enterpriseType: String? = null//    varchar    企业性质
    var regCapital: String? = null//    varchar    注册资金
    var mainBusiness: String? = null//   varchar    主营业务
    var ownershipRatio: String? = null//    varchar    股权比例
    var lastYearIncome: String? = null//   varchar    去年营收
    var lastYearProfit: String? = null//   varchar    去年利润
    var thisYearIncome: String? = null//   varchar    今年营收
    var thisYearProfit: String? = null//   varchar    今年利润
    var lunci: String? = null//    varchar    融资轮次
    var appraisement: String? = null//    varchar    投后估值
    var financeUse: String? = null//  varchar    融资用途
    var capitalPlan: String? = null//   varchar    资本规划
}