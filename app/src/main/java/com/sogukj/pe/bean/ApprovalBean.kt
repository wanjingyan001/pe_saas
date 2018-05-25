package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/10/25.
 */
class ApprovalBean : Serializable {
    var approval_id: Int? = null//	number	审批用印id
    var title: String? = null//	string	审批标题
    var kind: String? = null//	string	用印类别
    var name: String? = null//	string	申请人
    var add_time: String? = null//	date	添加时间
    var status_str: String? = null//	string	审批状态
    var type: Int? = null//	number	类型
    var status: Int? = null//	number	状态

}