package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/26.
 */
class ScheduleDetailsBean : Serializable {
    var info: Info? = null
    var record: List<Record>? = null

    inner class Info : Serializable {
        var number: String? = null//任务编号
        var cName: String = ""//关联项目
        var publisher: String? = null//任务发布者
        var timing: String? = null//安排时间
        var info: String? = null//任务详情
        var executor: String? = null//任务执行者
        var watcher: String? = null//抄送人
        var data_id: Int? = null//此条任务ID
        var type: Int? = null//类型 (1=>任务，2=>会议,3=>日程)
        var pub_id: Int? = null//发布者id  根据此判断是否隐藏显示修改按钮
        var clock: Int? = null//截止时间
    }

    inner class Record : Serializable {
        var type: String? = null//记录类型 1=>布置任务  2=>接受任务 3=>拒绝任务 4=>评价任务
        var time: String? = null//评论时间
        var content: String? = null//评论内容
        var name: String? = null//评论者
        var url: String? = null//图片链接
    }

}