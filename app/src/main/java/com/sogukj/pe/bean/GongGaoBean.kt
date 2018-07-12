package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2018/6/15.
 */
class GongGaoBean {
    var contents: String? = null//	公告内容
    var time: String? = null//  时间
    var title: String? = null// 公告标题
    var adjunct: ArrayList<GGFileBean>? = null

    class GGFileBean {
        var filename: String? = null
        var url: String? = null
    }
}