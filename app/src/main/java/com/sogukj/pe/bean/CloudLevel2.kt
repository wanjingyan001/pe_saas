package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras

/**
 * Created by CH-ZH on 2018/10/25.
 */
class CloudLevel2 : MultiItemEntity {
    var name = ""
    var time = ""
    var size = ""
    override fun getItemType(): Int {
        return Extras.TYPE_LEVEL_1
    }
}