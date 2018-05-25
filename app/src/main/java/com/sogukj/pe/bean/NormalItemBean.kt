package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/22.
 */
class NormalItemBean {
    //var item: ArrayList<NormalItem>? = null
    var pfbz: ArrayList<PFBZ>? = null
    var data: ArrayList<NormalItem.BeanItem>? = null
    var jf: ArrayList<NormalItem.BeanItem>? = null
    var kf: ArrayList<NormalItem.BeanItem>? = null

    class NormalItem {
        var pName: String? = null
        var data: ArrayList<BeanItem>? = null

        class BeanItem {
            var id: Int? = null
            var name: String? = null
            var weight: Int? = null//14
            var total_score: Int? = null//140.0
            var actual: String? = null
            var type: Int? = null //1=>关键绩效指标评价 2=>岗位胜任力评价 3=>加分项 4=>减分项
            var offset: Int? = null
            var info: String? = null
            var score: String? = null//score	string	小项得分	查看分数时显示
        }
    }
}