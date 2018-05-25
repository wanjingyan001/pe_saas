package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/10.
 */
class ModifiedTaskBean : Serializable {
    var id: Int? = null//id
    var type: Int? = null//类别
    var company_id: Int? = null//项目id
    var clock: Int? = null//提醒
    var info: String? = null//任务描述
    var start_time: String? = null//开始时间
    var end_time: String? = null//	结束时间
    var cName: String? = null//公司名称
    var executor: List<SelectBean>? = null//执行人
    var watcher: List<SelectBean>? = null//抄送人


    inner class SelectBean : Serializable {
        var id: Int? = null//人ID
        var name: String = ""//	姓名
        var url: String? = null//头像链接
    }
}