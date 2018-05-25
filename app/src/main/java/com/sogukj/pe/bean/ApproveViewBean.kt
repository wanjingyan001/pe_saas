package com.sogukj.pe.bean

/**
 * Created by qinfei on 17/10/27.
 */
class ApproveViewBean {
    var fixation: FromBean? = null//	object	申请人信息
    var relax: List<ValueBean>? = null    //array	申请信息
    var file_list: List<FileBean>? = null   //array	用印文件清单
    var approve: List<ApproverBean>? = null  //array	审批
    var export: List<ApproverBean>? = null    //object	用印环节
    var click: Int? = null    //number	按钮	1=>申请加急，2=>审批完成，3=>重新发起审批，4=>导出审批单，用印完成，5=>审批
    var segment: List<ApproverBean>? = null
    var mainStatus: Int? = null
    var copier: ArrayList<UserBean>? = null

    class FromBean {
        var user_id: Int? = null//	string	申请人ID
        var name: String? = null//	string	申请人姓名
        var url: String? = null//   string    申请人头像地址    头像地址可能为空
        var number: String? = null//   string    编号
        var add_time: String? = null//   string    申请时间
        var sp_type: String? = null//  string    审批类型
        var depart: String? = null//  string    部门
        var leaveType: Int? = null//10=>出差，11=>请假
        var state: Int? = null//0正常   1修改后    2撤销后
    }

    class ValueBean {
        var name: String? = null
        var value: String? = null
    }

    class FileBean {
        var file_name: String? = null
        var url: String? = null
    }

    class CommentBean {
        var name: String? = null//: "史月寒",
        var url: String? = null//: "http://www.hticm.com/upload/201610/27/201610270945285344.jpg",
        var comment_id: Int? = null//: 1,
        var add_time: String? = null//: "前天 18:44",
        var pid: Int? = null//: 0,
        var content: String? = null//: "干嘛拒绝我",
        var reply: String? = null//: ""
    }

}