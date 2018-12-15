package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras.TYPE_LEVEL_1

/**
 * Created by CH-ZH on 2018/12/14.
 */
class Depart1Item : MultiItemEntity {
    var id : Int ? = null
    var name : String ? = null
    constructor(name : String){
        this.name = name
    }
    override fun getItemType(): Int {
        return TYPE_LEVEL_1
    }

}