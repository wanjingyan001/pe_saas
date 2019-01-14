package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 * Created by admin on 2018/11/2.
 */
data class SystemPushBean(
        var id: Int,// 8
        var type: Int,//  201 会议  202 日程   203 任务  204    用印审批   205 周报
        var title: String,// 好好好
        var name: String,// 黄成涛
        var week_id: String? = null,//
        var sub_uid: String? = null,//
        var postName: String? = null,
        var pushTime: Int?,
        var satrtTime: Long?
) : Serializable, MultiItemEntity {
    override fun getItemType(): Int {
        return 4
    }

}


data class NewApprovePush(
        var id: Int,
        var type: Int,//301加急,302待审批,303撤销,304否决,305审批完成,306修改
        var title: String,
        var number: String,
        var subName: String,
        var pushTime: Int,
        var subId: Int
) : Serializable, MultiItemEntity {
    override fun getItemType(): Int {
        return if (type == 301 || type == 302) 1 else 5
    }

    val statusStr: String
        get() {
            return when (type) {
                301 -> "加急"
                302 -> "待审批"
                303 -> "撤销"
                304 -> "否决"
                305 -> "审批完成"
                306 -> "修改"
                else -> ""
            }
        }
}

data class NewProjectProcess(
        var id: Int,
        var type: Int = 100,
        var title: String,
        var cName: String,
        var subName: String,
        var pushTime: Int,
        var floor: Int,
        var pre: String
) : Serializable, MultiItemEntity {
    override fun getItemType(): Int {
        return 6
    }

}

data class PayHistory(
        var money: String,//支付总金额
        var unit_price: String,//单价
        var order_count: Int,//数量
        var content: String,//内容
        var pay_type: String,//支付方式
        var time: String,//
        var order_str: String,//订单号
        var order_time: String,//支付时间
        var type: Int,//401账号付费  402 智能文书  403 100个征信套餐  404 舆情监控额度 405 征信套餐和舆情套餐购买  406 钱包充值
        var pay_userName: String?,
        var title: String?
) : Serializable, MultiItemEntity {
    override fun getItemType(): Int {
        return 2
    }
}

/**
 * 推送提醒
 */
data class ReminderPush(
        val type: Int,//206提醒推送
        val title: String,//标题
        val id: Int,//审批id
        val content: String,//内容
        val pushTime: Int//推送时间
) : Serializable, MultiItemEntity {
    override fun getItemType(): Int {
        return 7
    }
}