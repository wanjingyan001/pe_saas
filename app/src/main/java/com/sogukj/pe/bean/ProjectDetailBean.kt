package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/25.
 */
class ProjectDetailBean : Serializable {
    var yu: Int? = null
    var fu: Int? = null
    //var yuQing: List<NewsBean>? = null//舆情新闻
    //var fuMian: List<NewsBean>? = null//负面新闻
    var counts: ArrayList<DetailBean>? = null
    var usual: ArrayList<DetailBean>? = null
    var is_focus: Int? = null//是否关注，1是关注，0未关注

    class DetailBean {
        var title: String? = null
        var state: Int? = null
        var value: ArrayList<DetailSmallBean>? = null

        class DetailSmallBean {
            var id: Int? = null
            var name: String? = null
            var state: Int? = null
            var status: Int? = null
            var module: Int? = null
            var count: String? = null
//            "id": 60,
//            "name": "股权信息",
//            "state": 1,
//            "status": 1,
//            "module": 1,
//            "count": "0"
        }
    }
}