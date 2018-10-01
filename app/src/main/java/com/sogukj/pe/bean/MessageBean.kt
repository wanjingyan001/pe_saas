package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/10/30.
 */
class MessageBean : Serializable {
    var news_id: Int? = null//	number	消息总数
    var approval_id: Int? = null//	number	审批id
    var add_time: Int? = null//
    var time: String? = null//   string    时间
    var title: String? = null//	string	审批标题
    var username: String? = null//	string	申请人
    var type: Int? = null//	1=>出勤休假, 2=>用印审批 ,3=>签字审批
    var status: Int? = null//	-1=>审批未通过, 1=>待审批,2=>已审批,3=>抄送给你, 4=>审批通过
    var status_str: String? = null
    var reasons: String? = null//	string	审批说明
    var urgent_count: Int? = null//	number	加急次数
    var message_count: Int? = null//	number	留言数量
    var type_name: String? = null//	string	用印类别
    val preapproval: String? = null//审批进度

    var contents: String? = null
    var has_read: Int? = null

    var finishNum: Int? = null
    var waitNum: Int? = null
//    "finishNum": 4,
//    "waitNum": 0
}