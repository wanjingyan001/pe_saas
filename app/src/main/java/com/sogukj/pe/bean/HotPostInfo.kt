package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 * Created by CH-ZH on 2018/9/6.
 */
class HotPostInfo : Serializable, MultiItemEntity {
    private var itemType: Int = 0

    companion object {
        val header = 1
        val item = 2
        val footer = 3
    }

    public fun setType(type:Int){
        itemType = type
    }

    override fun getItemType(): Int = itemType

    var icon: String? = null
    var name: String = ""
    var id: Int = 0
}