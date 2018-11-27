package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/25.
 */
class ProjectDetailBean : Serializable {
    var yu: Int? = 0
    var fu: Int? = 0
    //var yuQing: List<NewsBean>? = null//舆情新闻
    //var fuMian: List<NewsBean>? = null//负面新闻
    var counts: ArrayList<DetailBean>? = null
    var usual: ArrayList<DetailBean>? = null
    var is_focus: Int? = null//是否关注，1是关注，0未关注
    var type: Int? = null//0 群已存在，1 群不存在&可以建群，2 群不存在&不可以建群
    var group_id: String? = null//当type=0显示群id

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
            val floor : Int ? = null
//            "id": 60,
//            "name": "股权信息",
//            "state": 1,
//            "status": 1,
//            "module": 1,
//            "count": "0"
        }
    }
}