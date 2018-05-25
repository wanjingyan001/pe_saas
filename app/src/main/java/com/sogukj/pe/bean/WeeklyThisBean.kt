package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by sogubaby on 2017/12/8.
 */
class WeeklyThisBean : Serializable {
    var automatic: ArrayList<Automatic>? = null//本周周报
    var week: Week? = null//补充记录

    class Automatic : Serializable{
        var date: String? = null//2017-10-06
//        var week_day: String? = null
        var data: ArrayList<WeeklyData>? = null

        class WeeklyData : Serializable{
            var time: String? = null//08:24
            var title: String? = null//投资经理在上海接见xxxxx
            var is_collect: Int? = null//1是 0否  用于显示AI采集图标和背景图区分
            var type: Int? = null//0日程，1任务 2会议 3用印审批 4签字审批 5跟踪记录 6项目 7请假 8出差
            var type_name: String? = null//来源类型名称---任务
            var data_id: Int? = null//来源数据id	  用于点击跳转对应来源页面
            var start_time: String? = null
            var end_time: String? = null
        }
    }

    class Week : Serializable {
        var time: String? = null//08:24
        var start_time: String? = null//2017-10-06
        var end_time: String? = null//2017-10-13
        var info: String? = null//周报内容
        var week_id: Int? = null//周报id
        var is_send_week = 2//是否发送过，2未发送，1发送过
    }
}