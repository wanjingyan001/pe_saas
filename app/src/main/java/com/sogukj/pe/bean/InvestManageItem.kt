package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/21.
 */
class InvestManageItem {
    var pName: String? = null
    var data: ArrayList<InvestManageInnerItem>? = null
    var pfbz: ArrayList<PFBZ>? = null

    class InvestManageInnerItem {
        var id: Int? = null
        var name: String? = null
        var weight: Int? = null//14
        var total_score: Int? = null//140.0
        var target: String? = null
        var type: Int? = null //1=>关键绩效指标评价 2=>岗位胜任力评价 3=>加分项 4=>减分项
        var offset: Int? = null
        var desc: String? = null
        var info: String? = null
        var standard: String? = null
        var score: String? = null//score	string	小项得分	查看分数时显示
        var selfScore: Double? = null//selfScore	number	个人得分	风控部 尽调项目数量type=>2时所得总分(已加权)
    }
}