package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/10.
 */
class TaskModifyBean : Serializable {
    var data_id: Int? = null
    var type: Int = -1//0=>日程，1=>任务，2=>会议
    var ae = DetailBean()

    inner class DetailBean {
        var company_id: Int? = null//项目id
        var info: String = ""//任务描述
        var start_time: String? = null//开始时间
        var end_time: String = ""//结束时间
        var clock: Int? = null//提醒(传秒数，例如截止前15分钟，请传900)
        var executor: String? = null//执行人UID（逗号隔开，例如'1,2,3'）
        var watcher: String? = null//抄送人
    }
}