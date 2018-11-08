package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 * Created by admin on 2018/11/2.
 */
data class SystemPushBean(
        var id: Int,// 8
        var type: Int,// 202
        var title: String,// 好好好
        var name: String,// 黄成涛
        var week_id: String? = null,//
        var sub_uid: String? = null,//
        var postName: String? = null,
        var pushTime: Int
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