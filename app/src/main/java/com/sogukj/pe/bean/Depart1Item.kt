package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras.TYPE_LEVEL_1

/**
 * Created by CH-ZH on 2018/12/14.
 */
class Depart1Item : MultiItemEntity {
    var id : Int ? = null
    var name : String ? = null
    var pid : Int ? = null
    var isCanSelect : Boolean = false
    var isSelected : Boolean = false
    constructor(name : String,id:Int,pid:Int){
        this.name = name
        this.id = id
        this.pid = pid
    }
    override fun getItemType(): Int {
        return TYPE_LEVEL_1
    }

}