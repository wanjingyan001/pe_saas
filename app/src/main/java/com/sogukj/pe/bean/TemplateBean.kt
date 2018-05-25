package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/28.
 */
class TemplateBean {
    var jx: ArrayList<InnerItem>? = null
    var jf: ArrayList<InnerItem>? = null
    var kf: ArrayList<InnerItem>? = null

    class InnerItem {
        var performance_id: Int? = null
        var name: String? = null  //考核要素
        var info: String? = null  //工作目标
        var type: Int? = null     //类型（1:关键绩效指标评价 2:岗位胜任力评价 3加分项 4减分项）
    }
}