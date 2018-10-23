package com.sogukj.pe.module.approve.baseView.viewBean

import com.google.gson.internal.LinkedTreeMap

/**
 * 显示审批
 * Created by admin on 2018/10/17.
 */
data class ApproveDetail(
        var fixation: Fixation,
        var relax: List<Relax>?,
        var files: List<File>?,
        var flow: List<Flow>?,
        var handle: List<Handle>?,
        /**
         * 1=>'申请加急',2=>'修改',3=>'重新发起',4=>'撤销',5=>'审批',6=>'导出（用印|审批）单',7=>'（用印|流程）完成',8=>'确认意见并签字',9=>'文件签发',10=>'评论'
         */
        var click: List<Int>?,
        var button: List<Button>?,
        /**
         * 抄送人
         */
        var copier: List<Copier>?
)

/**
 * 经办人
 */
data class Handle(
        var uid: Int,// 经办人id
        var name: String,//经办人姓名
        var url: String,//头像
        var status_str: String,// 待处理
        var approval_time: String?//处理时间
)

/**
 *  发起人基础信息和审批基础信息
 */
data class Fixation(
        var phone: String,// 手机号
        var uid: Int,// 发起人uid
        var name: String,//发起人姓名
        var url: String,// http://prepewinner.oss-cn-hangzhou.aliyuncs.com/uploads/headimg/5ae18f59c9193.jpg?OSSAccessKeyId=dZwbJBSoG9OREtPi&Expires=1538292928&Signature=14mRyMpwGCHvBx+OMfz6IUKGqYM=
        var state: Int,// 0正常，1被重新发起的，2撤销 ,3修改
        var add_time: String,//提交时间
        var title: String,// 审批标题
        var number: String,// 审批编号
        var mainStatus: Int?,// 审批主状态 -1审批不通过，0待审批，1审批中，2审批人都通过，3待用印，4审批真正完成, 5撤销成功
        var tid: Int,//  审批模板id
        var tName: String,//审批模板名
        var control: Int,// -1=>请假，-2=>出差，-3=>外出，-21=>基金用印，-22=>管理公司用印,-23=>外资用印,-41=>报销
        var group: Int,// 对应分组
        var groupName: String,// 分组中文名
        var depart_name: String// 发起人职位
)

/**
 * 发起人输入的数据
 */
data class Relax(
        var key: String,// 单行输入框
        var value: String?// 测试
)

/**
 * 发起人提交的文件列表
 */
data class File(
        var name: String,// login.jpg
        var url: String,// http://prepewinner.oss-cn-hangzhou.aliyuncs.com/uploads/flow/5bb0410eca7eb.jpg?OSSAccessKeyId=dZwbJBSoG9OREtPi&Expires=1538295748&Signature=Y7W8uz42F+L04cj79eA/D39k/IM=
        var size: String// 28.04KB
)

/**
 * 审批流程记录
 */
data class Flow(
        var id: Int,// 审批记录id
        var approval_time: String,//审批时间
        var uid: Int,// 审批人id
        var status: Int,// -1已过期，0待审批，1已同意   2已否决  3重新提交, 4撤销，5已修改, 6已弃权，7评论
        var floor: Int,//审批人所处层次
        var content: String?,//评论
        var is_edit_file: Int?,// status为3重新提交时  是否有修改用印清单文件 1有 0没有
        var sign_img: String,//签字审批的签字图片
        var name: String,// 审批人姓名
        var url: String,// http://prepewinner.oss-cn-hangzhou.aliyuncs.com/uploads/headimg/5b2200f33d855.jpg?OSSAccessKeyId=dZwbJBSoG9OREtPi&Expires=1538292928&Signature=mEw+v/Px6bi+KFaQMUXhINAFvLg=
        var attach: List<Attach>?, //  附件
        var position: String,// 层级名
        var comment: List<Comment>? //留言列表
) {
    val getStatusStr: String
        get() {
            return when (status) {
                -1 -> "已过期"
                0 -> "待审批"
                1 -> "已同意"
                2 -> "已否决"
                3 -> "重新提交"
                4 -> "撤销"
                5 -> "已修改"
                6 -> "已弃权"
                7 -> "评论"
                else -> ""
            }
        }
}

/**
 *  附件
 */
data class Attach(
        var name: String,// login.jpg
        var url: String,// http://prepewinner.oss-cn-hangzhou.aliyuncs.com/uploads/flow/5bb0410eca7eb.jpg?OSSAccessKeyId=dZwbJBSoG9OREtPi&Expires=1538295748&Signature=Y7W8uz42F+L04cj79eA/D39k/IM=
        var size: String// 28.04KB
)

/**
 * 留言
 */
data class Comment(
        var uid: Int,// 留言者id
        var name: String,// 史月寒
        var url: String,// http://www.hticm.com/upload/201610/27/201610270945285344.jpg
        var comment_id: Int,// 留言记录id
        var add_time: String,// 留言时间
        var pid: Int,// 记录的父id
        var reply: String,// 被回复人姓名
        var content: String// 留言内容
)

/**
 * 展示的按钮
 */
data class Button(
        var key: Int,
        var value: String
)

/**
 * 抄送人
 */
data class Copier(
        var id: Int,// 1
        var name: String,// 黄凌冰
        var url: String// http://prepewinner.oss-cn-hangzhou.aliyuncs.com/uploads/headimg/5ad81fe39b66e.jpeg?OSSAccessKeyId=dZwbJBSoG9OREtPi&Expires=1538292929&Signature=8DeNYLtgwDog1NviiAlpjV6X6bs=
)