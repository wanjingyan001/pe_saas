package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2018/3/26.
 */
class LeaveRecordBean {
    var title: String? = null//标题
    var status: Int? = null//状态    -1=>不通过，0=>待审批，1=>审批中，4=>审批通过
    var add_time: String? = null//申请时间
    var start_time: String? = null//开始时间
    var end_time: String? = null//结束时间
    var name: String? = null//请假名
    var url: String? = null//头像
}