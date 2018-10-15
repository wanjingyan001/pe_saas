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


data class SealBean(val name: String,
                    var value: Int)

/**
 * 审批控件中的值
 */
data class ApproveValueBean(val name: String,
                            val id: String? = null,
                            val url: String? = null,
                            val size: String? = null,
                            var value: Int? = null,
                            var scal_unit: String? = null) : Serializable

/**
 * 审批人/抄送人/经办人
 */
data class Approvers(
        var sp: List<Sp>,
        var cs: Cs,
        var jr: String// 30
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
        var users: List<User>
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
        var url: String?
) : MultiItemEntity, Serializable {
    override fun getItemType(): Int = 2
}
