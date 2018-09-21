package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras

/**
 * Created by CH-ZH on 2018/9/20.
 */
class Level1Item : AbstractExpandableItem<Level2Item>, MultiItemEntity {

    var subName : String = ""
    constructor(subName : String){
        this.subName = subName
    }
    override fun getLevel(): Int {
        return 1
    }

    override fun getItemType(): Int {
        return Extras.TYPE_LEVEL_1
    }
}