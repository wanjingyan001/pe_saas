package com.sogukj.pe.bean

/**
 * Created by qinfei on 17/10/23.
 */
class ApproverBean {
    var position: String? = null//": "经办人",
    var approver: String? = null//": "吴隽雯,芦伊莎",
    var floor: Int? = null//": 1
    var hid: Int? = null//	number	审批人hid
    //    var position: String? = null//    string    审批人所处位置    头像地址可能为空
    var name: String? = null//    string    审批人姓名
    var url: String? = null//    string    审批人头像地址
    var approval_time: String? = null//    string    审批时间
    var status: Int? = null//    string    审批结果
    var status_str: String? = null//    string    审批结果
    var content: String? = null//   string    审批意见
    var is_edit_file: Int? = null//    string    是否有修改用印清单文件
    var comment: List<ApproveViewBean.CommentBean>? = null//   array    评论
    var user_id: Int? = null//   number    人id
    var sign_img: String? = null
}