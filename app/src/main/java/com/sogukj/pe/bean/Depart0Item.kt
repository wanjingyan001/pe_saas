package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.AbstractExpandableItem
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras.TYPE_LEVEL_0

/**
 * Created by CH-ZH on 2018/12/14.
 */
class Depart0Item : AbstractExpandableItem<Depart1Item>, MultiItemEntity  {
    var id : Int ? = null
    var name : String ? = null
    var isCanSelect : Boolean = false
    var isSelected : Boolean = false
    constructor(name : String){
        this.name = name
    }
    override fun getLevel(): Int {
        return 1
    }

    override fun getItemType(): Int {
      return TYPE_LEVEL_0
    }

}