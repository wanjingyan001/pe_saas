package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/10/30.
 */
class MessageBean : Serializable {
    var news_id: Int? = null//	number	消息总数
    var approval_id: Int? = null//	number	审批id
    var time: String? = null//   string    时间
    var title: String? = null//	string	审批标题
    var username: String? = null//	string	申请人
    var type: Int? = null//	number	类别	1=>出勤休假, 2=>用印审批 ,3=>签字审批
    var status: Int? = null//	number	审批状态	1待审批，2 已审批
    var status_str: String? = null
    var reasons: String? = null//	string	审批说明
    var urgent_count: Int? = null//	number	加急次数
    var message_count: Int? = null//	number	留言数量
    var type_name: String? = null//	string	用印类别
}