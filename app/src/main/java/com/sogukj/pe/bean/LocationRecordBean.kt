package com.sogukj.pe.bean

import java.util.*

/**
 * Created by sogubaby on 2018/8/8.
 */
class LocationRecordBean {
    var date: String? = null
    var week: String? = null
    var child: ArrayList<LocationCellBean>? = null

    class LocationCellBean {
        var id: Int? = null
        var type: Int? = null
        var time: Int? = null
        var place: String? = null

        var date: String? = null
        var longitude: String? = null
        var latitude: String? = null

        var sid: Int? = null
        var stype: String = ""
        var title: String = ""
        var add_time: String? =null
        var approve_type = 2 //审批类型  1老审批  2新

        fun getRealId(): Int? {
            return if (title == "不关联审批" || id == 0) {
                null
            } else {
                id
            }
        }
    }


//    "date": "2018-08-08",    //日期
//    "week": "星期三",    //周几
//    "child": [
//    {
//        "id": 1,
//        "type": 1,
//        "time": "08:48:35",    //打卡时间
//        "place": "firenze"    //打卡地点
//    },
//    {
//        "id": 2,
//        "type": 2,
//        "time": "15:49:22",
//        "place": "firenze"
//    }
//    ]
}