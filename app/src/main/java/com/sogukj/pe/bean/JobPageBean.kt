package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/21.
 */
class JobPageBean {
    var pfbz: ArrayList<PFBZ>? = null
    var data: ArrayList<PageItem>? = null
    var groupScore: String? = null//	单人评总分	查看分数时显示

    class PageItem {
        var id: String? = null //指标id
        var name: String? = null //指标名
        var weight: String? = null //权重
        var type: Int? = null //类型
        var total_score: Int? = null //每项总分
        var score: String? = null //单项分数 查看分数时显示
    }
}