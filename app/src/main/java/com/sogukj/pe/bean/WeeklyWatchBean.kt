package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by sogubaby on 2017/12/6.
 */
class WeeklyWatchBean {
    var date: String? = null//10.06-10.13
    var start_time: String? = null//2017-10-06
    var end_time: String? = null//2017-10-13
    var data: ArrayList<BeanObj>? = null

    class BeanObj : Serializable {
        var url: String? = null
        var name: String? = null
        var user_id: Int? = null
        var is_read: Int? = null//2=>已读，1=>未读,null=>全部
        var week_id: Int? = null
        var date: String? = null//1994-07-12 12:12:12
        //
        var start_time: String? = null//2017-10-06
        var end_time: String? = null//2017-10-13
    }

    fun clone(): WeeklyWatchBean {
        var cloObj = WeeklyWatchBean()
        cloObj.date = this.date
        cloObj.data = ArrayList<BeanObj>(this.data)
        return cloObj
    }
}