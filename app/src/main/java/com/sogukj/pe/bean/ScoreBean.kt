package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by sogubaby on 2017/12/20.
 */
class ScoreBean : Serializable {
    var name: String? = null//   姓名
    var url: String? = null// 头像链接

    var resumption: String? = null//   岗位胜任力指标评价
    var achieve_check: String? = null//  关键绩效指标评价
    var total_grade: String? = null//   最终得分

    var user_id: Int? = null  // 被评论人id


    var level: String? = null//   最终得分
}