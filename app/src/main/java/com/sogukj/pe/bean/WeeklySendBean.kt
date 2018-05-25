package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/5.
 */
class WeeklySendBean {
    var date: String? = null
    val data: ArrayList<WeeklySendBeanObj>? = null

    class WeeklySendBeanObj {
        var week: String? = null//每周时间段
        var week_id: Int? = null//周报ID
        var start_time: String? = null
        var end_time: String? = null
    }
}