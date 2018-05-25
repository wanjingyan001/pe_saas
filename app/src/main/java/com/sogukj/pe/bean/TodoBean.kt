package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/7.
 */
class TodoYear(val year: String) : Serializable


class TodoDay(val dayTime: String) : Serializable


/**
 *任务列表item
 */
class TaskItemBean : Serializable {
    var date: String = ""
    var data = ArrayList<ItemBean>()

    inner class ItemBean : Serializable {
        var id: Int = 0//日程ID
        var title: String = ""//标题
        var end_time: String = ""//截止时间
        var is_finish: Int = 0//是否完成（1=>完成，0=>未完成）
        var leader: String? = null//董事长安排  非董事长 此字段不传
        var data_id: Int = 0//任务ID
    }
}

class Users : Serializable {
    var selectUsers = ArrayList<UserBean>()
}