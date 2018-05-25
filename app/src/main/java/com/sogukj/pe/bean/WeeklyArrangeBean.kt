package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2018/3/16.
 */
class WeeklyArrangeBean : Serializable {
    var id: Int? = null
    var reasons: String? = null//事由
    var place: String? = null//地点
    var attendee: List<Person>? = null//出席人
    var participant: List<Person>? = null//参加人
    var date: String? = null//日期
    var weekday: String? = null//周几

    inner class Person : Serializable {
        var user_id: Int? = null
        var url: String? = null
        var name: String? = null
        var position: String? = null
    }
}