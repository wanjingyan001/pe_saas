package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/11/28.
 */
class InvestSuggestBean {
    var plan: Plan? = null
    var other: Other? = null

    class Plan {
        var valuation: String? = null//估值
        var amount: String? = null//金额
        var equityRatio: String? = null//股权比例
        var mainCast: String? = null//主投/跟投
        var position: String? = null//董事/监事职位
        var estimateTime: String? = null//预计时间
    }

    class Other {
        var motivation: String? = null//投资动因
        var riskTreat: String? = null//风险对策
        var managePlan: String? = null//投后管理计划
        var quitPlan: String? = null//退出计划
        var sensibilyAnalysis: String? = null//回报敏感性分析
        var otherIssue: String? = null//其他投决会关注的问题
        var contract: String? = null//合同文本
    }
}