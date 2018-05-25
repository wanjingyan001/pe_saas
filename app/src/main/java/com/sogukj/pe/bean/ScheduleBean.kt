package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/5.
 */
class ScheduleBean : Serializable {
    var id: Int? = null//日程ID
    var title: String? = null//标题
    var start_time: String? = null//开始时间
    var end_time: String? = null//截止时间
    var name: String? = null//姓名  当stat=2返回
    var type: Int? = null//类别（0日程，1任务， 2会议 ，3用印审批， 4签字审批， 5跟踪记录， 6项目， 7请假， 8出差）
    var data_id: Int? = null//对应表ID
    var timing: String? = null//时间  当stat=1或2时返回时间段，当stat=3时返回时间点
    var is_finish: Int = 0//是否完成  （0=>未完成，1=>完成）仅当stat=3时返回
    var company_id: Int? = null//公司id  仅当stat=3时返回
    var is_collect: Int? = null//是否是自动采集(1隐藏复选框,0是自动采集已完成)
    var publisher: String? = null//发起人
}