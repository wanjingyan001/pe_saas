package com.sogukj.pe.module.approve.baseView.viewBean

import android.support.annotation.IntDef
import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable
import java.lang.annotation.RetentionPolicy


/**
 * Created by admin on 2018/10/8.
 */
data class AttachmentBean(val name: String,
                          val url: String,
                          val size: String)


/**
 * 审批控件中的值
 */
data class ApproveValueBean(val name: String,
                            val id: Int? = null,
                            val url: String? = null,
                            val size: String? = null,
                            var value: Int? = null,
                            var scal_unit: String? = null,
                            val hours: String? = null,
                            val status: String? = null,
                            val title: String? = null,
                            val number: String? = null,
                            val add_time: String? = null,
                            val format: String? = null) : Serializable

/**
 * 审批人/抄送人/经办人
 */
data class Approvers(
        var sp: List<Sp>,
        var cs: Cs,
        var jr: String?// 30
)

data class Sp(
        var position: String,// 名称
        var pattern: Int,//  1=>依次审批，2=>会签，3=>或签
        var cla: Int,//  1=>主管，2=>指定成员，3=>角色
        var users: List<User>
)

data class User(
        var id: String,// 2
        var name: String,// 尹加久
        var url: String// http://prepewinner.oss-cn-hangzhou.aliyuncs.com/uploads/headimg/5b2200f33d855.jpg?OSSAccessKeyId=dZwbJBSoG9OREtPi&Expires=1538116852&Signature=tTRIGWsCGZF9OE0i%2BKC%2BrUng5nk%3D
)

data class Cs(
        var def: String,//默认抄送人 以逗号隔开
        var users: MutableList<User>
)

/**
 * 假期
 */
data class MyLeaveBean(
        var id: Int,// 1
        var name: String,// 年假
        var hours: String,// 109
        var status: Int//  1->还剩余时间，0->已申请时间
)

/**
 * 关联审批单
 */
data class Document(
        var id: Int,// 1
        var title: String,// 尹加久的请假
        var number: String,// CPCP201809251610294746
        var add_time: String// 2018-09-25 16:10:29
)

/**
 * 城市选择
 */
data class CityBean(
        var id: Int,// 2
        var name: String,// 北京
        var pid: Int,// 0
        var children: List<CChildren>
) : AbstractExpandableItem<CChildren>(), MultiItemEntity {
    override fun getLevel(): Int = 1

    override fun getItemType(): Int = 1

}

data class CChildren(
        var id: Int,
        var name: String,
        var pid: Int
) : MultiItemEntity, Serializable {
    override fun getItemType(): Int = 2
}

/**
 * 成员选择
 */
data class ApprovalUser(
        var id: Int,// 1
        var name: String,// 高管
        var children: List<UChild>
) : AbstractExpandableItem<UChild>(), MultiItemEntity {
    override fun getItemType(): Int = 1

    override fun getLevel(): Int = 1

}

data class UChild(
        var id: Int,
        var name: String,
        var url: String? = null
) : MultiItemEntity, Serializable {
    override fun getItemType(): Int = 2
}

/**
 * 审批列表
 */
data class ApproveList(
        var data: List<ApproveListBean>,
        var total: Int// 1
)

data class ApproveListBean(
        var approval_id: Int,// 1
        var title: String,// 吴美星的审批模版一
        var number: String,// SXYG201810081137216856
        var name: String,// 吴美星
        var add_time: Long,// 1538969841
        var status: Int,// int-1 审批不通过，0待审批，1审批中，2审批人都通过，3待用印，4审批真正完成, 5撤销成功
        var uid: Int,// 46
        var url: String,// http://prepewinner.oss-cn-hangzhou.aliyuncs.com/uploads/headimg/5ae18f59c9193.jpg?OSSAccessKeyId=dZwbJBSoG9OREtPi&Expires=1538990127&Signature=wUIbHIvqrA5etiK9VE8iKcdz4LM%3D
        var temName: String,// 审批模版一
        var handle: Boolean,//  true=>待经办  false=>不显示任何标志
        var mark: String,//(old,new)
        var start_time: String? = null,//请假开始时间
        var end_time: String? = null,//请假结束时间
        var holidayname: String? = null,//请假类型名字
        var kind: String? = null,//	string	用印类别(老审批)
        var status_str: String? = null,//	string	审批状态(老审批)
        var type: Int? = null//	number	类型(老审批)
)


data class NewApproveNum(
        var waitMe: Int,// 待我审批
        var overMe: Int,// 我已审批
        var happenMe: Int,// 我发起的
        var copyMe: Int// 抄送我的
)


data class ApproveRecordList(
        var data: List<ApproveRecord>,
        var total: Int// 1
)

data class ApproveRecord(
        val add_time: Long,//审批发起时间
        val approval_id: Int?,//审批ID
        val company_name: String? = null,//
        val end_time: String,//(请假)审批结束时间
        val holidayname: String?,//请假类型名称
        val kind: String?,//
        val mark: String,//new old
        val name: String?,//发起人名字
        val number: String?,//审批单号
        val reasons: String?,//审批事由
        val shortName: String?,//
        val start_time: String,//请假开始时间
        val status: Int,//审批状态 -1 审批不通过，0待审批，1审批中，2审批人都通过，3待用印，4审批真正完成, 5撤销成功
        val status_str: String?,//审批状态文字
        val table_id: Int?,
        val table_name: String?,
        val temName: String?,//审批模板名
        val title: String,//审批标题
        val type: Int?,//
        val uid: Int,//发起人ID
        val url: String//发起人头像
)