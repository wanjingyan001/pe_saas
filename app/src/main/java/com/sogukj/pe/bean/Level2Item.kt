package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras
import java.io.File

/**
 * Created by CH-ZH on 2018/9/20.
 */
class Level2Item : MultiItemEntity {
    var img : String ? = null
    var name : String = ""
    var type : Int = 0
    var file : File? = null
    constructor(img : String,name : String,type : Int = 0){
        this.img = img
        this.name = name
        this.type = type
    }
    constructor()
    override fun getItemType(): Int {
        return Extras.TYPE_FILE
    }
}