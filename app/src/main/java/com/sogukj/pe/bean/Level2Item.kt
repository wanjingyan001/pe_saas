package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras
import java.io.File

/**
 * Created by CH-ZH on 2018/9/20.
 */
class Level2Item : MultiItemEntity {
    var type : Int = 0
    var file : ProjectApproveInfo.ApproveFile? = null
    var localFile : File? = null
    var class_id : Int = 0
    constructor()
    override fun getItemType(): Int {
        return Extras.TYPE_FILE
    }
}