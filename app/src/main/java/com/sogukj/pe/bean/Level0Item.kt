package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras

/**
 * Created by CH-ZH on 2018/9/20.
 */
class Level0Item : AbstractExpandableItem<Level1Item>, MultiItemEntity {
    var title : String = ""
    constructor(title : String){
        this.title = title
    }
    override fun getLevel(): Int {
        return 0
    }

    override fun getItemType(): Int {
        return Extras.TYPE_LEVEL_0
    }

}