package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/20.
 */
class TouZiUpload {
    //zuixin
    var performance_id: Int? = null
    var actual: String? = null
    //填写页面
    var standard: String? = null  //standard	string		评分标准
    var info: String? = null        //info	string		实际情况
    //提交页面
    var score: Int? = null  //每个考核项分数  wu权重
    var type: Int? = null        //类型（1:关键绩效指标评价 2:岗位胜任力评价 3加分项 4减分项）
}